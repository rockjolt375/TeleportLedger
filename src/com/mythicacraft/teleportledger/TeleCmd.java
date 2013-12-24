package com.mythicacraft.teleportledger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.mythicacraft.teleportledger.utilities.ConfigAccessor;

public class TeleCmd implements CommandExecutor{

	TeleportLedger plugin;
	
	public TeleCmd(TeleportLedger plugin){
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		String senderName = sender.getName();
		ConfigAccessor playerData = new ConfigAccessor("player.yml");
		
		if(commandLabel == "tele"){
		
			if(args[0].equalsIgnoreCase("info")){
				String count = playerData.getConfig().getString(senderName);
				if(count == null) count = "0";
				sender.sendMessage(ChatColor.GREEN + "You currently have " + ChatColor.BLUE + count + ChatColor.GREEN + " teleport point(s).");
			}

		}
				
		return false;
	}

}
