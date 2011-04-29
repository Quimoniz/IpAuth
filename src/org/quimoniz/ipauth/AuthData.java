package org.quimoniz.ipauth;

public class AuthData {
  private String userName;
  private String password;
  private long authTime = 0;
  private byte[] ipBytes = null;
  public AuthData(String userName, String password, byte[] ipBytes, long authTime) {
    this.userName = userName;
	this.password = password;
	this.authTime = authTime;
	this.ipBytes = ipBytes;
  }
  public AuthData(String userName, String password) {
    this.userName = userName;
	this.password = password;
  }
  public String getUserName() { return userName; }
  public String getPassword() { return password; }
  public long getAuthTime() { return authTime; }
  public byte[] getIp() { return ipBytes; }
  public String dataLine() {
    StringBuilder outBuf = new StringBuilder(40);
    outBuf.append("\"" + userName.replaceAll("\"","\"\"") + "\",\"" + password + "\"");
    if(ipBytes != null) {
      outBuf.append(',');
	  for(int i = 0; i < ipBytes.length; i++) {
	    if(ipBytes[i] < 0)
	      outBuf.append(""+(((int)ipBytes[i])+256));
	     else
	       outBuf.append(""+ipBytes[i]);
        if((i + 1) < ipBytes.length)
	      outBuf.append('.');
      }
      outBuf.append("," + authTime);
	}
	return outBuf.toString();
  }
  public String toString() {
    return dataLine();
  }
}