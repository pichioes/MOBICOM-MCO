// Updated LoginActivity.kt - Facebook Login fixes

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
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import org.json.JSONException

class LoginActivity : AppCompatActivity() {
    private var isPasswordVisible = false
    private lateinit var dbHelper: AquaBuddyDatabaseHelper
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager

    // UI elements
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var passwordToggle: ImageButton
    private lateinit var googleSignInButton: ImageButton
    private lateinit var facebookSignInButton: ImageButton

    companion object {
        const val PREF_NAME = "AquaBuddyPrefs"
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_EMAIL = "user_email"
        const val KEY_IS_LOGGED_IN = "is_logged_in"
        const val KEY_LOGIN_TYPE = "login_type"
        const val LOGIN_TYPE_EMAIL = "email"
        const val LOGIN_TYPE_GOOGLE = "google"
        const val LOGIN_TYPE_FACEBOOK = "facebook"
        private const val RC_SIGN_IN = 9001
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Facebook SDK callback manager
        callbackManager = CallbackManager.Factory.create()

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
        setupFacebookLogin()
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
        googleSignInButton = findViewById(R.id.googleBtn)
        facebookSignInButton = findViewById(R.id.facebookBtn)

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

        // Facebook Sign-In button click
        facebookSignInButton.setOnClickListener {
            signInWithFacebook()
        }
    }

    private fun setupFacebookLogin() {
        // Register callback with LoginManager
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d(TAG, "Facebook login successful")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.d(TAG, "Facebook login cancelled")
                showLoginError("Facebook login was cancelled")
            }

            override fun onError(exception: FacebookException) {
                Log.e(TAG, "Facebook login error", exception)
                showLoginError("Facebook login failed: ${exception.message}")
            }
        })
    }

    private fun signInWithFacebook() {
        // Use only public_profile permission
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile"))
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken: $token")

        val request = GraphRequest.newMeRequest(token) { jsonObject, response ->
            try {
                val name = jsonObject?.getString("name") ?: ""
                val facebookId = jsonObject?.getString("id") ?: ""

                val email = if (jsonObject?.has("email") == true) {
                    jsonObject.getString("email")
                } else {
                    // fallback
                    "$facebookId@facebook.com"
                }

                if (name.isNotEmpty() && facebookId.isNotEmpty()) {
                    handleFacebookSignInSuccess(name, email, facebookId)
                } else {
                    showLoginError("Unable to get required information from Facebook")
                }
            } catch (e: JSONException) {
                Log.e(TAG, "Error parsing Facebook response", e)
                showLoginError("Error processing Facebook login")
            }
        }

        val parameters = Bundle()
        // Only request fields that are definitely available
        parameters.putString("fields", "id,name")
        request.parameters = parameters
        request.executeAsync()
    }

    private fun handleFacebookSignInSuccess(name: String, email: String, facebookId: String) {
        Thread {
            try {
                var user = dbHelper.getUserByEmail(email)

                if (user == null) {
                    // Create new user with Facebook account info
                    val userId = dbHelper.createFacebookUser(name, email, facebookId)
                    if (userId != -1L) {
                        user = dbHelper.getUserById(userId)
                    }
                }

                runOnUiThread {
                    if (user != null) {
                        saveUserSession(user.id, user.email, LOGIN_TYPE_FACEBOOK)
                        Toast.makeText(this, "Welcome, ${user.name}!", Toast.LENGTH_SHORT).show()
                        navigateToHome()
                    } else {
                        showLoginError("Failed to create user account. Please try again.")
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    showLoginError("An error occurred during Facebook Sign-In. Please try again.")
                    e.printStackTrace()
                }
            }
        }.start()
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            account?.let { googleAccount ->
                handleGoogleSignInSuccess(googleAccount)
            }
        } catch (e: ApiException) {
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
            showLoginError("Google Sign-In failed. Please try again.")
        }
    }

    private fun handleGoogleSignInSuccess(account: GoogleSignInAccount) {
        val email = account.email ?: ""
        val name = account.displayName ?: ""
        val googleId = account.id ?: ""

        Thread {
            try {
                var user = dbHelper.getUserByEmail(email)

                if (user == null) {
                    val userId = dbHelper.createGoogleUser(name, email, googleId)
                    if (userId != -1L) {
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

        if (!validateInput(email, password)) {
            return
        }

        Thread {
            try {
                Thread.sleep(500)
                val authenticatedUser = dbHelper.authenticateUser(email, password)

                runOnUiThread {
                    if (authenticatedUser != null) {
                        handleLoginSuccess(authenticatedUser)
                    } else {
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

        if (email.isEmpty()) {
            emailInput.error = "Email is required"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.error = "Please enter a valid email"
            isValid = false
        } else {
            emailInput.error = null
        }

        if (password.isEmpty()) {
            passwordInput.error = "Password is required"
            isValid = false
        } else {
            passwordInput.error = null
        }

        return isValid
    }

    private fun handleLoginSuccess(user: User) {
        saveUserSession(user.id, user.email, LOGIN_TYPE_EMAIL)
        Toast.makeText(this, "Welcome back, ${user.name}!", Toast.LENGTH_SHORT).show()
        navigateToHome()
    }

    private fun handleLoginFailure() {
        showLoginError("Invalid email or password. Please check your credentials.")
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

    fun getCurrentUserId(): Long {
        return sharedPreferences.getLong(KEY_USER_ID, -1L)
    }

    fun logout() {
        val loginType = sharedPreferences.getString(KEY_LOGIN_TYPE, LOGIN_TYPE_EMAIL)

        when (loginType) {
            LOGIN_TYPE_GOOGLE -> {
                googleSignInClient.signOut().addOnCompleteListener(this) {
                    clearUserSession()
                }
            }
            LOGIN_TYPE_FACEBOOK -> {
                LoginManager.getInstance().logOut()
                clearUserSession()
            }
            else -> {
                clearUserSession()
            }
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