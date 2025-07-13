package com.mobdeve.s17.mco2.group88

import android.os.Bundle
import android.widget.EditText
import android.widget.Spinner
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.Button
import android.view.View
import androidx.appcompat.widget.SwitchCompat

class EditProfile : AppCompatActivity() {

    // Variables for gender selection
    private var selectedGender: String = "Female"  // Default gender is Female

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_editpage)

        // EditText fields for user data
        val firstNameField: EditText = findViewById(R.id.edit_firstname)
        val lastNameField: EditText = findViewById(R.id.edit_lastname)
        val emailField: EditText = findViewById(R.id.editTextTextEmailAddress)

        // Spinner references for age, height, and weight
        val ageSpinner: Spinner = findViewById(R.id.spinner2)
        val heightSpinner: Spinner = findViewById(R.id.spinner3)
        val weightSpinner: Spinner = findViewById(R.id.spinner4)

        // Data lists for spinners
        val ageOptions = (10..100).map { it.toString() }
        val heightOptions = (120..220).map { "$it cm" }
        val weightOptions = (30..150).map { "$it kg" }

        // Set up spinners with their respective data
        setupSpinner(ageSpinner, ageOptions)
        setupSpinner(heightSpinner, heightOptions)
        setupSpinner(weightSpinner, weightOptions)

        // Gender Button Setup
        val femaleButton: Button = findViewById(R.id.femaleButton)
        val maleButton: Button = findViewById(R.id.maleButton)

        // Set initial background color for buttons
        femaleButton.setBackgroundColor(resources.getColor(R.color.colorSelected))
        maleButton.setBackgroundColor(resources.getColor(R.color.colorUnselected))

        // OnClickListener for Female Button
        femaleButton.setOnClickListener {
            selectedGender = "Female"  // Set selected gender to Female
            femaleButton.setBackgroundColor(resources.getColor(R.color.colorSelected))  // Highlight Female button
            maleButton.setBackgroundColor(resources.getColor(R.color.colorUnselected))  // Reset Male button
        }

        // OnClickListener for Male Button
        maleButton.setOnClickListener {
            selectedGender = "Male"  // Set selected gender to Male
            maleButton.setBackgroundColor(resources.getColor(R.color.colorSelected))  // Highlight Male button
            femaleButton.setBackgroundColor(resources.getColor(R.color.colorUnselected))  // Reset Female button
        }

        // Save Button: When clicked, it saves the data and navigates back to ProfileMainPage
        val saveButton = findViewById<Button>(R.id.save_button)
        saveButton.setOnClickListener {
            // You can use the data entered in the fields here
            // For example, firstNameField.text.toString() to get the first name
            val intent = Intent(this, ProfileMainPage::class.java)
            startActivity(intent)
            finish()  // Optional: prevents returning to EditProfile via back button
        }

    }

    // Function to set up spinners with data
    private fun setupSpinner(spinner: Spinner, items: List<String>) {
        val adapter = ArrayAdapter(this, R.layout.profile_spinner, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    // Function to handle Female button click
    fun onFemaleClick(view: View) {
        // Update background color and gender selection
        findViewById<Button>(R.id.femaleButton).setBackgroundColor(resources.getColor(R.color.colorSelected))
        findViewById<Button>(R.id.maleButton).setBackgroundColor(resources.getColor(R.color.colorUnselected))
        selectedGender = "Female"
    }

    // Function to handle Male button click
    fun onMaleClick(view: View) {
        // Update background color and gender selection
        findViewById<Button>(R.id.maleButton).setBackgroundColor(resources.getColor(R.color.colorSelected))
        findViewById<Button>(R.id.femaleButton).setBackgroundColor(resources.getColor(R.color.colorUnselected))
        selectedGender = "Male"
    }

}
