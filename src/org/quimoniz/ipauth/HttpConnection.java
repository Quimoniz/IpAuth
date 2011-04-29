
import java.net.Socket;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.BufferOverflowException;
import java.net.InetSocketAddress;

public class HttpConnection implements Runnable {
  private HttpListener main;
  private Socket myConnection;
  private Thread t;
  private boolean isRunning = false;
  private OutputStream   out = null;
  private InputStreamReader inReader  = null;
  private InputStream inStream = null;
//  private final int connectionTimeout = 2300;
  private final int BUFLENGTH = 1300;
  private HttpRequest request = null;
  private boolean halted = false;
  private boolean headBeenRead = false;
  private boolean contentBeenRead = false;
  public HttpConnection(Socket clientSocket, HttpListener main) {
    this.main = main;
	myConnection = clientSocket;
//	try {
	  t = new Thread(this);
	  isRunning = true;
	  t.start();
//	} catch(IOException exc) {
//	  exc.printStackTrace();
//	}
  }
  @Override public void run() {
    try {
      out           = myConnection.getOutputStream();
	  inStream      = myConnection.getInputStream ();
	  inReader      = new InputStreamReader(inStream,"ISO-8859-1");
	} catch(IOException exc) {
	  log(exc);
	}
	log("Initialized Connection with " + obfuscate(myConnection.getInetAddress().getHostAddress()) + ":" + myConnection.getPort());
	StringBuilder bufString = new StringBuilder();
	
	char [] cbuf = new char[BUFLENGTH];
	long charsRead = 0;
	int charReadCurrently = 0;
	int maxInputMs = 3000;
	long startTime = System.currentTimeMillis();
	int im8 = 0;
	boolean isReady = false;
	int crlf = 0; //will store previous reads of the CRLF pattern (Carriage Return, Line Feed)
	ByteBuffer bufByte = null;
	int contentLength = -1;
	long bytesRead = 0;
	byte [] bbuf = new byte[BUFLENGTH];
	do {
//	  charsReadCurrently = inReader.read(cbuf, 0, BUFLENGTH);
      try {
        isReady = inReader.ready();
	  } catch(IOException exc) { }
      if(isReady) {
	    if(crlf != 4) {
	      try {
		    charReadCurrently = inReader.read();
		    charsRead ++;
		  } catch(IOException exc) {
		    log(exc);
		  }
		  if(charReadCurrently == 13) {
		    if(crlf == 0)
		      crlf = 1;
		     else if(crlf == 2)
		       crlf = 3;
		  } else if(charReadCurrently == 10) {
		    if(crlf == 1) {
		      crlf = 2;
			  if(request == null)
			    request = new HttpRequest(bufString.toString(),this);
			   else {
			     request.addHeaderField(bufString.toString());
			     if(bufString.indexOf("Content-Length: ") == 0) { //We need to fetch this Header Entity for parsing the whole Request (if it includes a body), otherwise we can only get the Content-Length by the notion of the connection being closed
				   try {
				      contentLength = Integer.parseInt(bufString.substring(16).replaceAll("\r\n(\t| )",""));
//				      System.out.println("Read Content-Length: "+contentLength);
				   } catch(NumberFormatException exc) {
				     //INVALID contentLegth Value
				   } catch(NullPointerException exc) {
				     //Empty Content-Length Value
				   }
			     }
			   }
//			  System.out.println(bufString.toString());
			  bufString = new StringBuilder();
		    }
		    else if(crlf == 3) {
		      crlf = 4;
			  headBeenRead = true;
			  if(contentLength < 1)
			    break;
		    }
		  } else {
	        bufString.append((char)charReadCurrently);
		    crlf = 0;
		  }
		} else {
//		System.out.println("Attempting to read content");
		  if(bytesRead < contentLength) {
		    try {
		      charReadCurrently = inReader.read();
		    } catch(IOException exc) { log(exc); }
		    if(charReadCurrently != -1) {
		      if(bufByte == null) {
		        try {
		          bufByte = ByteBuffer.allocate(contentLength);
		        } catch(Exception exc) {
			      log(exc);
			    }
			  }
			  try {
			    byte[] currentByte = new byte []{(byte)charReadCurrently};
//			    System.out.print((char)charReadCurrently);
			    bufByte.put(currentByte);
			  } catch(Exception exc) {
//			    log(exc);
				exc.printStackTrace();
			  }
			  bytesRead ++;
		    }
		  } else {
		    contentBeenRead = true;
		    break;
		  }
		}
      }	else {
	    if(crlf == 4 && headBeenRead && bytesRead >= contentLength)
		  break;
		 else {
	      try {
            t.sleep(1);
		    im8 = 4;
		  } catch(InterruptedException exc) {} //I dont see any point in handling any exceptions here
		}
	  }
/*
	  //ToDo: Make it work for CRLF as well as for only LF
		if(currentChar == 10 || currentChar == 13) //NEWLINE!
		  
		 else 
*/
	  if(im8 == 8) {
	    im8 = 0;
		if((System.currentTimeMillis()-startTime) > maxInputMs)
		  break;
	  } else im8 ++;
//	  bufString.append(requestLine);
	} while(charReadCurrently != -1 && !contentBeenRead && !myConnection.isClosed());// && (contentLength < 1 || bytesRead >= contentLength)
/*
	System.out.println(
	"\ncharsRead:" + charsRead +
	"\ncharReadCurrently:" + charReadCurrently +
	"\nisReady:" + isReady +
	"\ncrlf:" + crlf +
	"\ncontentLength:" + contentLength +
	"\nheadBeenRead:" + headBeenRead +
	"\ncontentBeenRead:" + contentBeenRead
	);
*/
    contentBeenRead = true;
	if(bufByte != null && bufByte.hasArray())
	  request.setContent(bufByte.array());
	if(request != null)
	  request.process();
/*	
    while(isRunning) {
	  String requestLine = null;
	  try {
        requestLine = in.readLine();
//		System.out.println(requestLine);
	  } catch(IOException exc ) {
	    //We will assume that the connection got cut off or aborted, Immediate connection cutback
	  }
	  if(requestLine == null) {
	    close();
		break;
	  }
	  processRequest(requestLine);
	}
*/
  }
  public void halt() {
    halted = true;
    while(halted) {
	  System.out.print("#");
      try {
        t.sleep(500);
      } catch(InterruptedException exc) {
	    halted = false;
	  }
	}
  }
  public boolean isHalted() {
    if(((int)(Math.random()*40)) == 0)
      System.out.println("Returning halted state " +  halted);
    return halted;
  }
  public void continueReading() {
    if(isHalted()) {
	  System.out.println("Attempting to interrupt");
	  t.interrupt();
	} else System.out.println("Not halted!");
  }
  public void log(String line) {
    main.log(line);
  }
  public void log(Exception exc) {
    main.log(exc);
  }
  public String obfuscate(String toObfuscate) {
    return main.obfuscate(toObfuscate);
  }
  public boolean hasHeadBeenRead() {
    return headBeenRead;
  }
  public boolean hasContentBeenRead() {
    return contentBeenRead;
  }
  public boolean authenticate(String user, String password) {
    return main.authenticate(user, password, new InetSocketAddress(myConnection.getInetAddress().getHostAddress(), myConnection.getPort());
  }
  public void serverError() {
    try {
	  String errorContent = "<html>\n<head>\n<title>500 - Internal Server Error</title>\n</head>\n<body>\n<h1>500 - Internal Server Error</h1>\n</body>\n</html>";
	  String errorHeader = "HTTP/1.1 500 InternalServerError\r\nContent-Type: text/html\r\nPragma: no-cache\r\nCache-Control: no-cache\r\nContent-Length:" + errorContent.length() + "\r\nConnection: Close\r\n\r\n";
      out.write(errorHeader.getBytes());
	  out.write(errorContent.getBytes());
	  out.flush();
	  close();
	} catch(IOException exc) {
	  log(exc);
	}
  }
  public void httpOutput(String header, String content) {
    try {
	  if(header != null) {
	    if(content != null && content.length() > 0)
	      if(header.indexOf("Content-Length:") == -1)
		    header += "\r\nContent-Length: " + content.length();
	    header += "\r\n\r\n";
	    out.write(header.getBytes());
	  }
	  if(content != null) out.write(content.getBytes());
	  out.flush();
	} catch(IOException exc) {
	  log(exc);
	  serverError();
	}
  }
  public void close() {
    isRunning = false;
	try {
	  out.close();
	  inReader.close();
	  inStream.close();
	  myConnection.close();
	} catch(IOException exc) {
	  log(exc);
	}
  }
}