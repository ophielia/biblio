package meg.tools.imp;



public class ImporterFactory {
// TODO: import package - add result objects
	// TODO: import package - add error handling
	// TODO: import package - finish conversion utilities
	public static Importer getImporter(FileConfig config) {
		// currently, importer selection is based only upon the filetype.
		int filetype = config.getFileType();
		switch (filetype) {
		case FileConfig.FileType.Delimited:
			return new DelimitedImporter(config);
		default:
			// should throw a specific exception here - right now throwing a
			// kink in the works instead by returning a null.
			return null;

		}
	}

}
