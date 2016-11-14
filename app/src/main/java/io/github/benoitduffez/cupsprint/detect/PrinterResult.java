package io.github.benoitduffez.cupsprint.detect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PrinterResult {
    private List<PrinterRec> printerRecs;

    private List<String> errors;

    PrinterResult() {
        printerRecs = Collections.synchronizedList(new ArrayList<PrinterRec>());
        errors = Collections.synchronizedList(new ArrayList<String>());
    }

    public List<PrinterRec> getPrinters() {
        return printerRecs;
    }

    public List<String> getErrors() {
        return errors;
    }

    void setPrinterRecs(ArrayList<PrinterRec> printerRecs) {
        this.printerRecs = printerRecs;
    }
}
