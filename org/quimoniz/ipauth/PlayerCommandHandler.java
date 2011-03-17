package org.quimoniz.ipauth;

import org.bukkit.event.player.PlayerListener;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.entity.Player;

public class PlayerCommandHandler extends PlayerListener {
  public IpAuth main;
  public PlayerCommandHandler(IpAuth main) {
    this.main = main;
  }
  public void onPlayerLogin(PlayerLoginEvent e) {
    if(!main.isAllowed(e.getPlayer())) {
	  e.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Not whitelisted");
	  e.setResult(PlayerLoginEvent.Result.KICK_OTHER);
	}
  }
}