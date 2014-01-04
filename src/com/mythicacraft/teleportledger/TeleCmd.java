package com.mythicacraft.teleportledger;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
		
		///////////////Begin /tptokens command/////////////////////
		if(commandLabel.equalsIgnoreCase("tptokens")){
		
			if(!sender.hasPermission("teleportledger.mod") || args.length < 1){
				int count = playerData.getConfig().getInt(senderName + ".amount");
				sender.sendMessage(ChatColor.GREEN + "You currently have " + ChatColor.BLUE + count +
						ChatColor.GREEN + " teleport token(s).");
				return true;
			}
			else if(args.length == 1){
				int count = playerData.getConfig().getInt(args[0] + ".amount");
				sender.sendMessage(ChatColor.GREEN + "Player " + ChatColor.BLUE + args[0] +
						ChatColor.GREEN + " currently has " + ChatColor.BLUE + count +
						ChatColor.GREEN + " token(s) in their account.");
				return true;
			}
			else if(args.length == 3 && args[0].equalsIgnoreCase("set")){
				if(!isNumber(args[2])){ sender.sendMessage(ChatColor.RED + "Invalid Number, " +
						"type /tptokens set <player> <amount> to set token amount"); return true;
				}
				int count = setTokenAmount(args[1], Integer.parseInt(args[2]));
				sender.sendMessage(ChatColor.GOLD + "Player " + ChatColor.BLUE + args[1] + ChatColor.GOLD + " now has " +
						ChatColor.BLUE + count + ChatColor.GOLD + " in their account.");
				return true;
			}
			else{
				sender.sendMessage(ChatColor.RED + "That is not a valid use of this command!");
				return true;
			}
		}
		/////////////////////Begin /tpa, /tpask, /tpcall////////////////////////
		if(commandLabel.equalsIgnoreCase("tpa") || commandLabel.equalsIgnoreCase("tpask") ||
				commandLabel.equalsIgnoreCase("call")){
			if(args.length != 1){ sender.sendMessage(ChatColor.BLUE + "To request teleportation, use" +
					"/tpa <player>"); return true;}
			if(plugin.cooldownList.contains(senderPlayer)){sender.sendMessage(ChatColor.BLUE + "You must wait " + 
					"10 seconds between each teleport request."); return true;
			}
			Player requestee = isOnline(sender, args[0], TeleportType.TPA);
			if(requestee == null || isBlocked(senderName, args[0])) return true;
			if(isIgnoring(senderName, requestee.getName().toString())){
				sender.sendMessage(ChatColor.GOLD + "That player is not accepting TP requests at this time!");
				return true;
			}
			requestee.sendMessage(ChatColor.AQUA + "Player " + ChatColor.GOLD + sender.getName().toString() +
					ChatColor.AQUA + " has requested to teleport to your location. To accept, type" +
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
					"10 seconds between each teleport request."); return true;
			}
			Player requestee = isOnline(sender, args[0], TeleportType.TPAHERE);
			if(requestee == null || isBlocked(senderName, args[0])) return true;
			if(isIgnoring(senderName, requestee.getName().toString())){
				sender.sendMessage(ChatColor.GOLD + "That player is not accepting TP requests at this time!");
				return true;
			}
			requestee.sendMessage(ChatColor.AQUA + "Player " + ChatColor.GOLD + sender.getName().toString() +
					ChatColor.AQUA + " has requested to teleport you to their location. To accept, type" +
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
			if(request.getType() == TeleportType.TPA)
				request.getOwner().teleport(request.getRequestee().getLocation());
			else
				request.getRequestee().teleport(request.getOwner().getLocation());
						
			request.getOwner().sendMessage(ChatColor.GOLD + "[TeleportLedger] " + ChatColor.GREEN + "You now have " +
					ChatColor.BLUE + setTokenAmount(request.getOwnerName(), -1) + ChatColor.GREEN +
					" teleport tokens in your account.");
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
			plugin.teleportRequests.remove(senderName);
			return true;
		}
		////////////////////Begin /tpignore///////////////////////////
		if(commandLabel.equalsIgnoreCase("tpignoreall")){
			if(playerData.getConfig().getBoolean(senderName + ".ignoreAll") == true){
				playerData.getConfig().set(senderName + ".ignoreAll", false);
				sender.sendMessage(ChatColor.AQUA + "You are no longer accepting teleport requests.");
			}
			else{
				playerData.getConfig().set(senderName + ".ignoreAll", true);
				sender.sendMessage(ChatColor.AQUA + "You are now accepting teleport requests.");
			}
			playerData.saveConfig();
			return true;
		}
		///////////////////Begin /tpblock/////////////////////////
		if(commandLabel.equalsIgnoreCase("tpblock")){
			if(args.length != 1){ sender.sendMessage(ChatColor.BLUE + "To block tp requests from a specific player" + 
					", use /tpblock <player>"); return true;}
			List<String> blockedPlayers = playerData.getConfig().getStringList(senderName + ".blockedPlayers");
			if(blockedPlayers.contains(args[0])){
				sender.sendMessage(ChatColor.GOLD + "You have already blocked that player!");
				return true;
			}
			blockedPlayers.add(args[0]);
			setBlockedPlayers(senderName, blockedPlayers);
			sender.sendMessage(ChatColor.AQUA + "Player " + ChatColor.GOLD + args[0] + ChatColor.AQUA +
					" is now blocked. You will no longer receive TP requests from them.");
			return true;
		}
		//////////////////Begin /tpunblock////////////////////////
		if(commandLabel.equalsIgnoreCase("tpunblock")){
			if(args.length != 1){ sender.sendMessage(ChatColor.BLUE + "To unblock tp requests from a specific player" + 
					", use /tpunblock <player>"); return true;}
			List<String> blockedPlayers = playerData.getConfig().getStringList(senderName + ".blockedPlayers");
			if(!blockedPlayers.contains(args[0])){
				sender.sendMessage(ChatColor.GOLD + "You have not blocked that player!");
				return true;
			}
			blockedPlayers.remove(args[0]);
			setBlockedPlayers(senderName, blockedPlayers);
			sender.sendMessage(ChatColor.AQUA + "Player " + ChatColor.GOLD + args[0] + ChatColor.AQUA +
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
	
	private int setTokenAmount(String player, int count){
		ConfigAccessor playerData = new ConfigAccessor("players.yml");
		if(count != 0){
			int tokens = playerData.getConfig().getInt(player + ".amount");
			count += tokens;
		}
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
	
	private boolean isIgnoring(String sender, String requestee){
		ConfigAccessor playerData = new ConfigAccessor("players.yml");
		return playerData.getConfig().getBoolean(requestee + ".ignoreAll");
	}
	
	private boolean isNumber(String num){
		try{
			Integer.parseInt(num);
			return true;
		} catch(Exception e){return false;}
	}
	
	private Player isOnline(CommandSender sender, String player, TeleportType type){
		Player requestee = Bukkit.getPlayer(player);
		if(requestee == null){sender.sendMessage(ChatColor.GOLD + "[TeleportLedger] " + ChatColor.RED + "That" +
				"player is not online!"); return null;}	
		plugin.teleportRequests.put(player, new TeleportRequest(Bukkit.getPlayer(sender.getName().toString()),
				requestee, type));
		return requestee;
	}
	
	private void timeout(final String player){
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(plugin, new Runnable() {
			 
			  public void run() {
			      plugin.teleportRequests.remove(player);
			  }
			}, 200L);
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