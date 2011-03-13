package org.quimoniz.ipauth;

import org.bukkit.event.player.PlayerListener;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.entity.Player;

public class PlayerCommandHandler extends CraftPlayer {
  public IpAuth main;
  public PlayerCommandHandler(IpAuth main) {
    this.main = main;
  }
/*
  public boolean onCommand(CommandSender sender, Command invocation, String label, String [] args) {
    Player p = null;
	if(sender instanceof Player)
	  p = (Player) sender;
	if(p != null)
	  System.out.println(p.getDisplayName() + " invoked " + invocation.getName() + ". Label: " + label + ". args: " + java.util.Arrays.toString(args));
	 else
	   System.out.println("NPE!");
	return true;
  }
*/
  public void onPlayerChat(PlayerChatEvent e) {
    System.out.println(e.getMessage());
  }
}