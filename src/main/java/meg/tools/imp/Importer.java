package meg.tools.imp;

import java.io.File;
import java.text.ParseException;
import java.util.List;

import meg.tools.imp.utils.FieldFormat;
import meg.tools.imp.utils.Placeholder;

/**
 * The Importer object reads a file into placeholder objects. Different
 * implementations will handle delimited files, flat files and xml files. The
 * FileConfig object works with the Importer object to pass in additional
 * information, which may be specific to a particular import (where to start the
 * import, which information to ignore, and so on...)
 * 
 * @author maggie
 * 
 */
public interface Importer {

	List<Placeholder> parseFile(File file);  // TODO: generalize this, to allow String input

	Object formatField(String field, FieldFormat format) throws ParseException;

	List<Placeholder> parseString(String source);

}
