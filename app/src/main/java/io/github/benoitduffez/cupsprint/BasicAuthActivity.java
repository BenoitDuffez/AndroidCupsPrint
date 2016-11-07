package io.github.benoitduffez.cupsprint;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

/**
 * Ask for the HTTP basic auth credentials
 */
public class BasicAuthActivity extends Activity {
    public static final String CREDENTIALS_FILE = "basic_auth";

    public static final String KEY_BASIC_AUTH_PRINTERS_URL = BasicAuthActivity.class.getName() + ".PrinterUrl";

    public static final String KEY_BASIC_AUTH_LOGIN = BasicAuthActivity.class.getName() + ".Login";

    public static final String KEY_BASIC_AUTH_PASSWORD = BasicAuthActivity.class.getName() + ".Password";

    private static final String KEY_BASIC_AUTH_NUMBER = BasicAuthActivity.class.getName() + ".Number";

    /**
     * See if we have already saved credentials for this server
     *
     * @param fullUrl Server URL (may include the printer name)
     * @param prefs   Shared preferences to search credentials from
     * @return The credentials position in the preferences files, or -1 if it wasn't found
     */
    public static int findSavedCredentialsId(String fullUrl, SharedPreferences prefs) {
        for (int i = 0; i < prefs.getInt(KEY_BASIC_AUTH_NUMBER, 0); i++) {
            String url = prefs.getString(KEY_BASIC_AUTH_PRINTERS_URL + i, null);
            if (url != null && fullUrl.startsWith(url)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.basic_auth);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        final EditText userName = (EditText) findViewById(R.id.basic_auth_login);
        final EditText password = (EditText) findViewById(R.id.basic_auth_password);

        final String printersUrl = getIntent().getStringExtra(KEY_BASIC_AUTH_PRINTERS_URL);

        SharedPreferences prefs = getSharedPreferences(CREDENTIALS_FILE, MODE_PRIVATE);

        final int numCredentials = prefs.getInt(KEY_BASIC_AUTH_NUMBER, 0);
        int foundId = findSavedCredentialsId(printersUrl, prefs);
        final int targetId;
        if (foundId >= 0) {
            targetId = foundId;
            userName.setText(prefs.getString(KEY_BASIC_AUTH_LOGIN + foundId, ""));
            password.setText(prefs.getString(KEY_BASIC_AUTH_PASSWORD + foundId, ""));
        } else {
            targetId = numCredentials;
        }

        findViewById(R.id.basic_auth_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor prefs = getSharedPreferences(CREDENTIALS_FILE, MODE_PRIVATE).edit();
                prefs.putString(KEY_BASIC_AUTH_LOGIN + targetId, userName.getText().toString());
                prefs.putString(KEY_BASIC_AUTH_PASSWORD + targetId, password.getText().toString());
                prefs.putString(KEY_BASIC_AUTH_PRINTERS_URL + targetId, printersUrl);
                prefs.putInt(KEY_BASIC_AUTH_NUMBER, numCredentials + 1);
                prefs.apply();

                finish();
            }
        });
    }
}
