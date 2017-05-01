package io.github.benoitduffez.cupsprint.printservice;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.print.PrintAttributes;
import android.print.PrinterCapabilitiesInfo;
import android.print.PrinterId;
import android.print.PrinterInfo;
import android.printservice.PrintService;
import android.printservice.PrinterDiscoverySession;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import org.cups4j.CupsClient;
import org.cups4j.CupsPrinter;
import org.cups4j.operations.ipp.IppGetPrinterAttributesOperation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPeerUnverifiedException;

import ch.ethz.vppserver.ippclient.IppResult;
import ch.ethz.vppserver.schema.ippclient.Attribute;
import ch.ethz.vppserver.schema.ippclient.AttributeGroup;
import ch.ethz.vppserver.schema.ippclient.AttributeValue;
import io.github.benoitduffez.cupsprint.CupsPrintApp;
import io.github.benoitduffez.cupsprint.L;
import io.github.benoitduffez.cupsprint.R;
import io.github.benoitduffez.cupsprint.app.AddPrintersActivity;
import io.github.benoitduffez.cupsprint.app.BasicAuthActivity;
import io.github.benoitduffez.cupsprint.app.HostNotVerifiedActivity;
import io.github.benoitduffez.cupsprint.app.UntrustedCertActivity;
import io.github.benoitduffez.cupsprint.detect.MdnsServices;
import io.github.benoitduffez.cupsprint.detect.PrinterRec;
import io.github.benoitduffez.cupsprint.detect.PrinterResult;

/**
 * CUPS printer discovery class
 */
class CupsPrinterDiscoverySession extends PrinterDiscoverySession {
    private static final int HTTP_UPGRADE_REQUIRED = 426;

    private static final double MM_IN_MILS = 39.3700787;

    private static final String[] REQUIRED_ATTRIBUTES = {
            "media-default",
            "media-supported",
            "printer-resolution-default",
            "printer-resolution-supported",
            "print-color-mode-default",
            "print-color-mode-supported",
            "media-left-margin-supported",
            "media-bottom-right-supported",
            "media-top-margin-supported",
            "media-bottom-margin-supported"
    };

    private final PrintService mPrintService;

    int mResponseCode;

    private X509Certificate[] mServerCerts; // If the server sends a non-trusted cert, it will be stored here

    private String mUnverifiedHost; // If the SSL hostname cannot be verified, this will be the hostname

    CupsPrinterDiscoverySession(PrintService context) {
        mPrintService = context;
    }

    /**
     * Called when the framework wants to find/discover printers
     * Will prompt the user to trust any (the last) host that raises an {@link SSLPeerUnverifiedException}
     *
     * @param priorityList The list of printers that the user selected sometime in the past, that need to be checked first
     */
    @Override
    public void onStartPrinterDiscovery(@NonNull List<PrinterId> priorityList) {
        new AsyncTask<Void, Void, Map<String, String>>() {
            @Override
            protected Map<String, String> doInBackground(Void... params) {
                return scanPrinters();
            }

            @Override
            protected void onPostExecute(Map<String, String> printers) {
                onPrintersDiscovered(printers);
            }
        }.execute();
    }

    /**
     * Called when mDNS/manual printers are found
     * Called on the UI thread
     *
     * @param printers The list of printers found, as a map of URL=>name
     */
    void onPrintersDiscovered(@NonNull Map<String, String> printers) {
        final Resources res = CupsPrintApp.getInstance().getResources();
        final String toast = res.getQuantityString(R.plurals.printer_discovery_result, printers.size(), printers.size());
        Toast.makeText(mPrintService, toast, Toast.LENGTH_SHORT).show();
        L.d("onPrintersDiscovered(" + printers + ")");
        List<PrinterInfo> printersInfo = new ArrayList<>(printers.size());
        for (String url : printers.keySet()) {
            final PrinterId printerId = mPrintService.generatePrinterId(url);
            printersInfo.add(new PrinterInfo.Builder(printerId, printers.get(url), PrinterInfo.STATUS_IDLE).build());
        }

        addPrinters(printersInfo);
    }

