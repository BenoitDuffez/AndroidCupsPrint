package io.github.benoitduffez.cupsprint.detect

import java.util.ArrayList
import java.util.Collections

class PrinterResult internal constructor() {
    var printers: List<PrinterRec>? = null
        private set

    private val errors: List<String>

    init {
        printers = Collections.synchronizedList(ArrayList())
        errors = Collections.synchronizedList(ArrayList())
    }

    internal fun setPrinterRecs(printerRecs: ArrayList<PrinterRec>) {
        printers = printerRecs
    }
}
