package io.github.benoitduffez.cupsprint.printservice

import android.content.Context
import android.content.Intent
import android.os.Build
import android.print.PrintAttributes
import android.print.PrinterCapabilitiesInfo
import android.print.PrinterId
import android.print.PrinterInfo
import android.printservice.PrintService
import android.printservice.PrinterDiscoverySession
import android.text.TextUtils
import android.widget.Toast
import ch.ethz.vppserver.schema.ippclient.Attribute
import ch.ethz.vppserver.schema.ippclient.AttributeValue
import io.github.benoitduffez.cupsprint.AppExecutors
import io.github.benoitduffez.cupsprint.R
import io.github.benoitduffez.cupsprint.app.AddPrintersActivity
import io.github.benoitduffez.cupsprint.app.BasicAuthActivity
import io.github.benoitduffez.cupsprint.app.HostNotVerifiedActivity
import io.github.benoitduffez.cupsprint.app.UntrustedCertActivity
import io.github.benoitduffez.cupsprint.detect.MdnsServices
import io.github.benoitduffez.cupsprint.detect.PrinterRec
import org.cups4j.CupsClient
import org.cups4j.CupsPrinter
import org.cups4j.operations.IppOperation
import org.cups4j.operations.ipp.IppGetPrinterAttributesOperation
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.io.FileNotFoundException
import java.io.IOException
import java.net.*
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.net.ssl.SSLException
import javax.net.ssl.SSLPeerUnverifiedException

/**
 * CUPS printer discovery class
 */
internal class CupsPrinterDiscoverySession(private val printService: PrintService) : PrinterDiscoverySession() {
    private var responseCode: Int = 0
    private var serverCerts: Array<X509Certificate>? = null // If the server sends a non-trusted cert, it will be stored here
    private var unverifiedHost: String? = null // If the SSL hostname cannot be verified, this will be the hostname
    private val appExecutors: AppExecutors by printService.inject()
    private var ippPrintersStateTracking = ConcurrentHashMap<PrinterId, IppOperation>(3, 0.9f, 4) // Threadsafe access for 4 threads to ippOperation in PrinterStateTracking
    @Volatile private var mdnsPrinterDiscovery: MdnsServices? = null // Threadsafe access to mdnsService at PrinterDiscovery
    @Volatile private var runningPrinterDiscovery: Boolean = false // Threadsafe state of PrinterDiscovery


    /**
     * Called when the framework wants to find/discover printers
     * Will prompt the user to trust any (the last) host that raises an [SSLPeerUnverifiedException]
     *
     * @param priorityList The list of printers that the user selected sometime in the past, that need to be checked first
     */
    override fun onStartPrinterDiscovery(priorityList: List<PrinterId>) {
        // ToDo: Use the priorityList

        appExecutors.networkIO.execute {
            runningPrinterDiscovery = true
            val printers = scanPrinters()
            if (runningPrinterDiscovery) {
                appExecutors.mainThread.execute {
                    onPrintersDiscovered(printers)
                }
            }
            runningPrinterDiscovery = false
        }
    }

    /**
     * Called when mDNS/manual printers are found
     * Called on the UI thread
     *
     * @param printers The list of printers found, as a map of URL=>name
     */
    private fun onPrintersDiscovered(printers: Map<String, String>) {
        Timber.d("onPrintersDiscovered($printers)")

        val res = printService.applicationContext.resources
        val toast = res.getQuantityString(R.plurals.printer_discovery_result, printers.size, printers.size)
        Toast.makeText(printService, toast, Toast.LENGTH_SHORT).show()

        val printersInfo = ArrayList<PrinterInfo>(printers.size)
        for (url in printers.keys) {
            val printerId = printService.generatePrinterId(url)
            printersInfo.add(PrinterInfo.Builder(printerId, printers[url] ?: error("Null printer"), PrinterInfo.STATUS_IDLE).build())
        }

        addPrinters(printersInfo)
    }

