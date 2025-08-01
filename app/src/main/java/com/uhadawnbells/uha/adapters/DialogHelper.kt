package com.uhadawnbells.uha.adapters

import androidx.appcompat.app.AppCompatActivity
import com.uhadawnbells.uha.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DialogHelper(private val context: AppCompatActivity) {

    fun showAboutDialog() {
        MaterialAlertDialogBuilder(context, R.style.CustomDialogTheme)
            .setTitle(context.getString(R.string.about_dialog_title))
            .setIcon(R.drawable.ic_info)
            .setMessage(context.getString(R.string.about_message))
            .setPositiveButton(context.getString(R.string.close_button)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    fun showContactDialog() {
        MaterialAlertDialogBuilder(context, R.style.CustomDialogTheme)
            .setTitle(context.getString(R.string.contact_dialog_title))
            .setIcon(R.drawable.ic_contact)
            .setMessage(context.getString(R.string.contact_message))
            .setPositiveButton(context.getString(R.string.close_button)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    fun showPrivacyPolicyDialog(onReadFullPolicy: () -> Unit) {
        MaterialAlertDialogBuilder(context, R.style.CustomDialogTheme)
            .setTitle(context.getString(R.string.privacy_policy_dialog_title))
            .setIcon(R.drawable.ic_privacy)
            .setMessage(context.getString(R.string.privacy_message))
            .setPositiveButton(context.getString(R.string.close_button)) { dialog, _ ->
                dialog.dismiss()
                onReadFullPolicy()
            }
            .show()
    }

    fun showTermsConditionsDialog(onReadFullTerms: () -> Unit) {
        MaterialAlertDialogBuilder(context, R.style.CustomDialogTheme)
            .setTitle(context.getString(R.string.terms_conditions_dialog_title))
            .setIcon(R.drawable.ic_terms)
            .setMessage(context.getString(R.string.terms_message))
            .setPositiveButton(context.getString(R.string.close_button)) { dialog, _ ->
                dialog.dismiss()
                onReadFullTerms()
            }
            .show()
    }

    fun showHelpSupportDialog(onFAQ: () -> Unit) {
        MaterialAlertDialogBuilder(context, R.style.CustomDialogTheme)
            .setTitle(context.getString(R.string.help_support_dialog_title))
            .setIcon(R.drawable.ic_help)
            .setMessage(context.getString(R.string.help_message))
            .setPositiveButton(context.getString(R.string.close_button)) { dialog, _ ->
                dialog.dismiss()
                onFAQ()
            }
            .show()
    }
}