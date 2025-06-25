package com.mobdeve.s17.mco2.group88

import android.app.AlertDialog
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class CreateProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_createprofile)

        // Views
        val backButton = findViewById<ImageButton>(R.id.createprofileBackButton)
        val continueButton = findViewById<Button>(R.id.continueButton)
        val nameInput = findViewById<EditText>(R.id.nameInput)
        val ageInput = findViewById<EditText>(R.id.ageInput)
        val heightInput = findViewById<EditText>(R.id.heightInput)
        val weightInput = findViewById<EditText>(R.id.weightInput)
        val genderGroup = findViewById<RadioGroup>(R.id.genderRadioGroup)

        // Back navigation
        backButton.setOnClickListener {
            finish()
        }

        // Continue Button logic
        continueButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val age = ageInput.text.toString().trim()
            val height = heightInput.text.toString().trim()
            val weight = weightInput.text.toString().trim()
            val selectedGenderId = genderGroup.checkedRadioButtonId

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // You could also validate age/height/weight here if needed

            // Show confirmation popup
            val dialogView = layoutInflater.inflate(R.layout.popup_accountcreated, null)
            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create()

            dialog.show()

            // Popup size and transparency
            dialog.window?.apply {
                setLayout(
                    resources.getDimensionPixelSize(R.dimen.popup_width),
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setBackgroundDrawableResource(android.R.color.transparent)
                setGravity(Gravity.CENTER)
            }

            val okButton = dialogView.findViewById<Button>(R.id.btnGetStarted)
            okButton.setOnClickListener {
                dialog.dismiss()
                Toast.makeText(this, "Welcome, $name!", Toast.LENGTH_SHORT).show()
                // Proceed to home or another activity if needed
                // startActivity(Intent(this, HomeActivity::class.java))
                // finish()
            }
        }
    }
}
