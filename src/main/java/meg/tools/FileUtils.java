package meg.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtils {

	

	   /**
	    * Write a file, containing the given text, to the filesystem.
	    * <br>
	    * filename must represent the complete path together with the files name.<br>
	    * <b>Example:</b> "C:\projects\test.txt" would be a valid filename if working on a
	    * Windows environment.<br>
	    * @param filename   Name of the file to write
	    * @param text       String to write into the file
	    * @return           True if succesful, false if not.
	    */
	   static public boolean writeStringToFile (String filename, String text) {
	      FileWriter fWriter;

	      try {
	       fWriter = new FileWriter(filename);
	       fWriter.write(text);
	       fWriter.close();
	      } catch (IOException e) {
	         return false;
	      }

	      return true;
	   }
	   
	   static public void deleteFile(String path) {

		      try
		      {
		          File file = new File(path);
		          file.delete();
		      }  catch (Exception e) {
		         return;
		      }
		   }
	   
	   
	   
}
