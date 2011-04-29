/* We dont need to run this caching class in an additional Thread.
*  Very fast responses are less important then using less ressources.
*  If you can tell me how to make it more efficient, please contact me at quimoniz@resonanzkaska.de
*/

package org.quimoniz.ipauth;

import java.util.HashMap;
import java.io.File;
import java.io.FileNotFoundException;

public class CachedFileGetter {
   private static HashMap<String,CachedFile> fileList = new HashMap<String,CachedFile>();
   public static String getFile(String fileName) throws FileNotFoundException {
	 File fileObject = new File(fileName);
	 if(!fileObject.exists() || !fileObject.isFile())
	   throw new FileNotFoundException("File " + fileName + " could not be found!");
     CachedFile file = fileList.get(fileName);
	 if(file == null) {
	   file = new CachedFile(fileObject);
	   fileList.put(fileName,file);
	 }
	 return file.getContents();
   }
   public static void load(String fileName) {
	File fileObject = new File(fileName);
	if(!fileObject.exists() || !fileObject.isFile())
	  return;
	CachedFile file = fileList.get(fileName);
	if(file == null) {
	  file = new CachedFile(fileObject);
	  fileList.put(fileName,file);
	}
   }
}