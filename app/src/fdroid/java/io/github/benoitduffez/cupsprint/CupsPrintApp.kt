package io.github.benoitduffez.cupsprint

import android.app.Application
import org.koin.android.ext.android.startKoin
import org.koin.dsl.module.module

val applicationModule = module {
    single { AppExecutors() }
}

class CupsPrintApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin(this, listOf(applicationModule))
    }
}
