package com.bitcoin.merchant.app.screens.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.text.Html
import com.bitcoin.merchant.app.R
import com.bitcoin.merchant.app.screens.legal.PrivacyPolicyFragment
import com.bitcoin.merchant.app.screens.legal.ServiceTermsFragment
import com.bitcoin.merchant.app.screens.legal.TermsOfUseFragment
import com.bitcoin.merchant.app.util.PrefsUtil

object DialogHelper {
    fun show(activity: Activity, title: String?, message: String?, runner: () -> Unit) {
        activity.runOnUiThread {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(title)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(activity.getString(android.R.string.ok)) { dialog: DialogInterface, _: Int ->
                        dialog.dismiss()
                        runner.invoke()
                    }
            if (!activity.isFinishing) {
                builder.create().show()
            }
        }
    }

    fun showEndUserLegalAgreement(activity: Activity) {
        val resources = activity.resources
        val link1 = """<a href="${ServiceTermsFragment.URL}">${resources.getString(R.string.menu_service_terms)}</a>."""
        val link2 = """<a href="${TermsOfUseFragment.URL}">${resources.getString(R.string.menu_terms_of_use)}</a>."""
        val link3 = """<a href="${PrivacyPolicyFragment.URL}">${resources.getString(R.string.menu_privacy_policy)}</a>."""
        val message = resources.getString(R.string.contract_agreement_summary, link1, link2, link3);
        val dialog: AlertDialog = AlertDialog.Builder(activity)
                .setMessage(Html.fromHtml(message))
                .setCancelable(true)
                .setNegativeButton(resources.getString(android.R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                    activity.finish();
                }
                .setPositiveButton(resources.getString(R.string.contract_button_ok)) { dialog, _ ->
                    dialog.dismiss()
                    PrefsUtil.getInstance(activity).setValue(PrefsUtil.MERCHANT_KEY_EULA, true)
                }
                .create()
        dialog.show()
    }
}