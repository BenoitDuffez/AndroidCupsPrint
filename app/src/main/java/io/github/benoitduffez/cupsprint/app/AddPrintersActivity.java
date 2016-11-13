package io.github.benoitduffez.cupsprint.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.benoitduffez.cupsprint.R;

/**
 * Called when the system needs to manually add a printer
 */
public class AddPrintersActivity extends Activity {
    /**
     * Shared preferences file name
     */
    public static final String SHARED_PREFS_MANUAL_PRINTERS = "printers";

    /**
     * Will store the number of printers manually added
     */
    public static final String PREF_NUM_PRINTERS = "num";

    /**
     * Will be suffixed by the printer ID. Contains the URL.
     */
    public static final String PREF_URL = "url";

    /**
     * Will be suffixed by the printer ID. Contains the name.
     */
    public static final String PREF_NAME = "name";

    private EditText mUrl, mName, mServerIp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_printers);
        mServerIp = (EditText) findViewById(R.id.add_server_ip);
        mUrl = (EditText) findViewById(R.id.add_url);
        mName = (EditText) findViewById(R.id.add_name);
    }

    /**
     * Called when the button will be clicked
     *
     * @param button The add button
     */
    public void addPrinter(View button) {
        String url = mUrl.getText().toString();
        String name = mName.getText().toString();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, R.string.err_add_printer_empty_name, Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(url)) {
            Toast.makeText(this, R.string.err_add_printer_empty_url, Toast.LENGTH_LONG).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_MANUAL_PRINTERS, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        int id = prefs.getInt(PREF_NUM_PRINTERS, 0);
        editor.putString(PREF_URL + id, url);
        editor.putString(PREF_NAME + id, name);
        editor.putInt(PREF_NUM_PRINTERS, id + 1);
        editor.apply();

        // TODO: inform user?

        // Allow the system to process the new printer addition before we get back to the list of printers
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 200);
    }

    public void searchPrinters(View button) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                searchPrinters("http");
                searchPrinters("https");
                return null;
            }
        }.execute();
    }

    /**
     * Will search for printers at the scheme://xxx/printers/ URL
     *
     * @param scheme The target scheme, http or https
     */
    void searchPrinters(String scheme) {
        HttpURLConnection urlConnection = null;
        StringBuilder sb = new StringBuilder();
        String server = mServerIp.getText().toString();
        if (!server.contains(":")) {
            server += ":631";
        }
        final String baseUrl = String.format(Locale.ENGLISH, "%s://%s/printers/", scheme, server);
        try {
            urlConnection = (HttpURLConnection) new URL(baseUrl).openConnection();
            InputStreamReader isw = new InputStreamReader(urlConnection.getInputStream());
            int data = isw.read();
            while (data != -1) {
                char current = (char) data;
                sb.append(current);
                data = isw.read();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        /**
         * 1: URL
         * 2: Name
         * 3: Description
         * 4: Location
         * 5: Make and model
         * 6: Current state
         * pattern matching fields:                       1          2                  3               4                5              6
         */
        Pattern p = Pattern.compile("<TR><TD><A HREF=\"([^\"]+)\">([^<]+)</A></TD><TD>([^<]+)</TD><TD>([^<]+)</TD><TD>([^<]+)</TD><TD>([^<]+)</TD></TR>\n");
        Matcher matcher = p.matcher(sb);
        String url, name;
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_MANUAL_PRINTERS, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        int id = prefs.getInt(PREF_NUM_PRINTERS, 0);
        while (matcher.find()) {
            url = (baseUrl + matcher.group(1)).replace("//", "/");
            name = matcher.group(3);
            editor.putString(PREF_URL + id, url);
            editor.putString(PREF_NAME + id, name);
            id++;
        }
        editor.putInt(PREF_NUM_PRINTERS, id);
        editor.commit();
    }
}
