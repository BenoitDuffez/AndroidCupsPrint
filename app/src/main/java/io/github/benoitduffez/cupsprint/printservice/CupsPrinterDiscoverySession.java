/*
 * CupsPrinterDiscoverySession.java
 *
 * Copyright (c) 2015, Benoit Duffez. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package io.github.benoitduffez.cupsprint.printservice;

import android.os.AsyncTask;
import android.print.PrintAttributes;
import android.print.PrinterCapabilitiesInfo;
import android.print.PrinterId;
import android.print.PrinterInfo;
import android.printservice.PrintService;
import android.printservice.PrinterDiscoverySession;
import android.util.Log;

import com.jonbanjo.detect.MdnsServices;
import com.jonbanjo.detect.PrinterRec;
import com.jonbanjo.detect.PrinterResult;

import org.cups4j.CupsClient;
import org.cups4j.CupsPrinter;
import org.cups4j.operations.ipp.IppGetPrinterAttributesOperation;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ch.ethz.vppserver.ippclient.IppResult;
import ch.ethz.vppserver.schema.ippclient.Attribute;
import ch.ethz.vppserver.schema.ippclient.AttributeGroup;
import ch.ethz.vppserver.schema.ippclient.AttributeValue;
import io.github.benoitduffez.cupsprint.CupsPrintApp;

/**
 * CUPS printer discovery class
 */
public class CupsPrinterDiscoverySession extends PrinterDiscoverySession {

	private static final double MM_IN_MILS = 39.3700787;

	private PrintService mPrintService;

	public CupsPrinterDiscoverySession(PrintService context) {
		mPrintService = context;
	}

	/**
	 * Called when the framework wants to find/discover printers
	 *
	 * @param priorityList The list of printers that the user selected sometime in the past, that need to be checked first
	 */
	@Override
	public void onStartPrinterDiscovery(List<PrinterId> priorityList) {
		new AsyncTask<Void, Void, List<PrinterRec>>() {
			@Override
			protected List<PrinterRec> doInBackground(Void... params) {
				return scanPrinters();
			}

			@Override
			protected void onPostExecute(List<PrinterRec> printers) {
				onPrintersDiscovered(printers);
			}
		}.execute();
	}

	/**
	 * Called when mDNS printers are found
	 *
	 * @param printers The list of printers found using mDNS
	 */
	private void onPrintersDiscovered(List<PrinterRec> printers) {
		Log.i(CupsPrintApp.LOG_TAG, "onPrintersDiscovered(" + printers + ")");
		List<PrinterInfo> printersInfo = new ArrayList<>(printers.size());
		for (PrinterRec rec : printers) {
			final String localId = rec.getProtocol() + "://" + rec.getHost() + ":" + rec.getPort() + "/printers/" + rec.getQueue();
			final PrinterId printerId = mPrintService.generatePrinterId(localId);
			printersInfo.add(new PrinterInfo.Builder(printerId, rec.getNickname(), PrinterInfo.STATUS_IDLE).build());
		}

		addPrinters(printersInfo);
	}