    /**
     * Ran in the background thread, will check whether a printer is valid
     *
     * @return The printer capabilities if the printer is available, null otherwise
     */
    @Throws(Exception::class)
    fun checkPrinter(url: String?, printerId: PrinterId): PrinterCapabilitiesInfo? {
        if (url == null || !url.startsWith("http://") && !url.startsWith("https://")) {
            return null
        }
        val printerURL = URL(url)

        val tmpUri = URI(url)
        val schemeHostPort = tmpUri.scheme + "://" + tmpUri.host + ":" + tmpUri.port
        val clientURL = URL(schemeHostPort)

        // Most servers have URLs like xxx://ip:port/printers/printer_name; however some may have xxx://ip:port/printer_name (see GitHub issue #40)
        var path: String? = null
        if (url.length > schemeHostPort.length + 1) {
            path = url.substring(schemeHostPort.length + 1)
            val pos = path.indexOf('/')
            if (pos > 0) {
                path = path.substring(0, pos)
            }
        }

        val client = CupsClient(printService, clientURL).setPath(path ?: "/")
        val testPrinter: CupsPrinter?

        // Check if we need to save the server certs if we don't trust the connection
        try {
            testPrinter = client.getPrinter(printerURL)
        } catch (e: SSLException) {
            serverCerts = client.serverCerts
            unverifiedHost = client.host
            throw e
        } catch (e: CertificateException) {
            serverCerts = client.serverCerts
            unverifiedHost = client.host
            throw e
        } catch (e: FileNotFoundException) { // this one is returned whenever we get a 4xx HTTP response code
            responseCode = client.lastResponseCode // it might be an HTTP 401!
            throw e
        }

        if (testPrinter == null) {
            Timber.e("Printer not responding. Printer on fire?")
        } else {
            val propertyMap = HashMap<String, String>()
            propertyMap["requested-attributes"] = TextUtils.join(" ", REQUIRED_ATTRIBUTES)

            val op = IppGetPrinterAttributesOperation(printService)
            ippPrintersStateTracking[printerId] = op
            val builder = PrinterCapabilitiesInfo.Builder(printerId)
            val ippAttributes = op.request(printerURL, propertyMap)
            if (op.isAborted()){
                return null
            }
            ippPrintersStateTracking.remove(printerId)
            if (ippAttributes == null) {
                Timber.e("Couldn't get 'requested-attributes' from printer: $url")
                return null
            }

            var colorDefault = 0
            var colorMode = 0
            var duplexDefault = 0
            var duplexMode = 0
            var marginMilsTop = 0
            var marginMilsRight = 0
            var marginMilsBottom = 0
            var marginMilsLeft = 0
            val attributes = ippAttributes.attributeGroupList
            if (attributes == null) {
                Timber.e("Couldn't get attributes list from printer: $url")
                return null
            }

            var mediaSizeSet = false
            var resolutionSet = false
            for (attributeGroup in attributes) {
                for (attribute in attributeGroup.attribute) {
                    if ("media-default" == attribute.name) {
                        val mediaSize = if (attribute.attributeValue.size > 0) {
                            CupsPrinterDiscoveryUtils.getMediaSizeFromAttributeValue(attribute.attributeValue[0])
                        } else {
                            PrintAttributes.MediaSize.ISO_A4
                        }
                        if (mediaSize != null) {
                            mediaSizeSet = true
                            builder.addMediaSize(mediaSize, true)
                        }
                    } else if ("media-supported" == attribute.name) {
                        for (attributeValue in attribute.attributeValue) {
                            val mediaSize = CupsPrinterDiscoveryUtils.getMediaSizeFromAttributeValue(attributeValue)
                            if (mediaSize != null) {
                                mediaSizeSet = true
                                builder.addMediaSize(mediaSize, false)
                            }
                        }
                    } else if ("printer-resolution-default" == attribute.name) {
                        resolutionSet = true
                        builder.addResolution(CupsPrinterDiscoveryUtils.getResolutionFromAttributeValue("0", attribute.attributeValue[0]), true)
                    } else if ("printer-resolution-supported" == attribute.name) {
                        for (attributeValue in attribute.attributeValue) {
                            resolutionSet = true
                            builder.addResolution(CupsPrinterDiscoveryUtils.getResolutionFromAttributeValue(attributeValue.tag!!, attributeValue), false)
                        }
                    } else if ("print-color-mode-supported" == attribute.name) {
                        for (attributeValue in attribute.attributeValue) {
                            if ("monochrome" == attributeValue.value) {
                                colorMode = colorMode or PrintAttributes.COLOR_MODE_MONOCHROME
                            } else if ("color" == attributeValue.value) {
                                colorMode = colorMode or PrintAttributes.COLOR_MODE_COLOR
                            }
                        }
                    } else if ("print-color-mode-default" == attribute.name) {
                        var attributeValue: AttributeValue? = null
                        if (attribute.attributeValue.isNotEmpty()) {
                            attributeValue = attribute.attributeValue[0]
                        }
                        colorDefault = when {
                            attributeValue != null && "color" == attributeValue.value -> PrintAttributes.COLOR_MODE_COLOR
                            else -> PrintAttributes.COLOR_MODE_MONOCHROME
                        }
                    } else if ("sides-supported" == attribute.name) {
                        if (Build.VERSION.SDK_INT >= 23) {
                            for (attributeValue in attribute.attributeValue) {
                                val rawValue = attributeValue.value
                                Timber.d("Duplex mode $rawValue")
                                duplexMode = duplexMode or when (attributeValue.value) {
                                    "one-sided" -> PrintAttributes.DUPLEX_MODE_NONE
                                    "two-sided-long-edge" -> PrintAttributes.DUPLEX_MODE_LONG_EDGE
                                    "two-sided-short-edge" -> PrintAttributes.DUPLEX_MODE_SHORT_EDGE
                                    else -> 0
                                }
                            }
                        }
                    } else if ("sides-default" == attribute.name) {
                        if (Build.VERSION.SDK_INT >= 23) {
                            var attributeValue: AttributeValue? = null
                            if (attribute.attributeValue.isNotEmpty())
                                attributeValue = attribute.attributeValue[0]
                            val rawValue = attributeValue?.value
                            Timber.d("Default duplex mode $rawValue")
                            duplexDefault = when (attributeValue?.value) {
                                "two-sided-long-edge" -> PrintAttributes.DUPLEX_MODE_LONG_EDGE
                                "two-sided-short-edge" -> PrintAttributes.DUPLEX_MODE_SHORT_EDGE
                                else -> PrintAttributes.DUPLEX_MODE_NONE
                            }
                        }
                    } else if ("media-left-margin-supported" == attribute.name) {
                        marginMilsLeft = determineMarginFromAttribute(attribute)
                    } else if ("media-right-margin-supported" == attribute.name) {
                        marginMilsRight = determineMarginFromAttribute(attribute)
                    } else if ("media-top-margin-supported" == attribute.name) {
                        marginMilsTop = determineMarginFromAttribute(attribute)
                    } else if ("media-bottom-margin-supported" == attribute.name) {
                        marginMilsBottom = determineMarginFromAttribute(attribute)
                    }
                }
            }

            if (!mediaSizeSet) {
                builder.addMediaSize(PrintAttributes.MediaSize.ISO_A4, true)
            }

            if (!resolutionSet) {
                builder.addResolution(PrintAttributes.Resolution("0", "300x300 dpi", 300, 300), true)
            }

            // Workaround for KitKat (SDK 19)
            // see: https://developer.android.com/reference/android/print/PrinterCapabilitiesInfo.Builder.html
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT && colorMode == PrintAttributes.COLOR_MODE_MONOCHROME) {
                colorMode = PrintAttributes.COLOR_MODE_MONOCHROME or PrintAttributes.COLOR_MODE_COLOR
                Timber.w("Workaround for Kitkat enabled.")
            }

            // May happen. Fallback to monochrome by default
            if (colorMode and (PrintAttributes.COLOR_MODE_MONOCHROME or PrintAttributes.COLOR_MODE_COLOR) == 0) {
                colorMode = PrintAttributes.COLOR_MODE_MONOCHROME
            }

            // May happen. Fallback to monochrome by default
            if (colorDefault and (PrintAttributes.COLOR_MODE_MONOCHROME or PrintAttributes.COLOR_MODE_COLOR) == 0) {
                colorDefault = PrintAttributes.COLOR_MODE_MONOCHROME
            }

            if (Build.VERSION.SDK_INT >= 23) {
                if (duplexMode == 0)
                    colorMode = PrintAttributes.DUPLEX_MODE_NONE

                if (duplexDefault == 0)
                    colorMode = PrintAttributes.DUPLEX_MODE_NONE
            }

            builder.setColorModes(colorMode, colorDefault)
            if (Build.VERSION.SDK_INT >= 23)
                builder.setDuplexModes(duplexMode, duplexDefault)
            builder.setMinMargins(PrintAttributes.Margins(marginMilsLeft, marginMilsTop, marginMilsRight, marginMilsBottom))
            return builder.build()
        }
        return null
    }

    private fun determineMarginFromAttribute(attribute: Attribute): Int {
        val values = attribute.attributeValue
        if (values.isEmpty()) {
            return 0
        }

        var margin = Integer.MAX_VALUE
        for (value in attribute.attributeValue) {
            val valueMargin = (MM_IN_MILS * (value.value?.toInt() ?: 0) / 100).toInt()
            margin = margin.coerceAtMost(valueMargin)
        }
        return margin
    }

    /**
     * Called when the printer has been checked over IPP(S)
     * Called from the UI thread
     *
     * @param printerId               The printer
     * @param printerCapabilitiesInfo null if the printer isn't available anymore, otherwise contains the printer capabilities
     */
    private fun onPrinterChecked(printerId: PrinterId, printerCapabilitiesInfo: PrinterCapabilitiesInfo?) {
        Timber.d("onPrinterChecked: $printerId (printers: $printers), cap: $printerCapabilitiesInfo")
        if (printerCapabilitiesInfo == null) {
            val printerIds = ArrayList<PrinterId>()
            printerIds.add(printerId)
            removePrinters(printerIds)
            Toast.makeText(printService, printService.getString(R.string.printer_not_responding, printerId.localId), Toast.LENGTH_LONG).show()
            Timber.d("onPrinterChecked: Printer has no cap, removing it from the list")
        } else {
            val printers = ArrayList<PrinterInfo>()
            for (printer in getPrinters()) {
                if (printer.id == printerId) {
                    val printerWithCaps = PrinterInfo.Builder(printerId, printer.name, PrinterInfo.STATUS_IDLE)
                            .setCapabilities(printerCapabilitiesInfo)
                            .build()
                    Timber.d("onPrinterChecked: adding printer: $printerWithCaps")
                    printers.add(printerWithCaps)
                } else {
                    printers.add(printer)
                }
            }
            Timber.d("onPrinterChecked: we had ${getPrinters().size}printers, we now have ${printers.size}")
            addPrinters(printers)
        }
    }

    /**
     * Ran in background thread. Will do an mDNS scan of local printers
     *
     * @return The list of printers as [PrinterRec]
     */
    private fun scanPrinters(): Map<String, String> {
        Timber.d("Scanning for printers using mDNS, and add manual printers...")

        val printers = HashMap<String, String>()
        scanMDnsPrinters(printers)
        addManualPrinters(printers)

        return printers
    }

    /**
     * Perform mDNS scan and add valid printers
     */
    private fun scanMDnsPrinters(printers: HashMap<String, String>) {
        val mdns = MdnsServices()
        mdnsPrinterDiscovery = mdns
        val result = mdns.scan()
        mdnsPrinterDiscovery = null
        result.printers?.forEach { rec ->
            val mDnsUrl = rec.protocol + "://" + rec.host + ":" + rec.port + "/printers/" + rec.queue
            printers[mDnsUrl] = rec.nickname
            Timber.d("mDNS scan found printer ${rec.nickname} at URL: $mDnsUrl")
        }
    }

    /**
     * Add the printers that were manually entered from the app launcher activity
     */
    private fun addManualPrinters(printers: HashMap<String, String>) {
        val prefs = printService.getSharedPreferences(AddPrintersActivity.SHARED_PREFS_MANUAL_PRINTERS, Context.MODE_PRIVATE)
        val numPrinters = prefs.getInt(AddPrintersActivity.PREF_NUM_PRINTERS, 0)
        var name: String?
        var url: String?
        for (i in 0 until numPrinters) {
            url = prefs.getString(AddPrintersActivity.PREF_URL + i, null)
            name = prefs.getString(AddPrintersActivity.PREF_NAME + i, null)
            if (url != null && name != null && url.trim { it <= ' ' }.isNotEmpty() && name.trim { it <= ' ' }.isNotEmpty()) {
                // Ensure a port is set, and set it to 631 if unset
                try {
                    val uri = URI(url)
                    val port = if (uri.port < 0) 631 else uri.port
                    url = uri.scheme + "://" + uri.host + ":" + port
                    if (uri.path != null) {
                        url += uri.path
                    }

                    // Now, add printer
                    printers[url] = name
                    Timber.d("Manually added $name at URL: $url")
                } catch (e: URISyntaxException) {
                    Timber.e(e, "Unable to parse manually-entered URI: $url")
                }
            }
        }
    }

    override fun onStopPrinterDiscovery() {
        runningPrinterDiscovery = false
        mdnsPrinterDiscovery?.stop()
    }

    override fun onValidatePrinters(printerIds: List<PrinterId>) {
        //TODO?
    }

    /**
     * Called when the framework wants additional information about a printer: is it available? what are its capabilities? etc
     *
     * @param printerId The printer to check
     */
    override fun onStartPrinterStateTracking(printerId: PrinterId) {
        Timber.d("onStartPrinterStateTracking: $printerId")

        appExecutors.networkIO.execute {
            Timber.i("Checking printer status: $printerId")

            try {
                val printerCapabilitiesInfo = checkPrinter(printerId.localId, printerId)
                if (ippPrintersStateTracking[printerId]?.isAborted() == true) {
                    Timber.v("Checking Printer is aborted")
                } else {
                    Timber.v("HTTP response code: $responseCode")
                    appExecutors.mainThread.execute {
                        onPrinterChecked(printerId, printerCapabilitiesInfo)
                    }
                }
            } catch (e: Exception) {
                appExecutors.mainThread.execute {
                    if (handlePrinterException(e, printerId)) {
                        when {
                            e is MalformedURLException || e is URISyntaxException ||
                                    e.message?.contains("ETIMEDOUT") == true -> Timber.e("Start printer state tracking failed")
                            else -> Timber.e(e, "Start printer state tracking failed")
                        }
                    }
                }
            }
        }
    }

    /**
     * Run on the UI thread. Present the user some information about the error that happened during the printer check
     *
     * @param exception The exception that occurred
     * @param printerId The printer on which the exception occurred
     * @return true if the exception should be reported to Crashlytics, false otherwise
     */
    private fun handlePrinterException(exception: Exception, printerId: PrinterId): Boolean {
        // Happens when the HTTP response code is in the 4xx range
        when {
            exception is FileNotFoundException -> return handleHttpError(exception, printerId)
            exception is SSLPeerUnverifiedException || exception is IOException && exception.message != null && exception.message?.contains("not verified") == true -> {
                val dialog = Intent(printService, HostNotVerifiedActivity::class.java)
                dialog.putExtra(HostNotVerifiedActivity.KEY_HOST, unverifiedHost)
                dialog.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                printService.startActivity(dialog)
            }
            exception is SSLException && serverCerts != null -> {
                val dialog = Intent(printService, UntrustedCertActivity::class.java)
                dialog.putExtra(UntrustedCertActivity.KEY_CERT, serverCerts!![0])
                dialog.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                printService.startActivity(dialog)
            }
            exception is SocketTimeoutException -> Toast.makeText(printService, R.string.err_printer_socket_timeout, Toast.LENGTH_LONG).show()
            exception is UnknownHostException -> Toast.makeText(printService, R.string.err_printer_unknown_host, Toast.LENGTH_LONG).show()
            exception is ConnectException && exception.getLocalizedMessage()?.contains("ENETUNREACH")?:false -> Toast.makeText(printService, R.string.err_printer_network_unreachable, Toast.LENGTH_LONG).show()
            exception is IOException && exception.localizedMessage?.contains("Cleartext HTTP traffic")?:false -> Toast.makeText(printService, R.string.cleartext_error_android_9, Toast.LENGTH_LONG).show()
            else -> return handleHttpError(exception, printerId)
        }
        return false
    }

    /**
     * Run on the UI thread. Handle all errors related to HTTP errors (usually in the 4xx range)
     *
     * @param exception The exception that occurred
     * @param printerId The printer on which the exception occurred
     * @return true if the exception should be reported to Crashlytics, false otherwise
     */
    private fun handleHttpError(exception: Exception, printerId: PrinterId): Boolean {
        when (responseCode) {
            // happens when basic auth is required but not sent
            HttpURLConnection.HTTP_NOT_FOUND -> Toast.makeText(printService, R.string.err_404, Toast.LENGTH_LONG).show()
            HttpURLConnection.HTTP_BAD_REQUEST -> Toast.makeText(printService, R.string.err_400, Toast.LENGTH_LONG).show()
            HttpURLConnection.HTTP_UNAUTHORIZED -> try {
                val printerUri = URI(printerId.localId)
                val printersUrl = printerUri.scheme + "://" + printerUri.host + ":" + printerUri.port + "/printers/"
                val dialog = Intent(printService, BasicAuthActivity::class.java)
                dialog.putExtra(BasicAuthActivity.KEY_BASIC_AUTH_PRINTERS_URL, printersUrl)
                dialog.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                printService.startActivity(dialog)
            } catch (e: URISyntaxException) {
                Timber.e(e, "Couldn't parse URI: ${printerId.localId}")
                return true
            }

            // 426 Upgrade Required (plus header: Upgrade: TLS/1.2,TLS/1.1,TLS/1.0) which means please use HTTPS
            HTTP_UPGRADE_REQUIRED -> {
                // remove this printer from the list because it will refuse to print anything over HTTP
                Toast.makeText(printService, R.string.err_http_upgrade, Toast.LENGTH_LONG).show()
                val remove = ArrayList<PrinterId>(1)
                remove.add(printerId)
                removePrinters(remove)
            }

            else -> {
                Toast.makeText(printService, exception.localizedMessage, Toast.LENGTH_LONG).show()
                return true
            }
        }
        return false
    }

    override fun onStopPrinterStateTracking(printerId: PrinterId) {
        ippPrintersStateTracking[printerId]?.abort()
    }

    override fun onDestroy() {}

    companion object {
        private const val HTTP_UPGRADE_REQUIRED = 426
        private const val MM_IN_MILS = 39.3700787
        private val REQUIRED_ATTRIBUTES = arrayOf(
                "media-default",
                "media-supported",
                "printer-resolution-default",
                "printer-resolution-supported",
                "print-color-mode-default",
                "print-color-mode-supported",
                "sides-supported",
                "sides-default",
                "media-left-margin-supported",
                "media-bottom-right-supported",
                "media-top-margin-supported",
                "media-bottom-margin-supported"
        )
    }
}
