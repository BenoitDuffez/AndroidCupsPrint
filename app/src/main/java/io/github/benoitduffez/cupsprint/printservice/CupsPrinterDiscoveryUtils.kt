package io.github.benoitduffez.cupsprint.printservice

import android.print.PrintAttributes
import ch.ethz.vppserver.schema.ippclient.AttributeValue
import java.util.*
import java.util.regex.Pattern

/**
 * Misc util methods
 */
internal object CupsPrinterDiscoveryUtils {
    /**
     * Compute the resolution from the [AttributeValue]
     *
     * @param id             resolution ID (nullable)
     * @param attributeValue attribute (resolution) value
     * @return resolution parsed into a [android.print.PrintAttributes.Resolution]
     */
    fun getResolutionFromAttributeValue(id: String, attributeValue: AttributeValue): PrintAttributes.Resolution {
        val resolution = attributeValue.value!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val horizontal: Int
        val vertical: Int
        horizontal = Integer.parseInt(resolution[0])
        vertical = Integer.parseInt(resolution[1])
        return PrintAttributes.Resolution(id, String.format(Locale.ENGLISH, "%dx%d dpi", horizontal, vertical), horizontal, vertical)
    }

    /**
     * Compute the media size from the [AttributeValue]
     *
     * @param attributeValue attribute (media size) value
     * @return media size parsed into a [PrintAttributes.MediaSize]
     */
    fun getMediaSizeFromAttributeValue(attributeValue: AttributeValue): PrintAttributes.MediaSize? {
        val value = attributeValue.value?.toLowerCase(Locale.ENGLISH) ?: ""
        when {
            value.startsWith("ISO_A0".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.ISO_A0
            value.startsWith("ISO_A1".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.ISO_A1
            value.startsWith("ISO_A10".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.ISO_A10
            value.startsWith("ISO_A2".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.ISO_A2
            value.startsWith("ISO_A3".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.ISO_A3
            value.startsWith("ISO_A4".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.ISO_A4
            value.startsWith("ISO_A5".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.ISO_A5
            value.startsWith("ISO_A6".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.ISO_A6
            value.startsWith("ISO_A7".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.ISO_A7
            value.startsWith("ISO_A8".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.ISO_A8
            value.startsWith("ISO_A9".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.ISO_A9
            value.startsWith("ISO_B0".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.ISO_B0
            value.startsWith("ISO_B1".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.ISO_B1
            value.startsWith("ISO_B10".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.ISO_B10
            value.startsWith("ISO_B2".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.ISO_B2
            value.startsWith("ISO_B3".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.ISO_B3
            value.startsWith("ISO_B4".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.ISO_B4
            value.startsWith("ISO_B5".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.ISO_B5
            value.startsWith("ISO_B6".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.ISO_B6
            value.startsWith("ISO_B7".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.ISO_B7
            value.startsWith("ISO_B8".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.ISO_B8
            value.startsWith("ISO_B9".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.ISO_B9
            value.startsWith("ISO_C0".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.ISO_C0
            value.startsWith("ISO_C1".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.ISO_C1
            value.startsWith("ISO_C10".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.ISO_C10
            value.startsWith("ISO_C2".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.ISO_C2
            value.startsWith("ISO_C3".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.ISO_C3
            value.startsWith("ISO_C4".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.ISO_C4
            value.startsWith("ISO_C5".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.ISO_C5
            value.startsWith("ISO_C6".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.ISO_C6
            value.startsWith("ISO_C7".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.ISO_C7
            value.startsWith("ISO_C8".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.ISO_C8
            value.startsWith("ISO_C9".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.ISO_C9
            value.startsWith("JIS_B0".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.JIS_B0
            value.startsWith("JIS_B1".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.JIS_B1
            value.startsWith("JIS_B10".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.JIS_B10
            value.startsWith("JIS_B2".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.JIS_B2
            value.startsWith("JIS_B3".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.JIS_B3
            value.startsWith("JIS_B4".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.JIS_B4
            value.startsWith("JIS_B5".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.JIS_B5
            value.startsWith("JIS_B6".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.JIS_B6
            value.startsWith("JIS_B7".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.JIS_B7
            value.startsWith("JIS_B8".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.JIS_B8
            value.startsWith("JIS_B9".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.JIS_B9
            value.startsWith("JIS_EXEC".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.JIS_EXEC
            value.startsWith("JPN_CHOU2".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.JPN_CHOU2
            value.startsWith("JPN_CHOU3".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.JPN_CHOU3
            value.startsWith("JPN_CHOU4".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.JPN_CHOU4
            value.startsWith("JPN_HAGAKI".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.JPN_HAGAKI
            value.startsWith("JPN_KAHU".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.JPN_KAHU
            value.startsWith("JPN_KAKU2".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.JPN_KAKU2
            value.startsWith("JPN_OUFUKU".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.JPN_OUFUKU
            value.startsWith("JPN_YOU4".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.JPN_YOU4
            value.startsWith("NA_FOOLSCAP".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.NA_FOOLSCAP
            value.startsWith("NA_GOVT_LETTER".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.NA_GOVT_LETTER
            value.startsWith("NA_INDEX_3X5".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.NA_INDEX_3X5
            value.startsWith("NA_INDEX_4X6".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.NA_INDEX_4X6
            value.startsWith("NA_INDEX_5X8".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.NA_INDEX_5X8
            value.startsWith("NA_JUNIOR_LEGAL".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.NA_JUNIOR_LEGAL
            value.startsWith("NA_LEDGER".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.NA_LEDGER
            value.startsWith("NA_LEGAL".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.NA_LEGAL
            value.startsWith("NA_LETTER".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.NA_LETTER
            value.startsWith("NA_MONARCH".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.NA_MONARCH
            value.startsWith("NA_QUARTO".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.NA_QUARTO
            value.startsWith("NA_TABLOID".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.NA_TABLOID
            value.startsWith("OM_DAI_PA_KAI".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.OM_DAI_PA_KAI
            value.startsWith("OM_JUURO_KU_KAI".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.OM_JUURO_KU_KAI
            value.startsWith("OM_PA_KAI".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.OM_PA_KAI
            value.startsWith("PRC_1".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.PRC_1
            value.startsWith("PRC_10".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.PRC_10
            value.startsWith("PRC_16K".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.PRC_16K
            value.startsWith("PRC_2".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.PRC_2
            value.startsWith("PRC_3".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.PRC_3
            value.startsWith("PRC_4".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.PRC_4
            value.startsWith("PRC_5".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.PRC_5
            value.startsWith("PRC_6".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.PRC_6
            value.startsWith("PRC_7".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.PRC_7
            value.startsWith("PRC_8".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.PRC_8
            value.startsWith("PRC_9".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.PRC_9
            value.startsWith("ROC_16K".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.ROC_16K
            value.startsWith("ROC_8K".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.ROC_8K
            value.startsWith("UNKNOWN_LANDSCAPE".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.UNKNOWN_LANDSCAPE
            value.startsWith("UNKNOWN_PORTRAIT".toLowerCase(Locale.ENGLISH)) -> return PrintAttributes.MediaSize.UNKNOWN_PORTRAIT
            else -> {
                val m = Pattern.compile("_((\\d*\\.?\\d+)x(\\d*\\.?\\d+)([a-z]+))$").matcher(value)
                if (m.find()) {
                    try {
                        var x = java.lang.Float.parseFloat(m.group(2)?:"0")
                        var y = java.lang.Float.parseFloat(m.group(3)?:"0")
                        when (m.group(4)) {
                            "mm" -> {
                                x /= 25.4f
                                y /= 25.4f
                                x *= 1000f
                                y *= 1000f
                            }
                            // fall thru
                            "in" -> {
                                x *= 1000f
                                y *= 1000f
                            }
                            else -> return null
                        }
                        return PrintAttributes.MediaSize(value, m.group(1)?:"0", Math.round(x), Math.round(y))
                    } catch (ignored: NumberFormatException) {
                    }
                }
                return null
            }
        }
    }
}
