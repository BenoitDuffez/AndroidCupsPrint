package io.github.benoitduffez.cupsprint;

import android.app.Application;
import android.content.Context;

public class CupsPrintApp extends Application {
    public static final String LOG_TAG = "CUPS";

    private static CupsPrintApp instance;

    public static CupsPrintApp getInstance() {
        return instance;
    }

    public static Context getContext() {
        return instance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
