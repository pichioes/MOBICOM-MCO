package com.mobdeve.s17.mco2.group88

import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.view.LayoutInflater
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.content.Intent
import android.view.ViewGroup


class ProfileMainPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_mainpage)

        val logoutLayer = findViewById<View>(R.id.logout_layer)
        logoutLayer.setOnClickListener {
            showLogoutPopup()  // This is your existing bottom sheet function
        }

        val deleteaccLayer = findViewById<View>(R.id.deleteacc_layer)
        deleteaccLayer.setOnClickListener{
            showDeleteAccPopup()
        }

        val goalsLayer = findViewById<View>(R.id.goals_layout)
        goalsLayer.setOnClickListener{
            showGoalsPopup()
        }

        val myAccountLayer = findViewById<View>(R.id.myaccount_layer)
        myAccountLayer.setOnClickListener {
            val intent = Intent(this, EditProfile::class.java)
            startActivity(intent)
        }

    }


    private fun showGoalsPopup() {
        val dialog = android.app.Dialog(this)
        val view = layoutInflater.inflate(R.layout.popup_goals, null)

        val confirmBtn = view.findViewById<Button>(R.id.confirm_goals_button)
        confirmBtn.setOnClickListener { dialog.dismiss() }

        dialog.setContentView(view)
        dialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setDimAmount(0.5f) // dims background; you can increase for stronger blur effect
            setGravity(android.view.Gravity.CENTER)
        }

        dialog.setCancelable(true)
        dialog.show()
    }

    private fun showDeleteAccPopup() {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.popup_deleteacc, null)

        val cancelBtn = view.findViewById<Button>(R.id.cancel_delete_button)
        val deleteBtn = view.findViewById<Button>(R.id.confirm_delete_button)

        cancelBtn.setOnClickListener { dialog.dismiss() }
        deleteBtn.setOnClickListener {
            dialog.dismiss()
            // TODO: implement logout logic
        }

        dialog.setContentView(view)
        dialog.show()
    }
    private fun showLogoutPopup() {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.popup_logout, null)

        val cancelBtn = view.findViewById<Button>(R.id.cancel_button)
        val logoutBtn = view.findViewById<Button>(R.id.logout_button)

        cancelBtn.setOnClickListener { dialog.dismiss() }
        logoutBtn.setOnClickListener {
            dialog.dismiss()
            // TODO: implement logout logic
        }

        dialog.setContentView(view)
        dialog.show()
    }
}
