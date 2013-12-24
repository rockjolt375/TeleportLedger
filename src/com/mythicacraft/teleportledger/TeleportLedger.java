package com.mythicacraft.teleportledger;

import java.io.File;
import java.util.logging.Logger;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.mythicacraft.teleportledger.utilities.ConfigAccessor;

public class TeleportLedger extends JavaPlugin{

	private static final Logger log = Logger.getLogger("Minecraft");
	public static Permission perms = null;
	
	 public void onDisable() {
         log.info("[TeleportLedger] Disabled!");
	 }
	 
	 public void onEnable() {
		 PluginManager pm = getServer().getPluginManager();
		 
		 if(!setupVault()) {
             pm.disablePlugin(this);
             return;
		 }
		 setupPermissions();
		 loadPlayerData();
		 
		 getCommand("tele").setExecutor(new TeleCmd(this));
		 
		 log.info("[TeleportLedger] Enabled!");
	 }
	
	 public void loadPlayerData() {

         ConfigAccessor playerData = new ConfigAccessor("players.yml");
         String pluginFolder = this.getDataFolder().getAbsolutePath() + File.separator + "data"; 
         (new File(pluginFolder)).mkdirs();
         File teleTrack = new File(pluginFolder + File.separator + "players.yml");

         if (!teleTrack.exists()) {
                 log.info("No players.yml, making one now...");
                 playerData.saveDefaultConfig();
                 log.info("Done!");
                 return;
         }
         log.info("players.yml detected!");
	 }
	 
	 private boolean setupVault() {
         Plugin vault =  getServer().getPluginManager().getPlugin("Vault");
         if (vault != null && vault instanceof net.milkbowl.vault.Vault) { //first check that vault exists
                 log.info("[TeleportLedger] Hooked into Vault v" + vault.getDescription().getVersion());
         } else {
                 log.severe("[TeleportLedger] Vault plugin not found!");
                 return false;
         }
         return true;
	 }
	 
	 private boolean setupPermissions() {
	        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
	        perms = rsp.getProvider();
	        return perms != null;
	    }
}
