package meg.tools.imp;

import meg.tools.imp.utils.FieldFormat;

/**
 * Implementations of FileConfig hold the information about how to read a file
 * into placeholder objects, which are then mapped into business logic objects.
 * FileConfig objects only hold information about the file to be imported itself -
 * mapping information is kept separately.
 * 
 * Eventually, the FileConfig information should be saved either in an .xml file
 * or in the database. But to hurry things along right now, I'm going to make a
 * specific implementation for each file import that I have.
 * 
 * @author maggie
 * 
 */
public interface FileConfig {

	public static final class FileType {
		public static final int Delimited = 1;

		public static final int Flat = 2;

		public static final int Xml = 3;
	}

	int getFileType();

	int getStartLine();

	String getFieldDelim();

	FieldFormat[] getFieldFormats();
}
