package org.quimoniz.ipauth;

import java.net.Socket;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.IOException;


public class HttpConnection implements Runnable {
  private IpAuth pluginMain;
  private Socket myConnection;
  private Thread t;
  private boolean isRunning = false;
  public HttpConnection(Socket clientSocket, IpAuth pluginMain) {
    this.pluginMain = pluginMain;
	myConnection = clientSocket;
	try {
	  t = new Thread(this);
	  isRunning = true;
	  t.start();
	}
  }
  @Override public void run() {
    try {
      OutputStream    out = myConnection.getOutputStream();
	  BufferedReader  in  = new BufferedReader(new InputStreamReader(myConnection.getInputStream()));
	} catch(IOException exc) {
	  pluginMain.logException(exc);
	}
    while(isRunning) {
	  String requestLine = null;
	  try {
	    requestLine = in.readLine()
	  } catch(IOException exc) {
	    //We will assume that the connection got cut off or aborted, Immediate connection cutback
	  }
	  if(requestLine == null) {
	    close();
		break;
	  }
	  
	}
  }
  public void close() {
    isRunning = false;
	try {
	  myConnection.close();
	} catch(IOException exc) {
	  pluginMain.logException(exc);
	}
  }
}