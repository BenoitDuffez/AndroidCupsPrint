package io.github.benoitduffez.cupsprint.app

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.View
import io.github.benoitduffez.cupsprint.AppExecutors
import io.github.benoitduffez.cupsprint.R
import kotlinx.android.synthetic.main.add_printers.*
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.util.regex.Pattern

/**
 * Called when the system needs to manually add a printer
 */
class AddPrintersActivity : Activity() {
    private val executors: AppExecutors by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_printers)
    }

    /**
     * Called when the button will be clicked
     */
    fun addPrinter(@Suppress("UNUSED_PARAMETER") button: View) {
        val url = add_url.text.toString()
        val name = add_name.text.toString()

        if (TextUtils.isEmpty(name)) {
            add_name.error = getString(R.string.err_add_printer_empty_name)
            return
        }
        if (TextUtils.isEmpty(url)) {
            add_url.error = getString(R.string.err_add_printer_empty_url)
            return
        }
        try {
            URI(url)
        } catch (e: Exception) {
            add_url.error = e.localizedMessage
            return
        }

        val prefs = getSharedPreferences(SHARED_PREFS_MANUAL_PRINTERS, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val id = prefs.getInt(PREF_NUM_PRINTERS, 0)
        Timber.d("saving printer from input: $url")
        editor.putString(PREF_URL + id, url)
        editor.putString(PREF_NAME + id, name)
        editor.putInt(PREF_NUM_PRINTERS, id + 1)
        editor.apply()

        // TODO: inform user?

        // Allow the system to process the new printer addition before we get back to the list of printers
        Handler().postDelayed({ finish() }, 200)
    }

    fun searchPrinters(@Suppress("UNUSED_PARAMETER") button: View) {
        executors.networkIO.execute {
            searchPrinters("http")
            searchPrinters("https")
        }
    }

    /**
     * Will search for printers at the scheme://xxx/printers/ URL
     *
     * @param scheme The target scheme, http or https
     */
    private fun searchPrinters(scheme: String) {
        var urlConnection: HttpURLConnection? = null
        val sb = StringBuilder()
        var server = add_server_ip.text.toString()
        if (!server.contains(":")) {
            server += ":631"
        }
        val baseHost = "$scheme://$server"
        val baseUrl = "$baseHost/printers/"
        try {
            urlConnection = URL(baseUrl).openConnection() as HttpURLConnection
            val isw = InputStreamReader(urlConnection.inputStream)
            var data = isw.read()
            while (data != -1) {
                val current = data.toChar()
                sb.append(current)
                data = isw.read()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return
        } finally {
            urlConnection?.disconnect()
        }

        /*
         * 1: URL
         * 2: Name
         * 3: Description
         * 4: Location
         * 5: Make and model
         * 6: Current state
         * pattern matching fields:                       1          2                  3               4                5              6
         */
        val p = Pattern.compile("<TR><TD><A HREF=\"([^\"]+)\">([^<]*)</A></TD><TD>([^<]*)</TD><TD>([^<]*)</TD><TD>([^<]*)</TD><TD>([^<]*)</TD></TR>\n")
        val matcher = p.matcher(sb)
        var url: String
        var name: String
        val prefs = getSharedPreferences(SHARED_PREFS_MANUAL_PRINTERS, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        var id = prefs.getInt(PREF_NUM_PRINTERS, 0)
        while (matcher.find()) {
            val path = matcher.group(1)
            url = if (path.startsWith("/")) {
                baseHost + path
            } else {
                baseUrl + path
            }
            Timber.d("saving printer from search on $scheme: $url")
            name = matcher.group(3)
            editor.putString(PREF_URL + id, url)
            editor.putString(PREF_NAME + id, name)
            id++
        }
        editor.putInt(PREF_NUM_PRINTERS, id)
        editor.apply()
    }

    companion object {
        /**
         * Shared preferences file name
         */
        const val SHARED_PREFS_MANUAL_PRINTERS = "printers"

        /**
         * Will store the number of printers manually added
         */
        const val PREF_NUM_PRINTERS = "num"

        /**
         * Will be suffixed by the printer ID. Contains the URL.
         */
        const val PREF_URL = "url"

        /**
         * Will be suffixed by the printer ID. Contains the name.
         */
        const val PREF_NAME = "name"
    }
}
