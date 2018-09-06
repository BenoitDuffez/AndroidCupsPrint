package io.github.benoitduffez.cupsprint.detect

import java.util.ArrayList
import java.util.Collections

import io.github.benoitduffez.cupsprint.L

internal class Merger {
    fun merge(httpRecs: List<PrinterRec>, httpsRecs: MutableList<PrinterRec>) {
        val tmpRecs = ArrayList<PrinterRec>()
        for (httpRec in httpRecs) {
            var match = false
            for (httpsRec in httpsRecs) {
                try {
                    if (httpRec.queue == httpsRec.queue &&
                            httpRec.host == httpsRec.host &&
                            httpRec.port == httpsRec.port) {
                        match = true
                        break
                    }
                } catch (e: Exception) {
                    L.e("Invalid record in merge", e)
                }
            }
            if (!match) {
                tmpRecs.add(httpRec)
            }
        }
        for (rec in tmpRecs) {
            httpsRecs.add(rec)
        }
        httpsRecs.sort()
    }
}
