package com.jonbanjo.detect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PrinterResult {
	
	List<PrinterRec> printerRecs;
	List<String> errors;
	
	public PrinterResult(){
		printerRecs = Collections.synchronizedList(new ArrayList<PrinterRec>());
		errors = Collections.synchronizedList(new ArrayList<String>());
	}

	public List<PrinterRec> getPrinters(){
		return printerRecs;
	}
	
	public List<String> getErrors(){
		return errors;
	}
}
