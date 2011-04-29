package org.quimoniz.ipauth;

import java.util.ArrayList;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;

public abstract class CSVParser {
  public void parseFile(String fileLocation) {
    FileReader file=null;
    //System.out.println((new java.io.File("/"+fileLocation)).exists()?"existiert":"existiert nicht");
    String curLine=null;
    boolean lineFeed=false;
    boolean inDoubleQuotes=false;
    StringBuffer data=new StringBuffer();
    int attributeNum=0;
    char lastChar=0;
    try {
      file=new FileReader(fileLocation);
    }catch(FileNotFoundException exc) {
      handleException(exc);
    }
    //if(file==null) return;
    BufferedReader input=null;
    try {
      input=new BufferedReader(file);
    } catch(Exception exc) {
      exc.printStackTrace();
    }

    try {
      curLine=input.readLine();
     } catch(IOException exc) {
       handleException(exc);
     }
    ArrayList<String> dataRecord=new ArrayList<String>();
    char cur=0;
    boolean quotesUnescape=true;
    while(curLine!=null) {

      if(!lineFeed)
        dataRecord=new ArrayList<String>();
       else
         data.append("\n");
      for(int i=0; i<curLine.length(); i++) {
        cur=curLine.charAt(i);
        //System.out.println("cur: '"+cur+"', data: \""+data+"\", inDoubleQuotes: "+(inDoubleQuotes?"true":"false")+", attributeNum: "+attributeNum+", lastChar:'"+lastChar+"'");
        if(data.length()==0)
          if(cur=='"') {
            if(inDoubleQuotes) {
              data.append('"');
              quotesUnescape=false;
            }
            inDoubleQuotes=true;
            lastChar=cur;
            continue;
           }
        if(cur==',' && (!inDoubleQuotes || (inDoubleQuotes && lastChar=='"'))) {
          //System.out.println("Data:"+data);
          if(inDoubleQuotes && lastChar=='"')
            if(data.length()>0)
              data.delete(data.length()-1,data.length());
          inDoubleQuotes=false;
          dataRecord.add(data.toString());
          data=new StringBuffer();
          attributeNum++;
        }else {
          if(cur=='"' && lastChar=='"'){
            if(inDoubleQuotes) {
              if(quotesUnescape)
                data.append(cur);
              quotesUnescape=!quotesUnescape;
              //unnecessary because it changes nothing
              //lastChar=cur;
              continue;
            }else data.append(cur);
          }else if(cur=='"' && !inDoubleQuotes) {
             data.append(cur);
            }else
              data.append(cur);
          }
        lastChar=cur;
      }
      if(inDoubleQuotes)
        if(lastChar=='"') {
          //System.out.println("Data:"+data);
          if(inDoubleQuotes && lastChar=='"')
            data.delete(data.length()-1,data.length());
          inDoubleQuotes=false;
          dataRecord.add(data.toString());
          processDataRecord(dataRecord.toArray(new String[0]));
          quotesUnescape=true;
          data=new StringBuffer();
          lastChar=0;
          attributeNum=0;
        }else
           lineFeed=true;
       else {
         lineFeed=false;
         if(data.length()>0)
           dataRecord.add(data.toString());
         processDataRecord(dataRecord.toArray(new String[0]));
         quotesUnescape=true;
         lastChar=0;
         //System.out.println("DataRecord: "+dataRecord.toString());
         data=new StringBuffer();
         attributeNum=0;
       }
      try {
        curLine=input.readLine();
       } catch(IOException exc) {
         handleException(exc);
       }
    }
    if(lineFeed)
      if(data.length()>0) {
        if(inDoubleQuotes && lastChar=='"')
          data.delete(data.length()-1,data.length());
        dataRecord.add(data.toString());
        processDataRecord(dataRecord.toArray(new String[0]));
        System.out.println("DataRecord: "+dataRecord.toString());
        data=new StringBuffer();
       }
    try {
      file.close();
    } catch(IOException exc) {
      handleException(exc);
    }
  }
  public abstract void handleException(Exception exc);
  public abstract void processDataRecord(String [] dataRecord);
  
}