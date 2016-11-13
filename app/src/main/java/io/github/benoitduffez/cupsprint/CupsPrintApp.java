package io.github.benoitduffez.cupsprint;

import android.app.Application;
import android.content.Context;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

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
        Fabric.with(this, new Crashlytics());
    }
}
