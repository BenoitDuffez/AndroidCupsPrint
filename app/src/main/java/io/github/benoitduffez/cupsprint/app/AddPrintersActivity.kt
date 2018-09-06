package io.github.benoitduffez.cupsprint.app

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.Toast
import io.github.benoitduffez.cupsprint.R
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.regex.Pattern

/**
 * Called when the system needs to manually add a printer
 */
class AddPrintersActivity : Activity() {
    private var mUrl: EditText? = null
    private var mName: EditText? = null
    private var mServerIp: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_printers)
        mServerIp = findViewById<View>(R.id.add_server_ip) as EditText
        mUrl = findViewById<View>(R.id.add_url) as EditText
        mName = findViewById<View>(R.id.add_name) as EditText
    }

    /**
     * Called when the button will be clicked
     *
     * @param button The add button
     */
    fun addPrinter(button: View) {
        val url = mUrl!!.text.toString()
        val name = mName!!.text.toString()

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, R.string.err_add_printer_empty_name, Toast.LENGTH_LONG).show()
            return
        }
        if (TextUtils.isEmpty(url)) {
            Toast.makeText(this, R.string.err_add_printer_empty_url, Toast.LENGTH_LONG).show()
            return
        }

        val prefs = getSharedPreferences(SHARED_PREFS_MANUAL_PRINTERS, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val id = prefs.getInt(PREF_NUM_PRINTERS, 0)
        editor.putString(PREF_URL + id, url)
        editor.putString(PREF_NAME + id, name)
        editor.putInt(PREF_NUM_PRINTERS, id + 1)
        editor.apply()

        // TODO: inform user?

        // Allow the system to process the new printer addition before we get back to the list of printers
        Handler().postDelayed({ finish() }, 200)
    }

    fun searchPrinters(button: View) {
        object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg params: Void): Void? {
                searchPrinters("http")
                searchPrinters("https")
                return null
            }
        }.execute()
    }

    /**
     * Will search for printers at the scheme://xxx/printers/ URL
     *
     * @param scheme The target scheme, http or https
     */
    internal fun searchPrinters(scheme: String) {
        var urlConnection: HttpURLConnection? = null
        val sb = StringBuilder()
        var server = mServerIp!!.text.toString()
        if (!server.contains(":")) {
            server += ":631"
        }
        val baseUrl = String.format(Locale.ENGLISH, "%s://%s/printers/", scheme, server)
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
        val p = Pattern.compile("<TR><TD><A HREF=\"([^\"]+)\">([^<]+)</A></TD><TD>([^<]+)</TD><TD>([^<]+)</TD><TD>([^<]+)</TD><TD>([^<]+)</TD></TR>\n")
        val matcher = p.matcher(sb)
        var url: String
        var name: String
        val prefs = getSharedPreferences(SHARED_PREFS_MANUAL_PRINTERS, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        var id = prefs.getInt(PREF_NUM_PRINTERS, 0)
        while (matcher.find()) {
            url = (baseUrl + matcher.group(1)).replace("//", "/")
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
        val SHARED_PREFS_MANUAL_PRINTERS = "printers"

        /**
         * Will store the number of printers manually added
         */
        val PREF_NUM_PRINTERS = "num"

        /**
         * Will be suffixed by the printer ID. Contains the URL.
         */
        val PREF_URL = "url"

        /**
         * Will be suffixed by the printer ID. Contains the name.
         */
        val PREF_NAME = "name"
    }
}