	/**
	 * Ran in the background thread, will check whether a printer is valid
	 *
	 * @return The printer capabilities if the printer is available, null otherwise
	 */
	private PrinterCapabilitiesInfo checkPrinter(final String url, final PrinterId printerId) throws Exception {
		if (url == null || (!url.startsWith("http://") && !url.startsWith("https://"))) {
			return null;
		}
		URL printerURL = new URL(url);

		String clientUrl = url.substring(0, url.substring(0, url.lastIndexOf('/')).lastIndexOf('/'));
		URL clientURL = new URL(clientUrl);

		CupsClient client = new CupsClient(clientURL);
		CupsPrinter testPrinter = client.getPrinter(printerURL);
		if (testPrinter == null) {
			Log.e(CupsPrintApp.LOG_TAG, "Printer not found");
		} else {
			IppGetPrinterAttributesOperation op = new IppGetPrinterAttributesOperation();
			PrinterCapabilitiesInfo.Builder builder = new PrinterCapabilitiesInfo.Builder(printerId);
			IppResult ippAttributes = op.request(printerURL, new HashMap<String, String>());
			boolean colorDefault = false;
			int colorMode = 0;
			int marginMilsTop = 0, marginMilsRight = 0, marginMilsBottom = 0, marginMilsLeft = 0;
			for (AttributeGroup attributeGroup : ippAttributes.getAttributeGroupList()) {
				for (Attribute attribute : attributeGroup.getAttribute()) {
					if ("media-default".equals(attribute.getName())) {
						final PrintAttributes.MediaSize mediaSize = CupsPrinterDiscoveryUtils.getMediaSizeFromAttributeValue(attribute.getAttributeValue().get(0));
						if (mediaSize != null) {
							builder.addMediaSize(mediaSize, true);
						}
					} else if ("media-supported".equals(attribute.getName())) {
						for (AttributeValue attributeValue : attribute.getAttributeValue()) {
							final PrintAttributes.MediaSize mediaSize = CupsPrinterDiscoveryUtils.getMediaSizeFromAttributeValue(attributeValue);
							if (mediaSize != null) {
								builder.addMediaSize(mediaSize, false);
							}
						}
					} else if ("printer-resolution-default".equals(attribute.getName())) {
						builder.addResolution(CupsPrinterDiscoveryUtils.getResolutionFromAttributeValue("0", attribute.getAttributeValue().get(0)), true);
					} else if ("printer-resolution-supported".equals(attribute.getName())) {
						for (AttributeValue attributeValue : attribute.getAttributeValue()) {
							builder.addResolution(CupsPrinterDiscoveryUtils.getResolutionFromAttributeValue(attributeValue.getTag(), attributeValue), false);
						}
					} else if ("print-color-mode-supported".equals(attribute.getName())) {
						for (AttributeValue attributeValue : attribute.getAttributeValue()) {
							if ("monochrome".equals(attributeValue.getValue())) {
								colorMode |= PrintAttributes.COLOR_MODE_MONOCHROME;
							} else if ("color".equals(attributeValue.getValue())) {
								colorMode |= PrintAttributes.COLOR_MODE_COLOR;
							}
						}
					} else if ("print-color-mode-default".equals(attribute.getName())) {
						colorDefault = true;
					} else if ("media-left-margin-supported".equals(attribute.getName())) {
						for (AttributeValue attributeValue : attribute.getAttributeValue()) {
							if (Integer.parseInt(attributeValue.getValue()) < marginMilsLeft || marginMilsLeft == 0) {
								marginMilsLeft = (int) (MM_IN_MILS * Integer.parseInt(attributeValue.getValue()));
							}
						}
					} else if ("media-right-margin-supported".equals(attribute.getName())) {
						for (AttributeValue attributeValue : attribute.getAttributeValue()) {
							if (Integer.parseInt(attributeValue.getValue()) < marginMilsRight || marginMilsRight == 0) {
								marginMilsRight = (int) (MM_IN_MILS * Integer.parseInt(attributeValue.getValue()));
							}
						}
					} else if ("media-top-margin-supported".equals(attribute.getName())) {
						for (AttributeValue attributeValue : attribute.getAttributeValue()) {
							if (Integer.parseInt(attributeValue.getValue()) < marginMilsTop || marginMilsTop == 0) {
								marginMilsTop = (int) (MM_IN_MILS * Integer.parseInt(attributeValue.getValue()));
							}
						}
					} else if ("media-bottom-margin-supported".equals(attribute.getName())) {
						for (AttributeValue attributeValue : attribute.getAttributeValue()) {
							if (Integer.parseInt(attributeValue.getValue()) < marginMilsBottom || marginMilsBottom == 0) {
								marginMilsBottom = (int) (MM_IN_MILS * Integer.parseInt(attributeValue.getValue()));
							}
						}
					}
				}
			}
			builder.setColorModes(colorMode, colorDefault ? PrintAttributes.COLOR_MODE_COLOR : PrintAttributes.COLOR_MODE_MONOCHROME);
			builder.setMinMargins(new PrintAttributes.Margins(marginMilsLeft, marginMilsTop, marginMilsRight, marginMilsBottom));
			return builder.build();
		}
		return null;
	}

	/**
	 * Called when the printer has been checked over IPP(S)
	 *
	 * @param printerId               The printer
	 * @param printerCapabilitiesInfo null if the printer isn't available anymore, otherwise contains the printer capabilities
	 */
	private void onPrinterChecked(PrinterId printerId, PrinterCapabilitiesInfo printerCapabilitiesInfo) {
		if (printerCapabilitiesInfo == null) {
			final ArrayList<PrinterId> printerIds = new ArrayList<>();
			printerIds.add(printerId);
			removePrinters(printerIds);
		} else {
			List<PrinterInfo> printers = new ArrayList<>();
			for (PrinterInfo printer : getPrinters()) {
				if (printer.getId().equals(printerId)) {
					PrinterInfo printerWithCaps = new PrinterInfo.Builder(printerId, printer.getName(), PrinterInfo.STATUS_IDLE)
							.setCapabilities(printerCapabilitiesInfo)
							.build();
					printers.add(printerWithCaps);
				} else {
					printers.add(printer);
				}
			}
			addPrinters(printers);
		}
	}

	/**
	 * Ran in background thread. Will do an mDNS scan of local printers
	 *
	 * @return The list of printers as {@link PrinterRec}
	 */
	private List<PrinterRec> scanPrinters() {
		PrinterResult result = new MdnsServices().scan();
		//TODO: check for errors
		return result.getPrinters();
	}

	@Override
	public void onStopPrinterDiscovery() {
		//TODO
	}

	@Override
	public void onValidatePrinters(List<PrinterId> printerIds) {
		//TODO?
	}

	/**
	 * Called when the framework wants additional information about a printer: is it available? what are its capabilities? etc
	 *
	 * @param printerId The printer to check
	 */
	@Override
	public void onStartPrinterStateTracking(final PrinterId printerId) {
		new AsyncTask<Void, Void, PrinterCapabilitiesInfo>() {
			@Override
			protected PrinterCapabilitiesInfo doInBackground(Void... voids) {
				try {
					Log.i(CupsPrintApp.LOG_TAG, "Checking printer status: " + printerId);
					return checkPrinter(printerId.getLocalId(), printerId);
				} catch (Exception e) {
					Log.e(CupsPrintApp.LOG_TAG, "Failed to check printer " + printerId + ": " + e);
				}
				return null;
			}

			@Override
			protected void onPostExecute(PrinterCapabilitiesInfo printerCapabilitiesInfo) {
				onPrinterChecked(printerId, printerCapabilitiesInfo);
			}
		}.execute();
	}

	@Override
	public void onStopPrinterStateTracking(PrinterId printerId) {
		// TODO?
	}

	@Override
	public void onDestroy() {
	}
}
