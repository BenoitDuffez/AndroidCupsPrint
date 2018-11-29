package io.github.benoitduffez.cupsprint.detect

class PrinterRec internal constructor(val nickname: String, val protocol: String, val host: String, val port: Int, val queue: String) : Comparable<PrinterRec> {
    override fun toString(): String = "$nickname ($protocol on $host)"
    override fun compareTo(other: PrinterRec): Int = toString().compareTo(other.toString())
}