    /**
     * Ran in the background thread, will check whether a printer is valid
     *
     * @return The printer capabilities if the printer is available, null otherwise
     */
    @Nullable
    PrinterCapabilitiesInfo checkPrinter(final String url, final PrinterId printerId) throws Exception {
        if (url == null || (!url.startsWith("http://") && !url.startsWith("https://"))) {
            return null;
        }
        URL printerURL = new URL(url);

        URI tmpUri = new URI(url);
        String schemeHostPort = tmpUri.getScheme() + "://" + tmpUri.getHost() + ":" + tmpUri.getPort();
        URL clientURL = new URL(schemeHostPort);

        // Most servers have URLs like xxx://ip:port/printers/printer_name; however some may have xxx://ip:port/printer_name (see GitHub issue #40)
        String path = null;
        if (url.length() > schemeHostPort.length() + 1) {
            path = url.substring(schemeHostPort.length() + 1);
            int pos = path.indexOf('/');
            if (pos > 0) {
                path = path.substring(0, pos);
            }
        }

        CupsClient client = new CupsClient(clientURL).setPath(path);
        CupsPrinter testPrinter;

        // Check if we need to save the server certs if we don't trust the connection
        try {
            testPrinter = client.getPrinter(printerURL);
        } catch (SSLException | CertificateException e) {
            mServerCerts = client.getServerCerts();
            mUnverifiedHost = client.getHost();
            throw e;
        } catch (FileNotFoundException e) { // this one is returned whenever we get a 4xx HTTP response code
            mResponseCode = client.getLastResponseCode(); // it might be an HTTP 401!
            throw e;
        }

        if (testPrinter == null) {
            L.e("Printer not responding. Printer on fire?");
        } else {
            HashMap<String, String> propertyMap = new HashMap<>();
            propertyMap.put("requested-attributes", TextUtils.join(" ", REQUIRED_ATTRIBUTES));

            IppGetPrinterAttributesOperation op = new IppGetPrinterAttributesOperation();
            PrinterCapabilitiesInfo.Builder builder = new PrinterCapabilitiesInfo.Builder(printerId);
            IppResult ippAttributes = op.request(printerURL, propertyMap);
            if (ippAttributes == null) {
                L.e("Couldn't get 'requested-attributes' from printer: " + url);
                return null;
            }

            int colorDefault = 0;
            int colorMode = 0;
            int marginMilsTop = 0, marginMilsRight = 0, marginMilsBottom = 0, marginMilsLeft = 0;
            final List<AttributeGroup> attributes = ippAttributes.getAttributeGroupList();
            if (attributes == null) {
                L.e("Couldn't get attributes list from printer: " + url);
                return null;
            }

            boolean mediaSizeSet = false;
            boolean resolutionSet = false;
            for (AttributeGroup attributeGroup : attributes) {
                for (Attribute attribute : attributeGroup.getAttribute()) {
                    if ("media-default".equals(attribute.getName())) {
                        final PrintAttributes.MediaSize mediaSize = CupsPrinterDiscoveryUtils.getMediaSizeFromAttributeValue(attribute.getAttributeValue().get(0));
                        if (mediaSize != null) {
                            mediaSizeSet = true;
                            builder.addMediaSize(mediaSize, true);
                        }
                    } else if ("media-supported".equals(attribute.getName())) {
                        for (AttributeValue attributeValue : attribute.getAttributeValue()) {
                            final PrintAttributes.MediaSize mediaSize = CupsPrinterDiscoveryUtils.getMediaSizeFromAttributeValue(attributeValue);
                            if (mediaSize != null) {
                                mediaSizeSet = true;
                                builder.addMediaSize(mediaSize, false);
                            }
                        }
                    } else if ("printer-resolution-default".equals(attribute.getName())) {
                        resolutionSet = true;
                        builder.addResolution(CupsPrinterDiscoveryUtils.getResolutionFromAttributeValue("0", attribute.getAttributeValue().get(0)), true);
                    } else if ("printer-resolution-supported".equals(attribute.getName())) {
                        for (AttributeValue attributeValue : attribute.getAttributeValue()) {
                            resolutionSet = true;
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
                        AttributeValue attributeValue = null;
                        if (!attribute.getAttributeValue().isEmpty()) {
                            attributeValue = attribute.getAttributeValue().get(0);
                        }
                        if (attributeValue != null && "color".equals(attributeValue.getValue())) {
                            colorDefault = PrintAttributes.COLOR_MODE_COLOR;
                        } else {
                            colorDefault = PrintAttributes.COLOR_MODE_MONOCHROME;
                        }
                    } else if ("media-left-margin-supported".equals(attribute.getName())) {
                        marginMilsLeft = determineMarginFromAttribute(attribute);
                    } else if ("media-right-margin-supported".equals(attribute.getName())) {
                        marginMilsRight = determineMarginFromAttribute(attribute);
                    } else if ("media-top-margin-supported".equals(attribute.getName())) {
                        marginMilsTop = determineMarginFromAttribute(attribute);
                    } else if ("media-bottom-margin-supported".equals(attribute.getName())) {
                        marginMilsBottom = determineMarginFromAttribute(attribute);
                    }
                }
            }

            if (!mediaSizeSet) {
                builder.addMediaSize(PrintAttributes.MediaSize.ISO_A4, true);
            }

            if (!resolutionSet) {
                builder.addResolution(new PrintAttributes.Resolution("0", "300x300 dpi", 300, 300), true);
            }

            // Workaround for KitKat (SDK 19)
            // see: https://developer.android.com/reference/android/print/PrinterCapabilitiesInfo.Builder.html
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT && colorMode == PrintAttributes.COLOR_MODE_MONOCHROME) {
                colorMode = PrintAttributes.COLOR_MODE_MONOCHROME | PrintAttributes.COLOR_MODE_COLOR;
                L.w("Workaround for Kitkat enabled.");
            }

            // May happen. Fallback to monochrome by default
            if ((colorMode & (PrintAttributes.COLOR_MODE_MONOCHROME | PrintAttributes.COLOR_MODE_COLOR)) == 0) {
                colorMode = PrintAttributes.COLOR_MODE_MONOCHROME;
            }

            // May happen. Fallback to monochrome by default
            if ((colorDefault & (PrintAttributes.COLOR_MODE_MONOCHROME | PrintAttributes.COLOR_MODE_COLOR)) == 0) {
                colorDefault = PrintAttributes.COLOR_MODE_MONOCHROME;
            }

            builder.setColorModes(colorMode, colorDefault);
            builder.setMinMargins(new PrintAttributes.Margins(marginMilsLeft, marginMilsTop, marginMilsRight, marginMilsBottom));
            return builder.build();
        }
        return null;
    }

