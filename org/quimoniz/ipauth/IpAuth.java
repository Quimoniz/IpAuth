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

public class IpAuth extends JavaPlugin {
  PlayerCommandHandler playerListener;
  public void onEnable() {
    playerListener = new PlayerCommandHandler(this);
//    this.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
//    this.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_LOGIN, playerListener, Priority.Normal, this);
//	this.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_COMMAND, playerListener, Priority.Normal, this);
    PluginManager pm = this.getServer().getPluginManager();
	pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Priority.Normal, this);
  }
  public void onDisable() {
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
  @Override public boolean onCommand(CommandSender sender, Command invocation, String label, String [] args) {
    Player p = null;
	if(sender instanceof Player)
	  p = (Player) sender;
	if(p != null)
	  System.out.println(p.getDisplayName() + " invoked " + invocation.getName() + ". Label: " + label + ". args: " + java.util.Arrays.toString(args));
	 else
	   System.out.println("NPE!");
	return false;
  }
  @Override public  PluginCommand getCommand(String name) {
    System.out.println("getCommand called with: " + name);
//    return new PluginCommand(name,this);
    return null;
  }
}