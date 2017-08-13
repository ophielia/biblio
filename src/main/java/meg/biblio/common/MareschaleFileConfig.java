package meg.biblio.common;

import meg.tools.imp.FileConfig;
import meg.tools.imp.utils.FieldFormat;


public class MareschaleFileConfig implements FileConfig {

    public String getFieldDelim() {
        return "\t";
    }

    public int getFileType() {
        return FileConfig.FileType.Delimited;
    }

    public int getStartLine() {
        return 0;
    }

    public FieldFormat[] getFieldFormats() {
        FieldFormat[] formats = new FieldFormat[8];

        formats[0] = new FieldFormat();
        formats[1] = new FieldFormat();
        formats[2] = new FieldFormat();
        formats[3] = new FieldFormat();
        formats[4] = new FieldFormat();
        formats[5] = new FieldFormat();
        formats[6] = new FieldFormat();
        formats[7] = new FieldFormat();

        formats[0].setFieldType(FieldFormat.Type.String);
        formats[0].setFieldTag("field1");

        formats[1].setFieldType(FieldFormat.Type.String);
        formats[1].setFieldTag("field2");

        formats[2].setFieldType(FieldFormat.Type.String);
        formats[2].setFieldTag("field3");

        formats[3].setFieldType(FieldFormat.Type.String);
        formats[3].setFieldTag("field4");

        formats[4].setFieldType(FieldFormat.Type.String);
        formats[4].setFieldTag("field5");

        formats[5].setFieldType(FieldFormat.Type.String);
        formats[5].setFieldTag("field6");

        formats[6].setFieldType(FieldFormat.Type.String);
        formats[6].setFieldTag("field7");

        formats[7].setFieldType(FieldFormat.Type.String);
        formats[7].setFieldTag("field8");

        return formats;

    }

}