    private int determineMarginFromAttribute(Attribute attribute) {
        List<AttributeValue> values = attribute.getAttributeValue();
        if (values.isEmpty()) {
            return 0;
        }

        int margin = Integer.MAX_VALUE;
        for (AttributeValue value : attribute.getAttributeValue()) {
            int valueMargin = (int) (MM_IN_MILS * Integer.parseInt(value.getValue()) / 100);
            margin = Math.min(margin, valueMargin);
        }
        return margin;
    }

    /**
     * Called when the printer has been checked over IPP(S)
     * Called from the UI thread
     *
     * @param printerId               The printer
     * @param printerCapabilitiesInfo null if the printer isn't available anymore, otherwise contains the printer capabilities
     */
    void onPrinterChecked(PrinterId printerId, PrinterCapabilitiesInfo printerCapabilitiesInfo) {
        L.d("onPrinterChecked: " + printerId + " (printers: " + getPrinters() + "), cap: " + printerCapabilitiesInfo);
        if (printerCapabilitiesInfo == null) {
            final ArrayList<PrinterId> printerIds = new ArrayList<>();
            printerIds.add(printerId);
            removePrinters(printerIds);
            Toast.makeText(mPrintService, mPrintService.getString(R.string.printer_not_responding, printerId.getLocalId()), Toast.LENGTH_LONG).show();
            L.d("onPrinterChecked: Printer has no cap, removing it from the list");
        } else {
            List<PrinterInfo> printers = new ArrayList<>();
            for (PrinterInfo printer : getPrinters()) {
                if (printer.getId().equals(printerId)) {
                    PrinterInfo printerWithCaps = new PrinterInfo.Builder(printerId, printer.getName(), PrinterInfo.STATUS_IDLE)
                            .setCapabilities(printerCapabilitiesInfo)
                            .build();
                    L.d("onPrinterChecked: adding printer: " + printerWithCaps);
                    printers.add(printerWithCaps);
                } else {
                    printers.add(printer);
                }
            }
            L.d("onPrinterChecked: we had " + getPrinters().size() + "printers, we now have " + printers.size());
            addPrinters(printers);
        }
    }

