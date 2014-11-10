package meg.tools;

import java.util.regex.Pattern;

public class StringUtils {

    public static String[] split(String tosplit,String delim) {
        // Create a pattern to match breaks
        Pattern p = Pattern.compile(delim);
        // Split input with the pattern
        String[] result =
                 p.split(tosplit);
        return result;
    }


}
