package meg.tools.imp.utils;

import java.util.Locale;

public class FieldFormat {

	public static final class Type {
		public static final int String = 1;

		public static final int Double = 2;

		public static final int Ignore = 3;

		public static final int DateTime = 4;
	}

	private int fieldtype;

	private String inputpattern;

	private String inputfieldname;

	private String fieldTag;
	
	private Locale locale;

	private String textToRemove;

	public int getFieldType() {
		return fieldtype;
	}

	public void setFieldType(int fieldtype) {
		this.fieldtype = fieldtype;
	}

	public String getInputPattern() {
		return inputpattern;
	}

	public void setInputPattern(String inputpattern) {
		this.inputpattern = inputpattern;
	}

	public String getInputFieldName() {
		return inputfieldname;
	}

	public void setInputFieldName(String inputfieldname) {
		this.inputfieldname = inputfieldname;
	}

	public String getFieldTag() {
		return fieldTag;
	}

	public void setFieldTag(String fieldTag) {
		this.fieldTag = fieldTag;
	}
	
	public void setLocale(String locale) {
		this.locale = new Locale(locale);
	}
	
	public Locale getLocale() {
		if (this.locale != null) {
			return this.locale;
		} 
		return Locale.FRANCE;
	}

	public void setTextToRemove(String string) {
this.textToRemove=string;
		
	}

	public String getTextToRemove() {
		return textToRemove;
	}	
	
	

}
