package com.mobdeve.s17.mco2.group88

import android.os.Bundle
import android.widget.EditText
import android.widget.Spinner
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity

class EditProfile : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_editpage)

        // EditText fields
        val firstNameField: EditText = findViewById(R.id.edit_firstname)
        val lastNameField: EditText = findViewById(R.id.edit_lastname)
        val emailField: EditText = findViewById(R.id.editTextTextEmailAddress)

        // Spinner references
        val ageSpinner: Spinner = findViewById(R.id.spinner2)
        val heightSpinner: Spinner = findViewById(R.id.spinner3)
        val weightSpinner: Spinner = findViewById(R.id.spinner4)

        // Data lists
        val ageOptions = (10..100).map { it.toString() }
        val heightOptions = (120..220).map { "$it cm" }
        val weightOptions = (30..150).map { "$it kg" }

        // Adapters
        setupSpinner(ageSpinner, ageOptions)
        setupSpinner(heightSpinner, heightOptions)
        setupSpinner(weightSpinner, weightOptions)

        val genderGroup = findViewById<android.widget.RadioGroup>(R.id.genderRadioGroup)
        val femaleButton = findViewById<android.widget.RadioButton>(R.id.femaleRadioButton)
        val maleButton = findViewById<android.widget.RadioButton>(R.id.maleRadioButton)

        fun updateGenderColors() {
            val white = getColor(R.color.white)
            val black = getColor(R.color.black)

            femaleButton.setTextColor(if (femaleButton.isChecked) white else black)
            maleButton.setTextColor(if (maleButton.isChecked) white else black)
        }

        updateGenderColors() // apply initial state when screen opens

        genderGroup.setOnCheckedChangeListener { _, _ ->
            updateGenderColors()
        }
    }

    private fun setupSpinner(spinner: Spinner, items: List<String>) {
        val adapter = ArrayAdapter(this, R.layout.profile_spinner, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }


}

// Example: capture input on Save (you can wire this to a button)
/*
val saveButton: Button = findViewById(R.id.save_button)
saveButton.setOnClickListener {
    val selectedAge = ageSpinner.selectedItem.toString()
    val firstName = firstNameField.text.toString()
    val lastName = lastNameField.text.toString()
    val email = emailField.text.toString()

    // Handle your logic here — store, send, validate, etc.
}
*/