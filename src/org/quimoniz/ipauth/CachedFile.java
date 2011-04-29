import java.io.File;
import java.io.IOException;
import java.io.FileReader;

public class CachedFile {
  private File myFile;
  private long lastRead = 0;
  private String cachedContent = null;
  public CachedFile(File file) {
    myFile = file;
  }
  public String getContents() {
    if(cachedContent == null || myFile.lastModified() > lastRead) {
	  FileReader reader = null;
	  try {
	    reader = new FileReader(myFile);
	  } catch(IOException exc) {
	    exc.printStackTrace();
		return cachedContent;
	  }
	  final int BUFLENGTH = 1300;
	  long fileSize = myFile.length();
	  if(fileSize > Integer.MAX_VALUE) {
	    fileSize = Integer.MAX_VALUE;
		//I hate throwing Exceptions, so we will not do so
	  }
	  StringBuilder buf = new StringBuilder((int)fileSize);
	  char[] cbuf = new char[BUFLENGTH];
	  int charsCurrentlyRead = 0, charsRead = 0;
//	  System.out.println("Reading file " + myFile.getPath());
	  do {
	    charsCurrentlyRead = -1;
		try {
		  //Good that the implicit InputStreamReader takes care of converting read bytes into chars
	      charsCurrentlyRead = reader.read(cbuf, 0, (int) (((charsRead + BUFLENGTH) < fileSize) ? BUFLENGTH : (fileSize - charsRead)));
		} catch(IOException exc) {
		  exc.printStackTrace();
		}
		if(charsCurrentlyRead >= 1) {
		  charsRead += charsCurrentlyRead;
		  buf.append(cbuf, 0, charsCurrentlyRead);
		}
	  } while(charsCurrentlyRead >= 0 && (charsRead + charsCurrentlyRead) < fileSize);
	  cachedContent = buf.toString();
	  lastRead = myFile.lastModified();
	}
	return cachedContent;
  }
}