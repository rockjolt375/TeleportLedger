package com.mythicacraft.teleportledger;

import java.util.List;
import java.util.Stack;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import com.mythicacraft.teleportledger.TeleportLedger.TeleportType;
import com.mythicacraft.teleportledger.utilities.ConfigAccessor;

public class TeleCmd implements CommandExecutor{

	TeleportLedger plugin;
	
	public TeleCmd(TeleportLedger plugin){
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		String senderName = sender.getName().toString();
		Player senderPlayer = Bukkit.getPlayer(senderName);
		ConfigAccessor playerData = new ConfigAccessor("players.yml");
		
		if(!sender.hasPermission("teleportledger.use")){
			sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
			return true;
		}
		///////////////Begin /tptokens command/////////////////////
		if(commandLabel.equalsIgnoreCase("tptokens")){
		
			if(!sender.hasPermission("teleportledger.mod") || args.length < 1){
				int count = playerData.getConfig().getInt(senderName + ".amount");
				sender.sendMessage(ChatColor.GREEN + "You currently have " + ChatColor.BLUE + count +
						ChatColor.GREEN + " teleport token(s).");
				return true;
			}
			else if(args.length == 1){
				int count = playerData.getConfig().getInt(completeName(args[0]) + ".amount");
				sender.sendMessage(ChatColor.GREEN + "Player " + ChatColor.BLUE + args[0] +
						ChatColor.GREEN + " currently has " + ChatColor.BLUE + count +
						ChatColor.GREEN + " token(s) in their account.");
				return true;
			}
			else if(args.length == 3 && (args[0].equalsIgnoreCase("modify") || args[0].equalsIgnoreCase("set"))){
				if(!isPlayerInConfig(completeName(args[1]))){
					sender.sendMessage(ChatColor.GOLD + "Player " + ChatColor.BLUE + args[1] + ChatColor.GOLD +
							" was not found, perhaps they have not joined the server yet.");
					return true;
				}
				if(!isNumber(args[2])){ sender.sendMessage(ChatColor.RED + "Invalid Number, " +
						"type /tptokens set <player> <amount> to set token amount"); return true;
				}
				String playerName = completeName(args[1]);
				if(playerName == null){ sender.sendMessage(ChatColor.GOLD + "Player not found."); return true;}
				int count = 0;
				if(args[0].equalsIgnoreCase("modify"))
					count = modifyTokenAmount(playerName, Integer.parseInt(args[2]));
				else 
					count = setTokenAmount(playerName, Integer.parseInt(args[2]));
				sender.sendMessage(ChatColor.GOLD + "Player " + ChatColor.BLUE + playerName + ChatColor.GOLD + " now has " +
						ChatColor.BLUE + count + ChatColor.GOLD + " in their account.");
				return true;
			}
			else{
				sender.sendMessage(ChatColor.RED + "That is not a valid use of this command!");
				return true;
			}
		}
		///////////////////////Begin /back///////////////////////////
		if(commandLabel.equalsIgnoreCase("back")){
			if(args.length == 1 && args[0].equalsIgnoreCase("again")){
				if(plugin.backHistoryMap.containsKey(senderPlayer)){
					if(!plugin.backHistoryMap.get(senderPlayer).isEmpty()){
						plugin.previousLocationMap.put(senderPlayer, senderPlayer.getLocation());
						senderPlayer.teleport(plugin.backHistoryMap.get(senderPlayer).pop());
						return true;
					}
				}
				sender.sendMessage(ChatColor.GOLD + "You have no previous locations stored.");
				return true;
			}
			if(plugin.previousLocationMap.containsKey(senderPlayer)){
				Location location = plugin.previousLocationMap.get(senderPlayer);
				plugin.previousLocationMap.put(senderPlayer, senderPlayer.getLocation());
				senderPlayer.teleport(location);
				return true;
			}
			else
				sender.sendMessage(ChatColor.GOLD + "You have no previous locations stored.");
			return true;
		}
		/////////////////////Begin /tpa, /tpask, /tpcall////////////////////////
		if(commandLabel.equalsIgnoreCase("tpa") || commandLabel.equalsIgnoreCase("tpask") ||
				commandLabel.equalsIgnoreCase("call")){
			if(args.length != 1){ sender.sendMessage(ChatColor.BLUE + "To request teleportation, use" +
					"/tpa <player>"); return true;}
			if(plugin.cooldownList.contains(senderPlayer)){sender.sendMessage(ChatColor.BLUE + "You must wait " + 
					"5 seconds between each teleport request."); return true;
			}
			String playerName = completeName(args[0]);
			if(playerName == null){ sender.sendMessage(ChatColor.GOLD + "Player not found."); return true;}
			Player requestee = isOnline(sender, playerName);
			if(requestee == null || isBlocked(senderName, playerName) || isSelf(senderName, playerName)) return true;
			if(isIgnoring(requestee.getName().toString())){
				sender.sendMessage(ChatColor.GOLD + "That player is not accepting TP requests at this time!");
				return true;
			}
			plugin.teleportRequests.put(requestee.getName().toString(), new TeleportRequest(senderPlayer,
					requestee, TeleportType.TPA));
			requestee.sendMessage(ChatColor.AQUA + "Player " + ChatColor.GOLD + sender.getName().toString() +
					ChatColor.AQUA + " has requested to teleport to your location. To accept, type " +
					"\"/tpaccept\". To refuse, type \"/tpdeny\".");
			timeout(requestee.getName().toString());
			sender.sendMessage(ChatColor.AQUA + "You have requested to teleport to player " + ChatColor.GOLD +
					requestee.getName().toString() + ChatColor.AQUA + ". They have 10 seconds to accept your request.");
			plugin.cooldownList.add(senderPlayer);
			cooldown(senderPlayer);
			return true;
		}
		/////////////////////Begin /tpahere/////////////////////////
		if(commandLabel.equalsIgnoreCase("tpahere")){
			if(args.length != 1){ sender.sendMessage(ChatColor.BLUE + "To request teleportation, use /tpahere" +
					"<player>"); return true;}
			if(plugin.cooldownList.contains(senderPlayer)){sender.sendMessage(ChatColor.BLUE + "You must wait " + 
					"5 seconds between each teleport request."); return true;
			}
			String playerName = completeName(args[0]);
			if(playerName == null){ sender.sendMessage(ChatColor.GOLD + "Player not found."); return true;}
			Player requestee = isOnline(sender, playerName);
			if(requestee == null || isBlocked(senderName, playerName) || isSelf(senderName, playerName)) return true;
			if(isIgnoring(requestee.getName().toString())){
				sender.sendMessage(ChatColor.GOLD + "That player is not accepting TP requests at this time!");
				return true;
			}
			plugin.teleportRequests.put(requestee.getName().toString(), new TeleportRequest(senderPlayer,
					requestee, TeleportType.TPAHERE));
			requestee.sendMessage(ChatColor.AQUA + "Player " + ChatColor.GOLD + sender.getName().toString() +
					ChatColor.AQUA + " has requested to teleport you to their location. To accept, type " +
					"\"/tpaccept\". To refuse, type \"/tpdeny\".");
			timeout(requestee.getName().toString());
			sender.sendMessage(ChatColor.AQUA + "You have requested player " + ChatColor.GOLD + requestee.getName().toString() + 
					ChatColor.AQUA + " to teleport to you. They have 10 seconds to accept your request.");
			plugin.cooldownList.add(senderPlayer);
			cooldown(senderPlayer);
			return true;
		}
		//////////////////////Begin /tpaccept/////////////////////////
		if(commandLabel.equalsIgnoreCase("tpaccept")){
			TeleportRequest request = plugin.teleportRequests.get(senderName);
			if(request == null){
				sender.sendMessage(ChatColor.GOLD + "There are no pending teleportation requests at this time.");
				return true;
			}
			if(request.getType() == TeleportType.TPA){
				addToBackMaps(request.getOwner(), request.getOwner().getLocation());
				request.getOwner().teleport(request.getRequestee().getLocation());
			}
			else{
				addToBackMaps(request.getRequestee(), request.getRequestee().getLocation());
				request.getRequestee().teleport(request.getOwner().getLocation());
			}
			if(!request.getOwner().hasPermission("teleportledger.exempt")){
				request.getOwner().sendMessage(ChatColor.GOLD + "[TeleportLedger] " + ChatColor.GREEN + "You now have " +
						ChatColor.BLUE + modifyTokenAmount(request.getOwnerName(), -1) + ChatColor.GREEN +
						" teleport tokens in your account.");
			}
			plugin.teleportRequests.remove(senderName);
			return true;
		}
		/////////////////////Begin /tpdeny///////////////////////////
		if(commandLabel.equalsIgnoreCase("tpdeny")){
			TeleportRequest request = plugin.teleportRequests.get(senderName);
			if(request == null){
				sender.sendMessage(ChatColor.GOLD + "There are no pending teleportation requests at this time.");
				return true;
			}
			request.getOwner().sendMessage(ChatColor.GOLD + "Player " + ChatColor.BLUE + request.getRequesteeName() +
					ChatColor.GOLD + " has denied your request, no charges have been made.");
			sender.sendMessage(ChatColor.GOLD + "You have denied the teleport request.");
			plugin.teleportRequests.remove(senderName);
			return true;
		}
		////////////////////Begin /tpignoreall///////////////////////////
		if(commandLabel.equalsIgnoreCase("tpignoreall")){
			if(isIgnoring(senderName)){
				playerData.getConfig().set(senderName + ".ignoreAll", false);
				sender.sendMessage(ChatColor.AQUA + "You are now accepting teleport requests.");
			}
			else{
				playerData.getConfig().set(senderName + ".ignoreAll", true);
				sender.sendMessage(ChatColor.AQUA + "You are no longer accepting teleport requests.");
			}
			playerData.saveConfig();
			return true;
		}
		///////////////////Begin /tpblock/////////////////////////
		if(commandLabel.equalsIgnoreCase("tpblock")){
			if(args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("list"))){
				List<String> list = getBlockedList(senderName);
				String message = null;
				for(int i=0;i<list.size();i++){
					message +=  ", " + list.get(i);
				}
				if(message == null){
					sender.sendMessage(ChatColor.GOLD + "You have not blocked any players.");
					return true;
				}
				sender.sendMessage(ChatColor.GOLD + "You have blocked: " + ChatColor.WHITE + 
						message.substring(6));
				return true;
			}
			List<String> blockedPlayers = playerData.getConfig().getStringList(senderName + ".blockedPlayers");
			String playerName = completeName(args[0]);
			if(playerName == null){ sender.sendMessage(ChatColor.GOLD + "Player not found."); return true;}
			if(blockedPlayers.contains(playerName)){
				sender.sendMessage(ChatColor.GOLD + "You have already blocked that player!");
				return true;
			}
			blockedPlayers.add(playerName);
			setBlockedPlayers(senderName, blockedPlayers);
			sender.sendMessage(ChatColor.AQUA + "Player " + ChatColor.GOLD + playerName + ChatColor.AQUA +
					" is now blocked. You will no longer receive TP requests from them.");
			return true;
		}
		//////////////////Begin /tpunblock////////////////////////
		if(commandLabel.equalsIgnoreCase("tpunblock")){
			if(args.length != 1){ sender.sendMessage(ChatColor.BLUE + "To unblock tp requests from a specific player" + 
					", use /tpunblock <player>"); return true;}
			List<String> blockedPlayers = playerData.getConfig().getStringList(senderName + ".blockedPlayers");
			String playerName = completeName(args[0]);
			if(playerName == null){ sender.sendMessage(ChatColor.GOLD + "Player not found."); return true;}
			if(!blockedPlayers.contains(playerName)){
				sender.sendMessage(ChatColor.GOLD + "You have not blocked that player!");
				return true;
			}
			blockedPlayers.remove(playerName);
			setBlockedPlayers(senderName, blockedPlayers);
			sender.sendMessage(ChatColor.AQUA + "Player " + ChatColor.GOLD + playerName + ChatColor.AQUA +
					" is now unblocked. You can now receive TP requests from them.");
			return true;
		}
		return false;
	}
	
	private void setBlockedPlayers(String sender, List<String> list){
		ConfigAccessor playerData = new ConfigAccessor("players.yml");
		playerData.getConfig().set(sender + ".blockedPlayers", list);
		playerData.saveConfig();
	}
	
	private int modifyTokenAmount(String player, int count){
		ConfigAccessor playerData = new ConfigAccessor("players.yml");
		if(count != 0){
			int tokens = playerData.getConfig().getInt(player + ".amount");
			count += tokens;
		}
		playerData.getConfig().set(player + ".amount", count);
		playerData.saveConfig();
		
		return count;
	}
	
	private int setTokenAmount(String player, int count){
		ConfigAccessor playerData = new ConfigAccessor("players.yml");
		playerData.getConfig().set(player + ".amount", count);
		playerData.saveConfig();
		return count;
	}
	
	private boolean isBlocked(String sender, String requestee){
		ConfigAccessor playerData = new ConfigAccessor("players.yml");
		List<String> blockedPlayers = playerData.getConfig().getStringList(requestee + ".blockedPlayers");
		if(blockedPlayers.contains(sender)){
			Bukkit.getPlayer(sender).sendMessage(ChatColor.GOLD + "That player is not accepting TP requests "+
					"at this time!");
			return true;
		}
		return false;
	}
	
	private List<String> getBlockedList(String sender){
		ConfigAccessor playerData = new ConfigAccessor("players.yml");
		return playerData.getConfig().getStringList(sender + ".blockedPlayers");
	}
	
	private boolean isIgnoring(String requestee){
		ConfigAccessor playerData = new ConfigAccessor("players.yml");
		return playerData.getConfig().getBoolean(requestee + ".ignoreAll");
	}
	
	private boolean isNumber(String num){
		try{
			Integer.parseInt(num);
			return true;
		} catch(Exception e){return false;}
	}
	
	private Player isOnline(CommandSender sender, String player){
		Player requestee = Bukkit.getPlayer(player);
		if(requestee == null){sender.sendMessage(ChatColor.GOLD + "[TeleportLedger] " + ChatColor.RED + "That" +
				"player is not online!"); return null;}	
		return requestee;
	}
	
	private boolean isPlayerInConfig(String player){
		ConfigAccessor playerData = new ConfigAccessor("players.yml");
		return playerData.getConfig().contains(player);
	}
	
	private void addToBackMaps(Player sender, Location location){

		if(plugin.backHistoryMap.containsKey(sender)){
			plugin.backHistoryMap.get(sender).push(location);
			plugin.previousLocationMap.put(sender, location);
		}
		else{
			Stack<Location> locStack = new Stack<Location>();
			locStack.push(location);
			plugin.backHistoryMap.put(sender, locStack);
			plugin.previousLocationMap.put(sender, location);
		}

	}
	
	private void timeout(final String player){
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(plugin, new Runnable() {
			 
			  public void run() {
				  if(plugin.teleportRequests.containsKey(player)){
					  TeleportRequest request = plugin.teleportRequests.get(player);
					  request.getOwner().sendMessage(ChatColor.GOLD + "Player " + ChatColor.BLUE +
							  request.getRequesteeName() + "may be AFK and did not respond in time. " +
							  "Please try again later.");
				      plugin.teleportRequests.remove(player);
				  }
			  }
			}, 300L);
	}
	
	private boolean isSelf(String owner, String player){
		if(owner.equalsIgnoreCase(player)){
			Bukkit.getPlayer(owner).sendMessage(ChatColor.GOLD + "You cant request TP with yourself.");
			return true;
		}
		return false; 
	}
	
	 public String completeName(String playername) {
         Player[] onlinePlayers = Bukkit.getOnlinePlayers();
         for(int i = 0; i < onlinePlayers.length; i++) {
                 if(onlinePlayers[i].getName().toLowerCase().startsWith(playername.toLowerCase())) {
                         return onlinePlayers[i].getName();
                 }
         }
         OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
         for(int i = 0; i < offlinePlayers.length; i++) {
                 if(offlinePlayers[i].getName().toLowerCase().startsWith(playername.toLowerCase())) {
                         return offlinePlayers[i].getName();
                 }
         }
         return null;
 }
	
	private void cooldown(final Player player){
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(plugin, new Runnable() {
			 
			  public void run() {
			      plugin.cooldownList.remove(player);
			  }
			}, 100L);
	}
}