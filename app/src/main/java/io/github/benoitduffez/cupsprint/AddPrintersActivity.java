package io.github.benoitduffez.cupsprint;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.print.PrinterId;
import android.print.PrinterInfo;
import android.printservice.PrinterDiscoverySession;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import io.github.benoitduffez.cupsprint.printservice.CupsService;

/**
 * Called when the system needs to manually add a printer
 */
public class AddPrintersActivity extends Activity {
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
		CupsService service = CupsService.peekInstance();
		if (service != null) {
			final PrinterDiscoverySession session = service.getSession();
			if (session != null) {
				// Get info
				String url = mUrl.getText().toString();
				String name = mName.getText().toString();

				// Build a printer list with only one printer
				List<PrinterInfo> printersInfo = new ArrayList<>();
				final PrinterId printerId = service.generatePrinterId(url);
				printersInfo.add(new PrinterInfo.Builder(printerId, name, PrinterInfo.STATUS_IDLE).build());

				// Add it to the current printer discovery session
				session.addPrinters(printersInfo);
			}
		}

		// TODO: inform user?

		// Allow the system to process the new printer addition before we get back to the list of printers
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				finish();
			}
		}, 500);
	}
}
