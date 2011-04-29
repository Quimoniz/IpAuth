package org.quimoniz.ipauth;

import java.util.Hashtable;
import java.util.Collection;
import java.util.Iterator;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.charset.Charset;

public class AuthSaver {
  private String fileLocation;
  private Charset charset;
  public AuthSaver(String fileLocation) {
    this.fileLocation = fileLocation;
    try {
      charset = Charset.forName("ASCII");
    } catch(UnsupportedCharsetException exc) {
      handleException(exc);
    }
  }
  public void saveFile(Hashtable<String, AuthData> dataTable) {
    Collection<AuthData> tableEntries = dataTable.values();
    Iterator<AuthData> iter = tableEntries.iterator();
    FileOutputStream outStream = null;
    byte[] outBytes;
    try {
      outStream = new FileOutputStream(fileLocation);
    } catch(IOException exc) {
      handleException(exc);
    }
    outBytes = (new Integer(tableEntries.size()).toString() + "\n").getBytes(charset);
    try {
      outStream.write(outBytes);
    } catch(IOException exc) {
      handleException(exc);
    }
	while(iter.hasNext()) {
	  AuthData data = iter.next();
      outBytes = data.dataLine().getBytes(charset);
      try {
        outStream.write(outBytes);
        outStream.write(new byte[]{13,10});
      } catch(IOException exc) {
        handleException(exc);
      }
	}
  }
  public void handleException(Exception exc) {
    exc.printStackTrace();
  }
}