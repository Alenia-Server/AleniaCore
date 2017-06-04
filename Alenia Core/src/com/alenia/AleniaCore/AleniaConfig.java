package com.alenia.AleniaCore;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class AleniaConfig {

	private FileConfiguration aleniaConfig = null;
	private File aleniaConfigFile = null;
	
	AleniaCore core;
	String config;
	
	// Modification of the Config Template to make it Dynamic.
	
	public AleniaConfig(String config, AleniaCore core) {
		this.core=core;
		this.config=config;
			
		saveDefaultConfig();
	}
	
	public void reloadAleniaConfig() {
	    if (aleniaConfigFile == null) {
	    aleniaConfigFile = new File(core.getDataFolder(), config+".yml");
	    }
	    aleniaConfig = YamlConfiguration.loadConfiguration(aleniaConfigFile);

	    // Look for defaults in the jar
	    Reader defConfigStream;
		try {
			defConfigStream = new InputStreamReader(core.getResource(config+".yml"), "UTF8");
		    if (defConfigStream != null) {
		        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
		        aleniaConfig.setDefaults(defConfig);
		    }
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public FileConfiguration getAleniaConfig() {
	    if (aleniaConfig == null) {
	        reloadAleniaConfig();
	    }
	    return aleniaConfig;
	}
	
	public void saveAleniaConfig() {
	    if (aleniaConfig == null || aleniaConfigFile == null) {
	        return;
	    }
	    try {
	        getAleniaConfig().save(aleniaConfigFile);
	    } catch (IOException ex) {
	    	Bukkit.getLogger().log(Level.SEVERE, "Could not save config to " + aleniaConfigFile, ex);
	    }
	}
	
	public void saveDefaultConfig() {
	    if (aleniaConfigFile == null) {
	        aleniaConfigFile = new File(core.getDataFolder(), config+".yml");
	    }
	    if (!aleniaConfigFile.exists()) {            
	         core.saveResource(config+".yml", false);
	     }
	}
}
