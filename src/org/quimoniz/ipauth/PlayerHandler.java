package org.quimoniz.ipauth;

import org.bukkit.event.player.PlayerListener;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.entity.Player;

public class PlayerHandler extends PlayerListener {
  public IpAuth main;
  public PlayerHandler(IpAuth main) {
    this.main = main;
  }
  public void onPlayerLogin(PlayerLoginEvent e) {
	main.schedulePlayerCheck(e.getPlayer());
/*
    System.out.println("EVENT LOGIN");
	  System.out.println("Checking if new user is authenticated (LOGIN).");
    if(!main.isAllowed(p)) {
	  e.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Not whitelisted");
	  e.setResult(PlayerLoginEvent.Result.KICK_OTHER);
	}
*/
  }
 
  public void onPlayerJoin(PlayerLoginEvent e) {
//	main.schedulePlayerCheck(e.getPlayer());

	System.out.println("EVENT JOIN");
/* 	if(!main.isAllowed(e.getPlayer())) {
	  e.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Not whitelisted");
	  e.setResult(PlayerLoginEvent.Result.KICK_OTHER);
	}
*/
  }
}