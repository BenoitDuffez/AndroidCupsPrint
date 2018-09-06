package io.github.benoitduffez.cupsprint

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import io.fabric.sdk.android.Fabric
import org.koin.android.ext.android.startKoin
import org.koin.dsl.module.module
import timber.log.Timber

val applicationModule = module {
    single { AppExecutors() }
}

class CupsPrintApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val core = CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()
        Fabric.with(this, Crashlytics.Builder().core(core).build())

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashReportingTree())
        }

        startKoin(this, listOf(applicationModule))
    }

    /** A tree which logs important information for crash reporting.  */
    private class CrashReportingTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            Crashlytics.log(priority, tag, message)
            t.let { Crashlytics.logException(it) }
        }
    }
}
