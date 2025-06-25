package com.mobdeve.s17.mco2.group88

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class OtpBottomSheetDialog : BottomSheetDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        val view: View = LayoutInflater.from(context).inflate(R.layout.popup_otp, null)
        dialog.setContentView(view)
        return dialog
    }
}
