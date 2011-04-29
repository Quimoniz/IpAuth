import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.SocketTimeoutException;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.net.InetSocketAddress;

public class HttpListener implements Runnable {
  private ServerSocket webSocket = null;
  private boolean isRunning = false;
  private HashSet<HttpConnection> conList = new HashSet <HttpConnection>();
//  private String properties = "ipauth.properties";
  private String obfuscationMethod = "SHA1";
  private byte[] obfuscationSalt = new byte[] {-46,112,-85,27,-52,-71,-59,112,-24,46,-11,-55,74,13,-13,89,76,-95,-74,-55};
  private final byte[]  HEXCODES = new byte [] {(byte)'0',(byte)'1',(byte)'2',(byte)'3',(byte)'4',(byte)'5',(byte)'6',(byte)'7',(byte)'8',(byte)'9',(byte)'a',(byte)'b',(byte)'c',(byte)'d',(byte)'e',(byte)'f'};
  private MessageDigest obfuscationObject = null;
  private IpAuth plugin;
  public HttpListener(int port, IpAuth plugin) {
    this.plugin = plugin;
  	try {
	  webSocket = new ServerSocket(port);
	} catch(IOException exc) {
	  exc.printStackTrace();
	}
  }
  public void stop() {
    isRunning = false;
  }
  public void start() {
    if(!isRunning) {
	  isRunning = true;
	  t = new Thread(this);
	  t.start();
	}
  }
  @Override public void run() {
    try {
      webSocket.setSoTimeout(200);
	} catch(java.net.SocketException exc) {
	  exc.printStackTrace();
	}
	while(isRunning) {
	  try {
	    newConnection(webSocket.accept());
	  } catch(SocketTimeoutException exc) {
	  } catch(IOException exc) {
	    log(exc);
	  }
	}
  }
  public void newConnection(Socket con) {
    conList.add(new HttpConnection(con,this));
  }
  public void log(String line) {
    plugin.log(line);
  }
  public void log(Exception exc) {
    plugin.log(exc);
  }
  public synchronized String obfuscate(String toObfuscate) {
    if(obfuscationObject == null) {
	  if(obfuscationMethod != null && obfuscationMethod.length() > 0) {
	    try {
	      obfuscationObject = MessageDigest.getInstance(obfuscationMethod);
		} catch(NoSuchAlgorithmException exc) {
		  log(exc.toString());
		}
	  }
	} else obfuscationObject.reset();
	obfuscationObject.update(obfuscationSalt);
	byte [] obfuscatedBytes = obfuscationObject.digest(toObfuscate.getBytes());
	byte [] hexOut = new byte [obfuscatedBytes.length + obfuscatedBytes.length];
	for(int i = 0, j = 0; i < obfuscatedBytes.length;) {
	  hexOut[j] |= HEXCODES[obfuscatedBytes[i]>>4 & 0x0f];
	  j += 1;
	  hexOut[j] |= HEXCODES[obfuscatedBytes[i] & 0x0f];
	  i += 1;
	  j += 1;
	}
	return new String(hexOut);
  }
  public boolean authenticate(String user, String password, InetSocketAddress) {
    return true;
  }
  
}