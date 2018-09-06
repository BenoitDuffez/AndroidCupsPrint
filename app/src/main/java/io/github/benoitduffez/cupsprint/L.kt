package io.github.benoitduffez.cupsprint

import android.util.Log

import com.crashlytics.android.Crashlytics

/**
 * Logging/crash reporting functions
 */
object L {
    /**
     * Verbose log + crashlytics log
     *
     * @param msg Log message
     */
    fun v(msg: String) {
        Log.v(CupsPrintApp.LOG_TAG, msg)
        Crashlytics.log("V: $msg")
    }

    /**
     * Info log + crashlytics log
     *
     * @param msg Log message
     */
    fun i(msg: String) {
        Log.i(CupsPrintApp.LOG_TAG, msg)
        Crashlytics.log("I: $msg")
    }

    /**
     * Warning log + crashlytics log
     *
     * @param msg Log message
     */
    fun w(msg: String) {
        Log.w(CupsPrintApp.LOG_TAG, msg)
        Crashlytics.log("W: $msg")
    }

    /**
     * Debug log + crashlytics log
     *
     * @param msg Log message
     */
    fun d(msg: String) {
        Log.d(CupsPrintApp.LOG_TAG, msg)
        Crashlytics.log("D: $msg")
    }

    /**
     * Error log + crashlytics log
     *
     * @param msg Log message
     */
    fun e(msg: String) {
        Log.e(CupsPrintApp.LOG_TAG, msg)
        Crashlytics.log("E: $msg")
    }

    /**
     * Error reporting + send exception to crashlytics
     *
     * @param msg Log message
     * @param t   Throwable to send to crashlytics, if not null
     */
    fun e(msg: String, t: Throwable?) {
        e(msg)
        if (t != null) {
            e(t.localizedMessage)
            Crashlytics.logException(t)
        }
    }
}
