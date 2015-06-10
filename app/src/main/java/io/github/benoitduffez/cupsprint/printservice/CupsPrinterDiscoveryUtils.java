package io.github.benoitduffez.cupsprint.printservice;

import android.print.PrintAttributes;
import android.support.annotation.Nullable;

import java.util.Locale;

import ch.ethz.vppserver.schema.ippclient.AttributeValue;

/**
 * Misc util methods
 */
class CupsPrinterDiscoveryUtils {
	/**
	 * Compute the resolution from the {@link AttributeValue}
	 *
	 * @param id             resolution ID (nullable)
	 * @param attributeValue attribute (resolution) value
	 * @return resolution parsed into a {@link android.print.PrintAttributes.Resolution}
	 */
	protected static PrintAttributes.Resolution getResolutionFromAttributeValue(@Nullable String id, AttributeValue attributeValue) {
		String[] resolution = attributeValue.getValue().split(",");
		int horizontal, vertical;
		horizontal = Integer.parseInt(resolution[0]);
		vertical = Integer.parseInt(resolution[1]);
		return new PrintAttributes.Resolution(id, String.format(Locale.ENGLISH, "%dx%d dpi", horizontal, vertical), horizontal, vertical);
	}

	/**
	 * Compute the media size from the {@link AttributeValue}
	 *
	 * @param attributeValue attribute (media size) value
	 * @return media size parsed into a {@link PrintAttributes.MediaSize}
	 */
	protected static PrintAttributes.MediaSize getMediaSizeFromAttributeValue(AttributeValue attributeValue) {
		String value = attributeValue.getValue();
		if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_A0".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.ISO_A0;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_A1".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.ISO_A1;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_A10".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.ISO_A10;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_A2".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.ISO_A2;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_A3".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.ISO_A3;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_A4".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.ISO_A4;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_A5".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.ISO_A5;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_A6".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.ISO_A6;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_A7".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.ISO_A7;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_A8".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.ISO_A8;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_A9".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.ISO_A9;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_B0".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.ISO_B0;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_B1".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.ISO_B1;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_B10".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.ISO_B10;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_B2".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.ISO_B2;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_B3".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.ISO_B3;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_B4".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.ISO_B4;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_B5".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.ISO_B5;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_B6".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.ISO_B6;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_B7".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.ISO_B7;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_B8".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.ISO_B8;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_B9".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.ISO_B9;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_C0".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.ISO_C0;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_C1".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.ISO_C1;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_C10".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.ISO_C10;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_C2".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.ISO_C2;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_C3".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.ISO_C3;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_C4".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.ISO_C4;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_C5".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.ISO_C5;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_C6".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.ISO_C6;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_C7".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.ISO_C7;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_C8".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.ISO_C8;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ISO_C9".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.ISO_C9;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JIS_B0".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.JIS_B0;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JIS_B1".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.JIS_B1;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JIS_B10".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.JIS_B10;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JIS_B2".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.JIS_B2;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JIS_B3".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.JIS_B3;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JIS_B4".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.JIS_B4;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JIS_B5".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.JIS_B5;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JIS_B6".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.JIS_B6;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JIS_B7".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.JIS_B7;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JIS_B8".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.JIS_B8;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JIS_B9".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.JIS_B9;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JIS_EXEC".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.JIS_EXEC;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JPN_CHOU2".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.JPN_CHOU2;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JPN_CHOU3".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.JPN_CHOU3;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JPN_CHOU4".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.JPN_CHOU4;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JPN_HAGAKI".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.JPN_HAGAKI;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JPN_KAHU".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.JPN_KAHU;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JPN_KAKU2".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.JPN_KAKU2;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JPN_OUFUKU".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.JPN_OUFUKU;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("JPN_YOU4".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.JPN_YOU4;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("NA_FOOLSCAP".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.NA_FOOLSCAP;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("NA_GOVT_LETTER".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.NA_GOVT_LETTER;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("NA_INDEX_3X5".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.NA_INDEX_3X5;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("NA_INDEX_4X6".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.NA_INDEX_4X6;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("NA_INDEX_5X8".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.NA_INDEX_5X8;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("NA_JUNIOR_LEGAL".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.NA_JUNIOR_LEGAL;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("NA_LEDGER".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.NA_LEDGER;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("NA_LEGAL".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.NA_LEGAL;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("NA_LETTER".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.NA_LETTER;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("NA_MONARCH".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.NA_MONARCH;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("NA_QUARTO".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.NA_QUARTO;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("NA_TABLOID".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.NA_TABLOID;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("OM_DAI_PA_KAI".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.OM_DAI_PA_KAI;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("OM_JUURO_KU_KAI".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.OM_JUURO_KU_KAI;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("OM_PA_KAI".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.OM_PA_KAI;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("PRC_1".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.PRC_1;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("PRC_10".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.PRC_10;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("PRC_16K".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.PRC_16K;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("PRC_2".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.PRC_2;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("PRC_3".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.PRC_3;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("PRC_4".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.PRC_4;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("PRC_5".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.PRC_5;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("PRC_6".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.PRC_6;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("PRC_7".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.PRC_7;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("PRC_8".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.PRC_8;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("PRC_9".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.PRC_9;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ROC_16K".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.ROC_16K;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("ROC_8K".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.ROC_8K;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("UNKNOWN_LANDSCAPE".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.UNKNOWN_LANDSCAPE;
		} else if (value.toLowerCase(Locale.ENGLISH).startsWith("UNKNOWN_PORTRAIT".toLowerCase(Locale.ENGLISH))) {
			return PrintAttributes.MediaSize.UNKNOWN_PORTRAIT;
		} else {
			return null;
		}
	}
}
