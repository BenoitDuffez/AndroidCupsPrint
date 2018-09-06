package io.github.benoitduffez.cupsprint

import android.app.Application
import android.content.Context

import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore

import io.fabric.sdk.android.Fabric

class CupsPrintApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        val core = CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()
        Fabric.with(this, Crashlytics.Builder().core(core).build())
    }

    companion object {
        val LOG_TAG = "CUPS"

        var instance: CupsPrintApp? = null
            private set

        val context: Context
            get() = instance!!.applicationContext
    }
}
