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

package com.jonbanjo.cupsprint;

import android.os.AsyncTask;
import android.print.PrintAttributes;
import android.print.PrinterCapabilitiesInfo;
import android.print.PrinterId;
import android.print.PrinterInfo;
import android.printservice.PrintService;
import android.printservice.PrinterDiscoverySession;
import android.support.annotation.Nullable;
import android.util.Log;

import com.jonbanjo.detect.MdnsServices;
import com.jonbanjo.detect.PrinterRec;
import com.jonbanjo.detect.PrinterResult;
import com.jonbanjo.detect.ProgressUpdater;

import org.apache.commons.validator.UrlValidator;
import org.cups4j.CupsClient;
import org.cups4j.CupsPrinter;
import org.cups4j.operations.ipp.IppGetPrinterAttributesOperation;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import ch.ethz.vppserver.ippclient.IppResult;
import ch.ethz.vppserver.schema.ippclient.Attribute;
import ch.ethz.vppserver.schema.ippclient.AttributeGroup;
import ch.ethz.vppserver.schema.ippclient.AttributeValue;

/**
 * CUPS printer discovery class
 */
class CupsPrinterDiscoverySession extends PrinterDiscoverySession {
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
		Log.d("CUPS", "onPrintersDiscovered(" + printers + ")");
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
		String[] schemes = {"http", "https"};
		UrlValidator validator = new UrlValidator(schemes);
		if (!(validator.isValid(url))) {
			return null;
		}
		URL printerURL = new URL(url);

		String clientUrl = url.substring(0, url.substring(0, url.lastIndexOf('/')).lastIndexOf('/'));
		URL clientURL = new URL(clientUrl);

		CupsClient client = new CupsClient(clientURL);
		CupsPrinter testPrinter = client.getPrinter(printerURL);
		if (testPrinter == null) {
			Log.d("CUPS", "Printer not found");
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
		PrinterResult result = new MdnsServices(new ProgressUpdater() {
			@Override
			public void DoUpdate(int Value) {
			}
		}).scan();
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
					return checkPrinter(printerId.getLocalId(), printerId);
				} catch (Exception e) {
					Log.d("CUPS", "Failed to check printer " + printerId + ": " + e);
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

	/**
	 * Misc util methods
	 */
	private static class CupsPrinterDiscoveryUtils {
		/**
		 * Compute the resolution from the {@link AttributeValue}
		 *
		 * @param id             resolution ID (nullable)
		 * @param attributeValue attribute (resolution) value
		 * @return resolution parsed into a {@link android.print.PrintAttributes.Resolution}
		 */
		private static PrintAttributes.Resolution getResolutionFromAttributeValue(@Nullable String id, AttributeValue attributeValue) {
			String[] resolution = attributeValue.getValue().split(",");
			int horizontal, vertical;
			horizontal = Integer.parseInt(resolution[0]);
			vertical = Integer.parseInt(resolution[1]);
			return new PrintAttributes.Resolution(id, String.format(Locale.ENGLISH, "%dx%d dpi", horizontal, vertical), horizontal, vertical);
		}

		/**
		 * Compute the media size from the {@link AttributeValue}
		 *
		 * @param attributeValue attribute (media size) value
		 * @return media size parsed into a {@link android.print.PrintAttributes.MediaSize}
		 */
		private static PrintAttributes.MediaSize getMediaSizeFromAttributeValue(AttributeValue attributeValue) {
			String value = attributeValue.getValue();
			if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_A0".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.ISO_A0;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_A1".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.ISO_A1;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_A10".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.ISO_A10;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_A2".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.ISO_A2;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_A3".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.ISO_A3;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_A4".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.ISO_A4;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_A5".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.ISO_A5;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_A6".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.ISO_A6;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_A7".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.ISO_A7;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_A8".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.ISO_A8;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_A9".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.ISO_A9;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_B0".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.ISO_B0;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_B1".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.ISO_B1;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_B10".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.ISO_B10;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_B2".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.ISO_B2;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_B3".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.ISO_B3;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_B4".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.ISO_B4;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_B5".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.ISO_B5;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_B6".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.ISO_B6;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_B7".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.ISO_B7;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_B8".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.ISO_B8;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_B9".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.ISO_B9;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_C0".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.ISO_C0;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_C1".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.ISO_C1;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_C10".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.ISO_C10;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_C2".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.ISO_C2;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_C3".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.ISO_C3;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_C4".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.ISO_C4;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_C5".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.ISO_C5;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_C6".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.ISO_C6;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_C7".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.ISO_C7;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_C8".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.ISO_C8;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_C9".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.ISO_C9;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JIS_B0".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.JIS_B0;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JIS_B1".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.JIS_B1;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JIS_B10".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.JIS_B10;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JIS_B2".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.JIS_B2;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JIS_B3".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.JIS_B3;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JIS_B4".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.JIS_B4;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JIS_B5".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.JIS_B5;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JIS_B6".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.JIS_B6;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JIS_B7".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.JIS_B7;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JIS_B8".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.JIS_B8;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JIS_B9".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.JIS_B9;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JIS_EXEC".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.JIS_EXEC;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JPN_CHOU2".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.JPN_CHOU2;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JPN_CHOU3".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.JPN_CHOU3;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JPN_CHOU4".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.JPN_CHOU4;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JPN_HAGAKI".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.JPN_HAGAKI;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JPN_KAHU".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.JPN_KAHU;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JPN_KAKU2".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.JPN_KAKU2;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JPN_OUFUKU".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.JPN_OUFUKU;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JPN_YOU4".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.JPN_YOU4;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("NA_FOOLSCAP".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.NA_FOOLSCAP;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("NA_GOVT_LETTER".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.NA_GOVT_LETTER;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("NA_INDEX_3X5".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.NA_INDEX_3X5;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("NA_INDEX_4X6".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.NA_INDEX_4X6;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("NA_INDEX_5X8".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.NA_INDEX_5X8;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("NA_JUNIOR_LEGAL".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.NA_JUNIOR_LEGAL;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("NA_LEDGER".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.NA_LEDGER;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("NA_LEGAL".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.NA_LEGAL;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("NA_LETTER".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.NA_LETTER;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("NA_MONARCH".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.NA_MONARCH;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("NA_QUARTO".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.NA_QUARTO;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("NA_TABLOID".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.NA_TABLOID;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("OM_DAI_PA_KAI".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.OM_DAI_PA_KAI;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("OM_JUURO_KU_KAI".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.OM_JUURO_KU_KAI;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("OM_PA_KAI".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.OM_PA_KAI;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("PRC_1".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.PRC_1;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("PRC_10".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.PRC_10;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("PRC_16K".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.PRC_16K;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("PRC_2".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.PRC_2;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("PRC_3".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.PRC_3;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("PRC_4".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.PRC_4;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("PRC_5".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.PRC_5;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("PRC_6".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.PRC_6;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("PRC_7".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.PRC_7;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("PRC_8".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.PRC_8;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("PRC_9".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.PRC_9;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ROC_16K".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.ROC_16K;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ROC_8K".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.ROC_8K;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("UNKNOWN_LANDSCAPE".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.UNKNOWN_LANDSCAPE;
			} else if (value.toLowerCase(Locale.ENGLISH).startsWith("UNKNOWN_PORTRAIT".toLowerCase(Locale.ENGLISH))) {
				return PrintAttributes.MediaSize.UNKNOWN_PORTRAIT;
			} else {
				return null;
			}
		}
	}
}
