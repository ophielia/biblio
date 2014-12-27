package meg.tools.imp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import meg.tools.StringUtils;
import meg.tools.imp.utils.AbstractImporter;
import meg.tools.imp.utils.FieldFormat;
import meg.tools.imp.utils.Placeholder;
import meg.tools.imp.utils.TextPlaceholder;

public class DelimitedImporter extends AbstractImporter {

	private FileConfig config;

	public DelimitedImporter(FileConfig config) {
		this.config = config;
	}

	public List<Placeholder> parseFile(File file) {
		List<Placeholder> placeholders = new ArrayList<Placeholder>();
		try {
			// put file into BufferedReader
			FileReader fileread = new FileReader(file);
			BufferedReader bufread = new BufferedReader(fileread);

			// increment past beginning lines, if necessary
			int linenumber = this.config.getStartLine();
			int currentline = 0;
			String discard = bufread.readLine();
			while (discard != null && currentline < linenumber - 2) {
				// do nothing, just discarding some lines
				discard = bufread.readLine();
				currentline++;
			}

			// retrieve formats
			FieldFormat[] formats = config.getFieldFormats();

			String raw = null;
			while ((raw = bufread.readLine()) != null) {
				// break raw line into fields
				String[] fields = StringUtils
						.split(raw, config.getFieldDelim());

				// feed fields into placeholder
				Placeholder hold = new TextPlaceholder();
				for (int i = 0; i < fields.length; i++) {
					FieldFormat format = formats[i];
					// TODO: if format is null, import with default, don't skip.
					if (format == null
							|| format.getFieldType() == FieldFormat.Type.Ignore)
						continue;

					// format field
					Object formatted;
					try {
						formatted = formatField(fields[i], format);
						hold.setField(format.getFieldTag(), formatted);
					} catch (ParseException e) {
						// TODO: error handling
					}

				}
				// add placeholder object to list
				placeholders.add(hold);
			}

		} catch (IOException e) {
			//
			// put your error-handling code here
			//
		}

		// return list of placeholder objects
		return placeholders;
	}

	@Override
	public List<Placeholder> parseString(String source) {

		// put file into BufferedReader
		StringReader strread = new StringReader(source);
		BufferedReader bufread = new LineNumberReader(strread);

		try {
			return parseFromBuffer(bufread);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private List<Placeholder> parseFromBuffer(BufferedReader bufread)
			throws IOException {
		List<Placeholder> placeholders = new ArrayList<Placeholder>();
		// increment past beginning lines, if necessary
		int linenumber = this.config.getStartLine();
		int currentline = 0;
		String discard = bufread.readLine();
		while (discard != null && currentline < linenumber - 2) {
			// do nothing, just discarding some lines
			discard = bufread.readLine();
			currentline++;
		}

		// retrieve formats
		FieldFormat[] formats = config.getFieldFormats();

		String raw = null;
		while ((raw = bufread.readLine()) != null) {
			// break raw line into fields
			String[] fields = StringUtils.split(raw, config.getFieldDelim());

			// feed fields into placeholder
			Placeholder hold = new TextPlaceholder();
			for (int i = 0; i < fields.length; i++) {
				FieldFormat format = formats[i];
				// TODO: if format is null, import with default, don't skip.
				if (format == null
						|| format.getFieldType() == FieldFormat.Type.Ignore)
					continue;

				// format field
				Object formatted;
				try {
					formatted = formatField(fields[i], format);
					hold.setField(format.getFieldTag(), formatted);
				} catch (ParseException e) {
					// TODO: error handling
				}

			}
			// add placeholder object to list
			placeholders.add(hold);
		}
		return placeholders;
	}

}
