package org.quimoniz.ipauth;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Type;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginManager;


import org.bukkit.event.player.PlayerListener;

import java.net.ServerSocket;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.util.LinkedList;
import java.util.Iterator;


public class IpAuth extends JavaPlugin {
//  PlayerCommandHandler playerListener;
  private ServerSocket webSocket = null;
  private boolean isRunning = false;
  private Thread t = null;
  private LinkedList<HttpConnection> conList = new LinkedList<HttpConnection>();
  private HttpServer server = null;
  private FileOutputStream loggingStream;
  public void onEnable() {
    try {
	  loggingStream = new FileOutputStream("ipauth.log", true);
	} catch(IOException exc) {
	  exc.printStackTrace();
	}
    playerListener = new PlayerCommandHandler(this);
    this.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
//    this.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_LOGIN, playerListener, Priority.Normal, this);
//	this.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_COMMAND, playerListener, Priority.Normal, this);
    //PluginManager pm = this.getServer().getPluginManager();
	//pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Priority.Normal, this);´
	server = new HttpListener(5009,this);
  }
  public void onDisable() {
    server.stop();
  }
/*
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if(sender == null) 
	  return false;
    Player player = (Player) sender;
	String call = command.getName();
//	if(call.equalsIgnoreCase("regAuth"))
	  System.out.println(player.toString() + " called " + call + ". Label: " + label + ". args: "+java.util.Arrays.toString(args));
	return true;
  }
*/
  public void log(Exception exc) {
    StackTraceElement [] exceptionTrace = ((Throwable) exc).getStackTrace();
    StringBuilder buf = new StringBuilder(exceptionTrace.length * 20); //We will just try to guess the needed capacity
	for(int i = 0; i < exceptionTrace.length; i++)
	  buf.append(exceptionTrace[i].toString());
	log(buf.toString());
  }
  public void log(String line) {
    try {
      loggingStream.write((line + "\n").getBytes());
	} catch(IOException exc) {}
  }
  @Override public boolean onCommand(CommandSender sender, Command invocation, String label, String [] args) {
    Player p = null;
	if(sender instanceof Player)
	  p = (Player) sender;
	if(p != null) {
	  System.out.println(p.getDisplayName() + " invoked " + invocation.getName() + ". Label: " + label + ". args: " + java.util.Arrays.toString(args));
	  if(args.length == 1) {
	    return true;
	  } else
	    return false;
	}else
	   System.out.println("NPE!");
	return false;
  }
  public void newClient(Socket conSocket) {
    conList.add(new HttpConnection(conSocket,this));
  }
}