import java.security.MessageDigest;

public class RequestHandler {
  private HttpRequest request;
  private HttpConnection con;
  public RequestHandler(HttpRequest request, HttpConnection con) {
    this.request = request;
	this.con = con;
  }
  public void processRequest(String requestLine) {
    String loginContent = null, confirmContent = null, failContent = null;
//	System.out.println(java.util.Arrays.toString(request.getBytes()) + request);
	try {
	  loginContent   = CachedFileGetter.getFile("samples/login.html");
	  confirmContent = CachedFileGetter.getFile("samples/confirm.html");
	  failContent    = CachedFileGetter.getFile("samples/fail.html");
	} catch(java.io.FileNotFoundException exc) {
	  con.log(exc);
	  con.serverError();
	  return;
	}
	final String sampleHeader = "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\nPragma: no-cache\r\nCache-Control: no-cache\r\nConnection: Close";

     String[] arguments = requestLine.split(" ");
	if(arguments.length<2) return;
//    System.out.println("Processing Request: " + requestLine);
//    System.out.println(java.util.Arrays.toString(request.getHeader()));
	if(arguments[0].equalsIgnoreCase("GET")) {
	  if(arguments[1].equalsIgnoreCase("/")) {
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
		  if(con.authenticate(user, password))
		    con.httpOutput(sampleHeader, confirmContent);
		   else
		     con.httpOutput(sampleHeader, failContent);
		con.close();
	  }
	}
  }
}