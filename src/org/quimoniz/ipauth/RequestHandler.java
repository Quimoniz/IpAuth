package org.quimoniz.ipauth;

import java.io.FileNotFoundException;

public class RequestHandler {
  private HttpRequest request;
  private HttpConnection con;
  public RequestHandler(HttpRequest request, HttpConnection con) {
    this.request = request;
	this.con = con;
  }
  public void processRequest(String requestLine) {
	final String sampleHeader = "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\nPragma: no-cache\r\nCache-Control: no-cache\r\nConnection: Close";

     String[] arguments = requestLine.split(" ");
	if(arguments.length<2) return;
//    System.out.println("Processing Request: " + requestLine);
//    System.out.println(java.util.Arrays.toString(request.getHeader()));
	if(arguments[0].equalsIgnoreCase("GET")) {
	  if(arguments[1].equalsIgnoreCase("/")) {
		    String loginContent = null;
			try {
			  loginContent   = CachedFileGetter.getFile("samples/login.html");
			} catch(FileNotFoundException exc) {
			  con.log(exc);
			  con.serverError();
			  return;
			}
	    con.httpOutput(sampleHeader,loginContent);
		con.close();
	  }
	}
	if(arguments[0].equalsIgnoreCase("POST")) {
      if(arguments[1].equalsIgnoreCase("/login/")) {
		String loginData = new String(request.getContent());
		String [] data = loginData.split("&");
		String user = null, password = null;
		for(String keyValuePair : data) {
		  String key = keyValuePair.split("=")[0];
		  String value = keyValuePair.split("=")[1];
		  if(key.equals("user"))
		    user = value;
		   else if(key.equals("password"))
		     password = value;
		}
		if(user !=null && password!=null)
		  if(con.authenticate(user, password)) {
		    String confirmContent = null;
			try {
			  confirmContent = CachedFileGetter.getFile("samples/confirm.html");
			} catch(FileNotFoundException exc) {
			  con.log(exc);
			  con.serverError();
			  return;
			}
		    con.httpOutput(sampleHeader, confirmContent);
		  } else {
		    String failContent = null;
			try {
			  failContent    = CachedFileGetter.getFile("samples/fail.html");
			} catch(FileNotFoundException exc) {
			  con.log(exc);
			  con.serverError();
			  return;
			}
		     con.httpOutput(sampleHeader, failContent);
		     con.close();
		  }
	  }
	}
  }
}