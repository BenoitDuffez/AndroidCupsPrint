package io.github.benoitduffez.cupsprint.app

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.ViewGroup
import io.github.benoitduffez.cupsprint.R
import kotlinx.android.synthetic.main.basic_auth.*

/**
 * Ask for the HTTP basic auth credentials
 */
class BasicAuthActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.basic_auth)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val printersUrl = intent.getStringExtra(KEY_BASIC_AUTH_PRINTERS_URL)
        val prefs = getSharedPreferences(CREDENTIALS_FILE, Context.MODE_PRIVATE)

        val numCredentials = prefs.getInt(KEY_BASIC_AUTH_NUMBER, 0)
        val foundId = findSavedCredentialsId(printersUrl?:"", prefs)
        val targetId: Int
        if (foundId >= 0) {
            targetId = foundId
            basic_auth_login.setText(prefs.getString(KEY_BASIC_AUTH_LOGIN + foundId, ""))
            basic_auth_password.setText(prefs.getString(KEY_BASIC_AUTH_PASSWORD + foundId, ""))
        } else {
            targetId = numCredentials
        }

        basic_auth_button.setOnClickListener {
            val editPrefs = getSharedPreferences(CREDENTIALS_FILE, Context.MODE_PRIVATE).edit()
            editPrefs.putString(KEY_BASIC_AUTH_LOGIN + targetId, basic_auth_login.text.toString())
            editPrefs.putString(KEY_BASIC_AUTH_PASSWORD + targetId, basic_auth_password.text.toString())
            editPrefs.putString(KEY_BASIC_AUTH_PRINTERS_URL + targetId, printersUrl)
            editPrefs.putInt(KEY_BASIC_AUTH_NUMBER, numCredentials + 1)
            editPrefs.apply()

            finish()
        }
    }

    companion object {
        const val CREDENTIALS_FILE = "basic_auth"
        val KEY_BASIC_AUTH_PRINTERS_URL = "${BasicAuthActivity::class.java.name}.PrinterUrl"
        val KEY_BASIC_AUTH_LOGIN = "${BasicAuthActivity::class.java.name}.Login"
        val KEY_BASIC_AUTH_PASSWORD = "${BasicAuthActivity::class.java.name}.Password"
        internal val KEY_BASIC_AUTH_NUMBER = "${BasicAuthActivity::class.java.name}.Number"

        /**
         * See if we have already saved credentials for this server
         *
         * @param fullUrl Server URL (may include the printer name)
         * @param prefs   Shared preferences to search credentials from
         * @return The credentials position in the preferences files, or -1 if it wasn't found
         */
        fun findSavedCredentialsId(fullUrl: String, prefs: SharedPreferences): Int {
            for (i in 0 until prefs.getInt(KEY_BASIC_AUTH_NUMBER, 0)) {
                val url = prefs.getString(KEY_BASIC_AUTH_PRINTERS_URL + i, null)
                if (url != null && fullUrl.startsWith(url)) {
                    return i
                }
            }
            return -1
        }
    }
}
