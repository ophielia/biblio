package meg.tools.imp.utils;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

// TODO make these conversion methods more robust
public class ConversionUtils {

	public static String convertToString(Object toset) {
		if (toset==null) return "";
		if (toset instanceof String) {
			return (String) toset;
		} else {
			return toset.toString();
		}
	}

	public static Double convertToDouble(Object toset) {
		if (toset instanceof Double) {
			return (Double) toset;
		} else if (toset instanceof Long) {
			return new Double(((Long) toset).doubleValue());
		} else {
			DecimalFormat numberformat = new DecimalFormat();
			try {
				return (Double) numberformat.parse(toset.toString());
			} catch (ParseException e) {
				// TODO error handling here
			}
		}

		return null;

	}

	public static Date convertToDate(Object toset) {
		if (toset instanceof Date) {
			return (Date) toset;
		} else {
			SimpleDateFormat dateformat = new SimpleDateFormat();
			try {
				return dateformat.parse(toset.toString());
			} catch (ParseException e) {
				// TODO error handling here
			}
		}
		return null;

	}

	public static Long convertToLong(Object toset) {
		if (toset instanceof Long) {
			return (Long) toset;
		} else {
			return new Long(toset.toString());
		}

	}

}
