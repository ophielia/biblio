package meg.tools.imp.utils;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import meg.tools.imp.Importer;


public abstract class AbstractImporter implements Importer {

	private DecimalFormat numberformat = new DecimalFormat();

	private DateFormat dateformat = new SimpleDateFormat();

	public Object formatField(String field, FieldFormat format)
			throws ParseException {
		switch (format.getFieldType()) {
		case FieldFormat.Type.String:
			return field;
		case FieldFormat.Type.Double:
			DecimalFormatSymbols symbols = new DecimalFormatSymbols(format.getLocale());
			symbols.setGroupingSeparator('.');
			symbols.setDecimalSeparator(',');
			numberformat = new DecimalFormat(format.getInputPattern(),symbols);
			field = stripSpaces(field);
			return numberformat.parse(field);
		case FieldFormat.Type.DateTime:
			dateformat = new SimpleDateFormat(format.getInputPattern());
			return dateformat.parse(field);

		default:
			return null;
		}
	}

	private String stripSpaces(String field) {
		StringBuffer stripped = new StringBuffer();
		StringBuffer fieldbuf = new StringBuffer(field);
		for (int 	i = 0; i < field.length(); i++) {
			if (fieldbuf.substring(i,i+1).equals("+")) continue;
			if (fieldbuf.substring(i,i+1).equals(" ")) continue;
			stripped.append(fieldbuf.substring(i,i+1));
		}
		return stripped.toString();
	}

}
