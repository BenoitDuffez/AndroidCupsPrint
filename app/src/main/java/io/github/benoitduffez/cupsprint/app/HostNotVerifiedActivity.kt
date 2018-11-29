package io.github.benoitduffez.cupsprint.app

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import io.github.benoitduffez.cupsprint.R
import kotlinx.android.synthetic.main.host_not_verified.*

/**
 * Ask for host trust when it couldn't be verified
 */
class HostNotVerifiedActivity : Activity() {
    private var unverifiedHostname: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.host_not_verified)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        unverifiedHostname = intent.getStringExtra(KEY_HOST)
        host_not_verified_title.text = getString(R.string.host_not_verified_title, unverifiedHostname)

        host_not_verified_trust_button.setOnClickListener { validateTrust(true) }
        host_not_verified_abort_button.setOnClickListener { validateTrust(false) }
    }

    /**
     * Save choice and finish
     *
     * @param trusted Whether the host should be trusted or not
     */
    private fun validateTrust(trusted: Boolean) {
        val prefs = getSharedPreferences(HOSTS_FILE, Context.MODE_PRIVATE).edit()
        prefs.putBoolean(unverifiedHostname, trusted)
        prefs.apply()
        finish()
    }

    companion object {
        val KEY_HOST = "${HostNotVerifiedActivity::class.java.name}.ErrorText"

        private const val HOSTS_FILE = "hosts_trust"

        /**
         * Check whether host is known and trusted
         *
         * @param context  Used to retrieve the saved setting
         * @param hostname Hostname to be checked
         * @return true if the hostname was explicitly trusted, false otherwise or if unknown
         */
        fun isHostnameTrusted(context: Context, hostname: String): Boolean {
            return context.getSharedPreferences(HOSTS_FILE, Context.MODE_PRIVATE).getBoolean(hostname, false)
        }
    }
}
