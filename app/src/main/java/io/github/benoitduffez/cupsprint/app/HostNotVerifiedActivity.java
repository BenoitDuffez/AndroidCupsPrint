package io.github.benoitduffez.cupsprint.app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.github.benoitduffez.cupsprint.R;

/**
 * Ask for host trust when it couldn't be verified
 */
public class HostNotVerifiedActivity extends Activity {
    public static final String KEY_HOST = HostNotVerifiedActivity.class.getName() + ".ErrorText";

    private static final String HOSTS_FILE = "hosts_trust";

    private String mUnverifiedHostname;

    /**
     * Check whether host is known and trusted
     *
     * @param context  Used to retrieve the saved setting
     * @param hostname Hostname to be checked
     * @return true if the hostname was explicitly trusted, false otherwise or if unknown
     */
    public static boolean isHostnameTrusted(Context context, String hostname) {
        return context.getSharedPreferences(HOSTS_FILE, MODE_PRIVATE).getBoolean(hostname, false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.host_not_verified);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView title = (TextView) findViewById(R.id.host_not_verified_title);
        mUnverifiedHostname = getIntent().getStringExtra(KEY_HOST);
        title.setText(getString(R.string.host_not_verified_title, mUnverifiedHostname));

        findViewById(R.id.host_not_verified_trust_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateTrust(true);
            }
        });
        findViewById(R.id.host_not_verified_abort_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateTrust(false);
            }
        });
    }

    /**
     * Save choice and finish
     *
     * @param trusted Whether the host should be trusted or not
     */
    void validateTrust(boolean trusted) {
        SharedPreferences.Editor prefs = getSharedPreferences(HOSTS_FILE, MODE_PRIVATE).edit();
        prefs.putBoolean(mUnverifiedHostname, trusted);
        prefs.apply();
        finish();
    }
}