    /**
     * Ran in background thread. Will do an mDNS scan of local printers
     *
     * @return The list of printers as {@link PrinterRec}
     */
    @NonNull
    Map<String, String> scanPrinters() {
        final MdnsServices mdns = new MdnsServices();
        PrinterResult result = mdns.scan();

        //TODO: check for errors
        Map<String, String> printers = new HashMap<>();
        String url, name;

        // Add the printers found by mDNS
        for (PrinterRec rec : result.getPrinters()) {
            url = rec.getProtocol() + "://" + rec.getHost() + ":" + rec.getPort() + "/printers/" + rec.getQueue();
            printers.put(url, rec.getNickname());
        }

        // Add the printers manually added
        final SharedPreferences prefs = mPrintService.getSharedPreferences(AddPrintersActivity.SHARED_PREFS_MANUAL_PRINTERS, Context.MODE_PRIVATE);
        final int numPrinters = prefs.getInt(AddPrintersActivity.PREF_NUM_PRINTERS, 0);
        for (int i = 0; i < numPrinters; i++) {
            url = prefs.getString(AddPrintersActivity.PREF_URL + i, null);
            name = prefs.getString(AddPrintersActivity.PREF_NAME + i, null);
            if (url != null && name != null && url.trim().length() > 0 && name.trim().length() > 0) {
                // Ensure a port is set, and set it to 631 if unset
                try {
                    URI uri = new URI(url);
                    if (uri.getPort() < 0) {
                        url = uri.getScheme() + "://" + uri.getHost() + ":" + 631;
                    } else {
                        url = uri.getScheme() + "://" + uri.getHost() + ":" + uri.getPort();
                    }
                    if (uri.getPath() != null) {
                        url += uri.getPath();
                    }

                    // Now, add printer
                    printers.put(url, name);
                } catch (URISyntaxException e) {
                    L.e("Unable to parse manually-entered URI: " + url, e);
                }
            }
        }

        return printers;
    }

    @Override
    public void onStopPrinterDiscovery() {
        //TODO
    }

    @Override
    public void onValidatePrinters(@NonNull List<PrinterId> printerIds) {
        //TODO?
    }

