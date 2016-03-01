package io.github.benoitduffez.cupsprint;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;

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

	EditText mUrl, mName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_printers);
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
}
