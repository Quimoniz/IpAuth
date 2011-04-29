package org.quimoniz.ipauth;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Type;
import org.bukkit.event.Event.Priority;
//import org.bukkit.plugin.PluginManager;

import java.io.IOException;

import java.io.FileInputStream;
import java.util.Properties;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.io.FileOutputStream;
import java.io.File;


public class IpAuth extends JavaPlugin {
  PlayerHandler playerListener = null;
  private final String propertiesFileName = "ipauth.properties";
  private HttpListener server = null;
  private FileOutputStream loggingStream = null;
  protected Properties myProperties = null;
  private AuthStore authentifications;
  private File myFolder;
  public void onEnable() {
	myFolder = this.getFile();
	if(!myFolder.exists())
      myFolder.mkdir();
    try {
	  loggingStream = new FileOutputStream(myFolder.getPath() + "/ipauth.log", true);
	} catch(IOException exc) {
	  exc.printStackTrace();
	}
    myProperties = new Properties();
    FileInputStream propertiesInputStream = null;
    try {
      propertiesInputStream = new FileInputStream(myFolder.getPath() + "/" + propertiesFileName);
      myProperties.load(propertiesInputStream);
    } catch(IOException exc) {
      log(exc,true);
    }
    if(propertiesInputStream == null) {
      myProperties.setProperty("valid-auth-duration","7200");
      myProperties.setProperty("http-port","5009");
      myProperties.setProperty("kick-message","Not whitelisted!");
    }
    playerListener = new PlayerHandler(this);
    this.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
    this.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_LOGIN, playerListener, Priority.Normal, this);
    int serverPort = 5009;
    try {
      serverPort = Integer.parseInt(myProperties.getProperty("http-port"));
    } catch(NumberFormatException exc) {
      log(exc,true);
    }
	server = new HttpListener(serverPort, this);
	server.start();
    authentifications = new AuthStore(myProperties.getProperty("auth-csv", myFolder.getPath() + "/auth-data.csv"));
  }
  public void onDisable() {
    server.stop();
    authentifications.save();
	try {
	  loggingStream.close();
	} catch(IOException exc) { }
	loggingStream = null;
    myProperties = null;
  }
  @Override public boolean onCommand(CommandSender sender, Command invocation, String label, String [] args) {
    Player p = null;
	if(sender instanceof Player)
	  p = (Player) sender;
//	  System.out.println(p.getDisplayName() + " invoked " + invocation.getName() + ". Label: " + label + ". args: " + java.util.Arrays.toString(args));
	if(p!= null && args.length == 1) {
	  String rawCommand = invocation.getName().toLowerCase();
	  if(rawCommand.equals("regauth")) {
        String user = p.getName();
        String password = server.obfuscate(args[0]);
	    authentifications.add(new AuthData(user, password));
        sender.sendMessage("Registered Password for " + user);
	    return true;
	  } else if(rawCommand.equals("onlinemode")) {
		if(p.isOp()) {
		  String param = args[0].toLowerCase();
		  if(param.equals("on") || param.equals("true") || param.equals("1") || param.equals("active") || param.equals("activated") || param.equals("yes") || param.equals("y")) {
		    setOnlineMode(true);
		    sender.sendMessage("Setting online mode to true successfull");
		    return true;
		  } else if(param.equals("off") || param.equals("false") || param.equals("0") || param.equals("unactive") || param.equals("deactivated") || param.equals("no") || param.equals("n")) {
		    setOnlineMode(false);
		    sender.sendMessage("Setting online mode to false successfull");
		    return true;
		  } else {
			sender.sendMessage("Failed to set online mode");
			return false;
		  }
		} else {
	      return false;
		}
	  } else {
		return false;
	  }
	} else {
	  if(p != null)
		if(args.length > 1)
          sender.sendMessage("More then 1 parameter: " + java.util.Arrays.toString(args));
		else if(args.length == 0)
		  sender.sendMessage("No paramater given!");
	  return false;
    }
  }

  public void log(Exception exc, boolean severe) {
    StackTraceElement [] exceptionTrace = ((Throwable) exc).getStackTrace();
    StringBuilder buf = new StringBuilder(exceptionTrace.length * 20); //We will just try to guess the needed capacity
	for(int i = 0; i < exceptionTrace.length; i++) {
      if(i > 0) buf.append("\n  ");
	  buf.append(exceptionTrace[i].toString());
    }
	log(buf.toString(), severe);
  }
  public void log(String line, boolean severe) {
    try {
      loggingStream.write((line + "\n").getBytes());
	} catch(IOException exc) {}
    if(severe)
      System.out.println(line);
  }
  public void log(Exception exc) {
    log(exc,false);
  }
  public void log(String line) {
    log(line,false);
  }
  //authenticate is being called by the http server
  public boolean authenticate(String user, String password, InetSocketAddress address) {
    password = server.obfuscate(password);
    AuthData data = authentifications.get(user);
    if(data != null && data.getPassword().equals(password)) {
      authentifications.add(new AuthData(user,password,address.getAddress().getAddress(),(long)(System.currentTimeMillis()/1000)));
      Player pOnline = this.getServer().getPlayer(user);
      if(pOnline != null)
    	if(!pOnline.getAddress().getAddress().equals(address.getAddress().getAddress()))
    	  pOnline.kickPlayer("IP adress doesn't match with authentificated one");
      return true;
    } else
      return false;
  }
  public int getValidAuthDuration() {
    int duration = 7200;
    if(myProperties != null) {
      try {
        duration = Integer.parseInt(myProperties.getProperty("valid-auth-duration"));
      } catch(NumberFormatException exc) {
        log(exc);
      }
    }
    return duration;
  }
  public void schedulePlayerCheck(Player p) {
	this.getServer().getScheduler().scheduleAsyncDelayedTask(this, new ScheduledAuthChecker(p.getName(),this), 1);
	
  }
  public boolean isAllowed(Player p) {
	if(getOnlineMode()) {
	  return true;
	}
    InetSocketAddress socketAddress = p.getAddress();
    InetAddress address = socketAddress.getAddress();
    String name = p.getName();
    AuthData data = authentifications.get(name);
    long currentTime = (long)(System.currentTimeMillis()/1000);
    if(data != null && data.getUserName().equals(name) && (data.getAuthTime() + getValidAuthDuration()) > currentTime) {
      boolean sameIp = false; 
      try {
    	sameIp = InetAddress.getByAddress(data.getIp()).equals(address);
      } catch(java.net.UnknownHostException exc) {
    	log(exc);
      }
      if(sameIp)
        return true;
      else
    	return false;
    }else
      return false;
  }
  private void setOnlineMode(boolean onlineMode) {
	((org.bukkit.craftbukkit.CraftServer)this.getServer()).getServer().onlineMode = onlineMode;
  }
  private boolean getOnlineMode() {
    return ((org.bukkit.craftbukkit.CraftServer)this.getServer()).getServer().onlineMode;
  }
  private void setPlayerName(Player p, String newName) {
	  ((org.bukkit.craftbukkit.entity.CraftPlayer) p).getHandle().name = newName;
  }
}
class ScheduledAuthChecker implements Runnable {
  private String playerName;
  private IpAuth plugin;
  public ScheduledAuthChecker(String playerName, IpAuth plugin) {
	this.playerName = playerName;
	this.plugin = plugin;
  }
  @Override public void run() {
	Player player = plugin.getServer().getPlayer(playerName);
	if(!plugin.isAllowed(player))
	  player.kickPlayer(plugin.myProperties.getProperty("kick-message"));
  }
}