package org.quimoniz.ipauth;


public class AuthLoader extends CSVParser {
  private int lineCount = -1;
  private AuthStore store;
  public AuthLoader(String fileLocation, AuthStore store) {
    this.store = store;
    parseFile(fileLocation);
  }
  public void processDataRecord(String [] dataRecord) {
    if(lineCount == -1) {
	  try {
	    lineCount = Integer.parseInt(dataRecord[0]);
	  } catch(NumberFormatException exc) {
	    handleException(exc);
	  }
	  if(lineCount >= 0)
	    store.setListLength(lineCount);
	} else {
	  if(dataRecord.length == 4) {
        String userName = dataRecord[0];
	    String password = dataRecord[1];
	    String ipString = dataRecord[2];
	    byte [] ipBytes;
	    String [] ipSplits = ipString.split("\\.");
	    ipBytes = new byte[ipSplits.length];
	    for(int i = 0; i < ipSplits.length; i++) {
	      int parseByteInt = 0;
	      try {
	        parseByteInt = Integer.parseInt(ipSplits[i]);
	      } catch(NumberFormatException exc) {
	        handleException(exc);
	      }
	      if(parseByteInt < 128)
	        ipBytes[i] = (byte) parseByteInt;
	      else
            ipBytes[i] = (byte) (parseByteInt - 256);
        }
	    long authTime = 0;
	    try {
	      authTime = Long.parseLong(dataRecord[3]);
	    } catch(NumberFormatException exc) {
	      handleException(exc);
	    }
	    AuthData data = new AuthData(userName, password, ipBytes, authTime);
	    store.add(data);
	  } else if(dataRecord.length == 2) {
	    String userName = dataRecord[0];
		String password = dataRecord[1];
	    AuthData data = new AuthData(userName, password);
	    store.add(data);
	  }
	}
  }
  public void handleException(Exception exc) {
    exc.printStackTrace();
  }
}