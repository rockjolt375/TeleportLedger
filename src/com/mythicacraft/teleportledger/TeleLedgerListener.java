package com.mythicacraft.teleportledger;

import java.util.ArrayList;
import java.util.List;

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
	
	TeleportLedger plugin;
	
	public TeleLedgerListener(TeleportLedger plugin){
		this.plugin = plugin;
	}
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
			 return;
		 }
		 else if(!cmd.contains("/tpa ") && !cmd.contains("/tpahere") && !cmd.contains("/back") &&
				 !cmd.contains("/tpback") || event.getPlayer().hasPermission("teleportLedger.exempt")) return;
		 
		 String player = event.getPlayer().getName().toString();
		 
		 if(getTeleCount(player) == 0){
			 event.setCancelled(true);
			 event.getPlayer().sendMessage(ChatColor.GOLD + "[TeleportLedger] " + ChatColor.RED +
						 "You dont have enough teleport tokens.");
			 return;
		 }
		 if(cmd.contains("/back")){
			 setTokenAmount(player, getTeleCount(player) - 1);
			 event.getPlayer().sendMessage(ChatColor.GOLD + "You have " + ChatColor.BLUE + getTeleCount(player) +
						 ChatColor.GOLD + " tokens left in your account.");
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
			 playerData.getConfig().set(player + ".amount", plugin.getConfig().getInt("startingAmount"));
			 playerData.getConfig().set(player + ".ignoreAll", false);
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
		 List<String> groups = getGroups();
		 
		 for(int i=0; i<groups.size();i++){
			 if(groups.get(i).equalsIgnoreCase(group)){
				 setTokenAmount(player.getName().toString(), count +
						 plugin.getConfig().getInt(groups.get(i)));
			 }
		 }
	 }
	
	
	/**
	 * Returns list of groups defined in config.yml
	 * 
	 * @return 		Returns list of groups
	 */
	private List<String> getGroups(){
		return plugin.getConfig().getStringList("Groups");
	}
	 
	 /**
	  * Retrieves amount of teleport tokens to Player's account
	  * 
	  * @param player	Player currently being evaluated
	  * @return			returns string count of player tokens
	  */
	private int getTeleCount(String player){
		 ConfigAccessor playerData = new ConfigAccessor("players.yml");
		 return playerData.getConfig().getInt(player + ".amount");
	 }
	 
	 /**
	 * @param player	Player currently being evaluated
	 * @param tpCount	Int containing updated account balance
	 */
	private void setTokenAmount(String player, int tpCount){
		 ConfigAccessor playerData = new ConfigAccessor("players.yml");
		 playerData.getConfig().set(player + ".amount", tpCount);
		 playerData.saveConfig();
	 }
	
}
