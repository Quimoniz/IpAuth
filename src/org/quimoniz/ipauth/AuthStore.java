package org.quimoniz.ipauth;

import java.io.File;
import java.util.Hashtable;

public class AuthStore {
  private AuthLoader loader;
  private AuthSaver  saver;
  private final int DEFAULTTABLESIZE = 20;
  private Hashtable <String, AuthData> authList = new Hashtable <String, AuthData>(DEFAULTTABLESIZE);
  private String fileLocation;
  public AuthStore (String fileLocation) {
    loader = new AuthLoader(fileLocation, this);
	saver  = new AuthSaver (fileLocation);
	this.fileLocation = fileLocation;
  }
  public void save() {
    saver.saveFile(authList);
  }
  public void setListLength(int lineCount) {
    if(lineCount > DEFAULTTABLESIZE)
      authList = new Hashtable <String, AuthData>(lineCount);
  }
  public void add(AuthData data) {
    authList.put(data.getUserName(),data);
  }
  public AuthData get(String key) {
    return authList.get(key);
  }
}