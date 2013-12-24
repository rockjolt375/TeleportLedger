package com.mythicacraft.teleportledger.utilities;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class ConfigAccessor {

        private Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("TeleportLedger");
        private final String fileName;
        private File configFile;
        private FileConfiguration fileConfiguration;
        private String folderPath;

        public ConfigAccessor(String fileName) {
                if (plugin == null)
                        throw new IllegalArgumentException("plugin cannot be null");
                this.fileName = fileName;
                folderPath = plugin.getDataFolder().getAbsolutePath() + File.separator + "data";
                configFile = new File(folderPath + File.separator + fileName);;
        }

        public void reloadConfig() {
                fileConfiguration = YamlConfiguration.loadConfiguration(configFile);
                // Look for defaults in the jar
                InputStream defConfigStream = plugin.getResource(fileName);
                if (defConfigStream != null) {
                        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
                        fileConfiguration.setDefaults(defConfig);
                }
        }

        public FileConfiguration getConfig() {
                if (fileConfiguration == null) {
                        this.reloadConfig();
                }
                return fileConfiguration;
        }

        public void saveConfig() {
                if (fileConfiguration == null || configFile == null) {
                        return;
                } else {
                        try {
                                getConfig().save(configFile);
                        } catch (IOException ex) {
                                plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configFile, ex);
                        }
                }
        }

        public void saveDefaultConfig() {
                if (!configFile.exists()) {
                        this.plugin.saveResource("data" + File.separator + fileName, false);
                }
        }

}