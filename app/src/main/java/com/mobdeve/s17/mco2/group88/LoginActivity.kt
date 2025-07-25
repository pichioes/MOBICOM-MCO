package com.mobdeve.s17.mco2.group88

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

class LoginActivity : AppCompatActivity() {
    private var isPasswordVisible = false
    private lateinit var dbHelper: AquaBuddyDatabaseHelper
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var googleSignInClient: GoogleSignInClient

    // UI elements
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var passwordToggle: ImageButton
    private lateinit var googleSignInButton: ImageButton

    companion object {
        const val PREF_NAME = "AquaBuddyPrefs"
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_EMAIL = "user_email"
        const val KEY_IS_LOGGED_IN = "is_logged_in"
        const val KEY_LOGIN_TYPE = "login_type"
        const val LOGIN_TYPE_EMAIL = "email"
        const val LOGIN_TYPE_GOOGLE = "google"
        private const val RC_SIGN_IN = 9001
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize database helper and shared preferences
        dbHelper = AquaBuddyDatabaseHelper(this)
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        // Configure Google Sign-In
        configureGoogleSignIn()

        // Check if user is already logged in
        if (isUserLoggedIn()) {
            navigateToHome()
            return
        }

        // Extend layout into status bar
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = ContextCompat.getColor(this, R.color.primary)

        setContentView(R.layout.login_signin)
        initializeViews()
        setupClickListeners()
    }

    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun initializeViews() {
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.loginButton)
        passwordToggle = findViewById(R.id.passwordToggle)
        googleSignInButton = findViewById(R.id.imageButton1) // Google button

        val forgotPasswordText: TextView = findViewById(R.id.textForgotPassword)
        val signUpText: TextView = findViewById(R.id.textSignUpHere)

        // Navigate to ForgotPassword
        forgotPasswordText.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        // Navigate to Signup
        signUpText.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun setupClickListeners() {
        // Toggle password visibility
        passwordToggle.setOnClickListener {
            togglePasswordVisibility()
        }

        // Login button click
        loginButton.setOnClickListener {
            attemptLogin()
        }

        // Google Sign-In button click
        googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            // Signed in successfully, show authenticated UI
            account?.let { googleAccount ->
                handleGoogleSignInSuccess(googleAccount)
            }
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
            showLoginError("Google Sign-In failed. Please try again.")
        }
    }

    private fun handleGoogleSignInSuccess(account: GoogleSignInAccount) {
        val email = account.email ?: ""
        val name = account.displayName ?: ""
        val googleId = account.id ?: ""

        // Check if user exists in database, if not create new user
        Thread {
            try {
                var user = dbHelper.getUserByEmail(email)

                if (user == null) {
                    // Create new user with Google account info
                    val userId = dbHelper.createGoogleUser(name, email, googleId)
                    if (userId != -1L) {
                        // Get the user from database after creation (this will have all required fields)
                        user = dbHelper.getUserById(userId)
                    }
                }

                runOnUiThread {
                    if (user != null) {
                        saveUserSession(user.id, user.email, LOGIN_TYPE_GOOGLE)
                        Toast.makeText(this, "Welcome, ${user.name}!", Toast.LENGTH_SHORT).show()
                        navigateToHome()
                    } else {
                        showLoginError("Failed to create user account. Please try again.")
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    showLoginError("An error occurred during Google Sign-In. Please try again.")
                    e.printStackTrace()
                }
            }
        }.start()
    }

    private fun togglePasswordVisibility() {
        val position = passwordInput.selectionStart
        if (isPasswordVisible) {
            passwordInput.transformationMethod = PasswordTransformationMethod.getInstance()
            passwordToggle.setImageResource(R.drawable.password_eyeclosed)
        } else {
            passwordInput.transformationMethod = null
            passwordToggle.setImageResource(R.drawable.password_eyeopen)
        }
        passwordInput.setSelection(position)
        isPasswordVisible = !isPasswordVisible
    }

    private fun attemptLogin() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        // Validate input
        if (!validateInput(email, password)) {
            return
        }

        // Perform authentication in background thread
        Thread {
            try {
                // Simulate network delay (optional - remove in production)
                Thread.sleep(500)

                val authenticatedUser = dbHelper.authenticateUser(email, password)

                runOnUiThread {
                    if (authenticatedUser != null) {
                        // Login successful
                        handleLoginSuccess(authenticatedUser)
                    } else {
                        // Login failed
                        handleLoginFailure()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    showLoginError("An error occurred during login. Please try again.")
                    e.printStackTrace()
                }
            }
        }.start()
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

        // Validate email
        if (email.isEmpty()) {
            emailInput.error = "Email is required"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.error = "Please enter a valid email"
            isValid = false
        } else {
            emailInput.error = null
        }

        // Validate password
        if (password.isEmpty()) {
            passwordInput.error = "Password is required"
            isValid = false
        } else {
            passwordInput.error = null
        }

        return isValid
    }

    private fun handleLoginSuccess(user: User) {
        // Save user session
        saveUserSession(user.id, user.email, LOGIN_TYPE_EMAIL)

        // Show success message
        Toast.makeText(this, "Welcome back, ${user.name}!", Toast.LENGTH_SHORT).show()

        // Navigate to home
        navigateToHome()
    }

    private fun handleLoginFailure() {
        showLoginError("Invalid email or password. Please check your credentials.")

        // Clear password field for security
        passwordInput.setText("")
    }

    private fun saveUserSession(userId: Long, email: String, loginType: String = LOGIN_TYPE_EMAIL) {
        sharedPreferences.edit().apply {
            putLong(KEY_USER_ID, userId)
            putString(KEY_USER_EMAIL, email)
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_LOGIN_TYPE, loginType)
            apply()
        }
    }

    private fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showLoginError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
    }

    // Utility function to get current user ID (use this in other activities)
    fun getCurrentUserId(): Long {
        return sharedPreferences.getLong(KEY_USER_ID, -1L)
    }

    // Updated logout function to handle Google Sign-Out
    fun logout() {
        val loginType = sharedPreferences.getString(KEY_LOGIN_TYPE, LOGIN_TYPE_EMAIL)

        if (loginType == LOGIN_TYPE_GOOGLE) {
            googleSignInClient.signOut().addOnCompleteListener(this) {
                // Clear shared preferences after Google sign out
                clearUserSession()
            }
        } else {
            clearUserSession()
        }
    }

    private fun clearUserSession() {
        sharedPreferences.edit().apply {
            clear()
            apply()
        }

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}