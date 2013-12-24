package com.mythicacraft.teleportledger;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import ru.tehkode.permissions.events.PermissionEntityEvent;
import ru.tehkode.permissions.events.PermissionEntityEvent.Action;

import com.mythicacraft.teleportledger.utilities.ConfigAccessor;

public class TeleLedgerListener implements Listener{
	
	TeleportLedger plugin;
	
	public TeleLedgerListener(TeleportLedger plugin){
		this.plugin = plugin;
	}
	

	 @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerTeleport(final PlayerTeleportEvent event) {
		 String player = event.getPlayer().toString();
		 
		 int count = getTeleCount(player);
		 
		 if(count == 0){
			 event.setCancelled(true);
			 event.getPlayer().sendMessage(ChatColor.RED + "I'm sorry, you do not have enough teleport points.");
			 return;
		 }
		 updateConfig(player, --count);
     }
	 
	 @EventHandler(priority = EventPriority.MONITOR)
	 public void onPlayerGroupChange(final PermissionEntityEvent event){
		 if(event.getAction() == Action.RANK_CHANGED){
			Player player = (Player) event.getEntity();
			
			String group = TeleportLedger.perms.getPrimaryGroup(player);
			groupChangedUpdate(player, group);
		 }
	 }
	 
	 public void groupChangedUpdate(Player player, String group){
		 int count = getTeleCount(player.toString());
		 switch(group){
		 case "Member":
			 break;
		 case "Stone":
			 updateConfig(player.toString(), ++count);
			 break;
		 case "Coal":
			 updateConfig(player.toString(), count + 3);
			 break;
		 case "Iron":
			 updateConfig(player.toString(), count + 5);
			 break;
		 case "Gold":
			 updateConfig(player.toString(), count + 10);
			 break;
		 case "Diamond":
			 updateConfig(player.toString(), count + 15);
			 break;
		 case "Emerald":
			 updateConfig(player.toString(), count + 20);
			 break;
		 }
	 }
	 
	 public int getTeleCount(String player){
		 int count = 0;
		 
		 ConfigAccessor playerData = new ConfigAccessor("player.yml");
		 
		 if(playerData.getConfig().getString(player) == null){
			 playerData.getConfig().addDefault(player, 0);
			 return 0;
		 }
		 count = Integer.parseInt(playerData.getConfig().getString(player));
		 return count;
	 }
	 
	 public void updateConfig(String player, int tpCount){
		 ConfigAccessor playerData = new ConfigAccessor("player.yml");
		 
		 playerData.getConfig().set(player, tpCount);
		 playerData.saveConfig();
	 }
	
}
