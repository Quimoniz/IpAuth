package org.quimoniz.ipauth;

import java.util.LinkedList;

public class HttpRequest {
  private String requestLine;
  private HttpConnection con;
  private LinkedList<String> header = new LinkedList<String>();
  private boolean headBeenRead = false;
  private boolean contentBeenRead = false;
  private RequestHandler handler;
  private byte [] content = null;
  private Thread t = null;
  public HttpRequest(String requestLine, HttpConnection con) {
    this.requestLine = requestLine;
	this.con = con;
    handler = new RequestHandler(this,con);	
  }
  public String[] getHeader() {
    if(!headBeenRead) {
	  while(!con.hasHeadBeenRead()) {
	    try {
		  Thread.currentThread().sleep(5);
		} catch(InterruptedException exc) { }
	  }
	  headBeenRead = true;
	}
	return header.toArray(new String[0]);
  }
  public byte[] getContent() {
    if(!headBeenRead) getHeader();
	if(!contentBeenRead) {
	  while(!con.hasContentBeenRead()) {
	    try {
		  Thread.currentThread().sleep(5);
		} catch(InterruptedException exc) { }
	  }
	  contentBeenRead = true;
	}
	return content;
  }
  protected void setContent(byte[] byteArray) {
//    System.out.println(new String(byteArray));
    content = byteArray;
  }
  protected void addHeaderField(String field) {
    header.add(field);
  }
  public String getRequestLine() {
    return requestLine;
  }
  public void process() {
    handler.processRequest(requestLine);
  }
}