    /**
     * Called when the framework wants additional information about a printer: is it available? what are its capabilities? etc
     *
     * @param printerId The printer to check
     */
    @Override
    public void onStartPrinterStateTracking(@NonNull final PrinterId printerId) {
        L.d("onStartPrinterStateTracking: " + printerId);
        new AsyncTask<Void, Void, PrinterCapabilitiesInfo>() {
            Exception mException;

            @Override
            protected PrinterCapabilitiesInfo doInBackground(Void... voids) {
                try {
                    L.i("Checking printer status: " + printerId);
                    return checkPrinter(printerId.getLocalId(), printerId);
                } catch (Exception e) {
                    mException = e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(PrinterCapabilitiesInfo printerCapabilitiesInfo) {
                L.v("HTTP response code: " + mResponseCode);
                if (mException != null) {
                    if (handlePrinterException(mException, printerId)) {
                        L.e("Couldn't start printer state tracking", mException);
                    }
                } else {
                    onPrinterChecked(printerId, printerCapabilitiesInfo);
                }
            }
        }.execute();
    }

    /**
     * Run on the UI thread. Present the user some information about the error that happened during the printer check
     *
     * @param exception The exception that occurred
     * @param printerId The printer on which the exception occurred
     * @return true if the exception should be reported for a potential bug, false otherwise
     */
    boolean handlePrinterException(@NonNull Exception exception, @NonNull PrinterId printerId) {
        // Happens when the HTTP response code is in the 4xx range
        if (exception instanceof FileNotFoundException) {
            return handleHttpError(exception, printerId);
        } else if (exception instanceof SSLPeerUnverifiedException
                || (exception instanceof IOException && exception.getMessage() != null && exception.getMessage().contains("not verified"))) {
            Intent dialog = new Intent(mPrintService, HostNotVerifiedActivity.class);
            dialog.putExtra(HostNotVerifiedActivity.KEY_HOST, mUnverifiedHost);
            dialog.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mPrintService.startActivity(dialog);
        } else if (exception instanceof SSLException && mServerCerts != null) {
            Intent dialog = new Intent(mPrintService, UntrustedCertActivity.class);
            dialog.putExtra(UntrustedCertActivity.KEY_CERT, mServerCerts[0]);
            dialog.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mPrintService.startActivity(dialog);
        } else if (exception instanceof SocketTimeoutException) {
            Toast.makeText(mPrintService, R.string.err_printer_socket_timeout, Toast.LENGTH_LONG).show();
        } else if (exception instanceof UnknownHostException) {
            Toast.makeText(mPrintService, R.string.err_printer_unknown_host, Toast.LENGTH_LONG).show();
        } else if (exception instanceof ConnectException && exception.getLocalizedMessage().contains("ENETUNREACH")) {
            Toast.makeText(mPrintService, R.string.err_printer_network_unreachable, Toast.LENGTH_LONG).show();
        } else {
            return handleHttpError(exception, printerId);
        }
        return false;
    }

    /**
     * Run on the UI thread. Handle all errors related to HTTP errors (usually in the 4xx range)
     *
     * @param exception The exception that occurred
     * @param printerId The printer on which the exception occurred
     * @return true if the exception should be reported for a potential bug, false otherwise
     */
    private boolean handleHttpError(@NonNull Exception exception, @NonNull PrinterId printerId) {
        switch (mResponseCode) {
            // happens when basic auth is required but not sent
            case HttpURLConnection.HTTP_NOT_FOUND:
                Toast.makeText(mPrintService, R.string.err_404, Toast.LENGTH_LONG).show();
                break;

            case HttpURLConnection.HTTP_BAD_REQUEST:
                Toast.makeText(mPrintService, R.string.err_400, Toast.LENGTH_LONG).show();
                break;

            case HttpURLConnection.HTTP_UNAUTHORIZED:
                try {
                    final URI printerUri = new URI(printerId.getLocalId());
                    String printersUrl = printerUri.getScheme() + "://" + printerUri.getHost() + ":" + printerUri.getPort() + "/printers/";
                    Intent dialog = new Intent(mPrintService, BasicAuthActivity.class);
                    dialog.putExtra(BasicAuthActivity.KEY_BASIC_AUTH_PRINTERS_URL, printersUrl);
                    dialog.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mPrintService.startActivity(dialog);
                } catch (URISyntaxException e) {
                    L.e("Couldn't parse URI: " + printerId.getLocalId(), e);
                    return true;
                }
                break;

            // 426 Upgrade Required (plus header: Upgrade: TLS/1.2,TLS/1.1,TLS/1.0) which means please use HTTPS
            case HTTP_UPGRADE_REQUIRED:
                // remove this printer from the list because it will refuse to print anything over HTTP
                Toast.makeText(mPrintService, R.string.err_http_upgrade, Toast.LENGTH_LONG).show();
                List<PrinterId> remove = new ArrayList<>(1);
                remove.add(printerId);
                removePrinters(remove);
                break;

            default:
                Toast.makeText(mPrintService, exception.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                return true;
        }
        return false;
    }

    @Override
    public void onStopPrinterStateTracking(@NonNull PrinterId printerId) {
        // TODO?
    }

    @Override
    public void onDestroy() {
    }
}
