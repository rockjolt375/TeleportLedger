package com.mythicacraft.teleportledger;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import ru.tehkode.permissions.events.PermissionEntityEvent;
import ru.tehkode.permissions.events.PermissionEntityEvent.Action;

import com.mythicacraft.teleportledger.utilities.ConfigAccessor;

/**
 * @author rockjolt375
 * @email mythicacraft@gmail.com
 * @category www.MythicaCraft.com
 */
public class TeleLedgerListener implements Listener{
	 /**
	  * Listens for public teleportation commands
	  * <p>
	  * If a player has teleport tokens, and typed any one of the publicly
	  * accessible commands for teleporting, the command would be carried out
	  * while subtracting one token to that players account.
	  * <p>
	  * If the account contains zero tokens, the command is cancelled and the
	  * player is notified of insufficient funds.
	  * 
	  * @param event 		Player command object being processed
	  */
	@EventHandler(priority = EventPriority.HIGHEST)
     public void onPlayerTeleport(final PlayerCommandPreprocessEvent event) {
		 String cmd = event.getMessage().toLowerCase();
		 if(cmd.contains("/modback")){
			 if(event.getPlayer().hasPermission("teleportledger.mod")){
				 event.setMessage("/back");
				 return;
			 }
			 else{
				 event.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to use this" +
				 		"command!");
				 event.setCancelled(true);
				 return;
			 }
		 }
		 else if(cmd.contains("/tptoken ") || cmd.equalsIgnoreCase("/tptoken")){
			 event.setMessage(cmd.replace("/tptoken", "/tptokens"));
			 return;
		 }
		 else if(cmd.contains("/tpignore ") || cmd.equalsIgnoreCase("/tpignore")){
			 event.setMessage(cmd.replace("/tpignore", "/tpblock"));
		 }
		 else if(!cmd.contains("/tpa") && !cmd.contains("/tpahere") && !cmd.contains("/back") &&
				 !cmd.contains("/tpback")) return;
		 
		 String player = event.getPlayer().getName();
		 if(getTeleCount(player) == 0){
			 event.setCancelled(true);
			 event.getPlayer().sendMessage(ChatColor.GOLD + "[TeleportLedger] " + ChatColor.RED +
					 "You dont have enough teleport tokens.");
			 return;
		 }
     }
	 
	 /**
	  * Listens for a player group change and modifies the ledger accordingly
	  * 
	  * @param event		Player group change event object
	  */
	@EventHandler(priority = EventPriority.MONITOR)
	 public void onPlayerGroupChange(final PermissionEntityEvent event){
		 if(event.getAction() == Action.RANK_CHANGED){
			Player player = (Player) event.getEntity();
			
			String group = TeleportLedger.perms.getPrimaryGroup(player);
			groupChangedUpdate(player, group);
		 }
	 }
	
	/**
	 * Inserts player into player.yml if player doesnt exist
	 * 
	 * @param event			Player join event object
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	 public void onPlayerJoin(final PlayerJoinEvent event){
		String player = event.getPlayer().getName().toString();
		ConfigAccessor playerData = new ConfigAccessor("players.yml");
		
		 if(!playerData.getConfig().contains(player)){
			 playerData.getConfig().set(player + ".amount", 0);
			 playerData.getConfig().set(player + ".ignoreAll", true);
			 playerData.getConfig().set(player + ".blockedPlayers", new ArrayList<String>());
			 playerData.saveConfig();
		 }
	}
	 
	 /**
	  * Modifies the player account file according to new group
	  * 
	  * @param player	Player currently being evaluated
	  * @param group		New group player was added to
	  */
	public void groupChangedUpdate(Player player, String group){
		 int count = getTeleCount(player.toString());
		 switch(group){
		 case "Member":
			 break;
		 case "Stone":
			 setTokenAmount(player.toString(), ++count);
			 break;
		 case "Coal":
			 setTokenAmount(player.toString(), count + 3);
			 break;
		 case "Iron":
			 setTokenAmount(player.toString(), count + 5);
			 break;
		 case "Gold":
			 setTokenAmount(player.toString(), count + 10);
			 break;
		 case "Diamond":
			 setTokenAmount(player.toString(), count + 15);
			 break;
		 case "Emerald":
			 setTokenAmount(player.toString(), count + 20);
			 break;
		 }
	 }
	 
	 /**
	  * Retrieves amount of teleport tokens to Player's account
	  * 
	  * @param player	Player currently being evaluated
	  * @return			returns string count of player tokens
	  */
	public int getTeleCount(String player){
		 ConfigAccessor playerData = new ConfigAccessor("players.yml");
		 return playerData.getConfig().getInt(player + ".amount");
	 }
	 
	 /**
	 * @param player	Player currently being evaluated
	 * @param tpCount	Int containing updated account balance
	 */
	public void setTokenAmount(String player, int tpCount){
		 ConfigAccessor playerData = new ConfigAccessor("players.yml");
		 playerData.getConfig().set(player + ".amount", tpCount);
		 playerData.saveConfig();
	 }
	
}
