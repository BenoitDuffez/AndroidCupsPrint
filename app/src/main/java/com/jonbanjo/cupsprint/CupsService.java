package com.jonbanjo.cupsprint;

import android.printservice.PrintJob;
import android.printservice.PrintService;
import android.printservice.PrinterDiscoverySession;

/**
 * CUPS print service
 */
public class CupsService extends PrintService {
	@Override
	protected PrinterDiscoverySession onCreatePrinterDiscoverySession() {
		return new CupsPrinterDiscoverySession(this);
	}

	@Override
	protected void onRequestCancelPrintJob(PrintJob printJob) {
		// TODO
	}

	@Override
	protected void onPrintJobQueued(PrintJob printJob) {
	}

}
