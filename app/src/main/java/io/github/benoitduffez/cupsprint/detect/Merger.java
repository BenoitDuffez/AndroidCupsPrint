package io.github.benoitduffez.cupsprint.detect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.benoitduffez.cupsprint.L;

class Merger {
    void merge(List<PrinterRec> httpRecs, List<PrinterRec> httpsRecs) {
        List<PrinterRec> tmpRecs = new ArrayList<>();
        for (PrinterRec httpRec : httpRecs) {
            boolean match = false;
            for (PrinterRec httpsRec : httpsRecs) {
                try {
                    if (httpRec.getQueue().equals(httpsRec.getQueue()) &&
                            httpRec.getHost().equals(httpsRec.getHost()) &&
                            httpRec.getPort() == httpsRec.getPort()) {
                        match = true;
                        break;
                    }
                } catch (Exception e) {
                    L.e("Invalid record in merge", e);
                }
            }
            if (!match) {
                tmpRecs.add(httpRec);
            }
        }
        for (PrinterRec rec : tmpRecs) {
            httpsRecs.add(rec);
        }
        Collections.sort(httpsRecs);
    }
}
