package com.alenia.AleniaCore;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerTracker implements Listener {

	World world;
	World defaultWorld;
	AleniaCore core;
	boolean ready = false;
	
	public PlayerTracker(String worldname, String defaultWorldname, AleniaCore core) {
		this.core=core;
		
		try {
			// Gives the server time to load the Hardcore world on startup.
			Bukkit.getLogger().info(AleniaCore.AleniaPrefix+"Waiting for server to be fully loaded.");
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Define world, and check to see if it exists.
		if ((world = Bukkit.getWorld(worldname)) == null) {
			Bukkit.getLogger().warning(AleniaCore.AleniaPrefix+"Hardcore world doesn't exist!");
			return;
		}
		
		if ((defaultWorld = Bukkit.getWorld(defaultWorldname)) == null) {
			Bukkit.getLogger().warning(AleniaCore.AleniaPrefix+"Default world doesn't exist!");
			return;
		}
		
		// Ready lock, fixes an Internal Error related to running a command before the Tracker has fully loaded.
		ready = true;
		
		Bukkit.getPluginManager().registerEvents(this, core);
		Bukkit.getLogger().info(AleniaCore.AleniaPrefix+"PlayerTracker started!"+
				"\nThe server clock is set to: "+new Date().toString());
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		// Check if the player is in the Hardcore world.
		// Using UID's on the unlikely chance there are name conflicts.
		if (e.getPlayer().getWorld().getUID()==world.getUID()) {
			startTracking(e.getPlayer());
		} 
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		if (e.getPlayer().getWorld().getUID()==world.getUID()) {
			stopTracking(e.getPlayer());
		} 
	}
	
	@EventHandler
	public void onChangeWorld(PlayerChangedWorldEvent e) {
		// Checks if the player left the world, or changed to it.
		if (e.getPlayer().getWorld().getUID()==world.getUID()) {
			startTracking(e.getPlayer());
		} else if (e.getFrom().getUID()==world.getUID()) {
			stopTracking(e.getPlayer());
		}
	}
	
	// Sends the player to the default world, onChangeWorld event stops the tracker.
	@EventHandler
	public void onRespawn(PlayerRespawnEvent e) {
		if (e.getPlayer().getWorld().getUID()==world.getUID()) {
			e.setRespawnLocation(defaultWorld.getSpawnLocation());
		}
	}
	
	public boolean startTracking(Player pl) {
		if (core.trackerLogConfig.getAleniaConfig().get(pl.getUniqueId().toString()) == null)
			core.trackerLogConfig.getAleniaConfig().set(pl.getUniqueId().toString()+".total", 0);
		
		Date d = new Date();
		core.trackerLogConfig.getAleniaConfig().set(
				pl.getUniqueId().toString()+".currentStartTime", d.getTime());
		core.trackerLogConfig.saveAleniaConfig();
		
		Bukkit.getLogger().info(AleniaCore.AleniaPrefix+
				"Tracking "+pl.getName()+" ("+pl.getUniqueId().toString()+") at time: "+d.getTime());
		return true;
	}
	
	public boolean stopTracking(Player pl) {
		Date d = new Date();
		long newTime = core.trackerLogConfig.getAleniaConfig().getLong(
				pl.getUniqueId().toString()+".currentStartTime");
		
		long totalTime = d.getTime() - newTime;
		
		long previousTotal = core.trackerLogConfig.getAleniaConfig().getLong(
				pl.getUniqueId().toString()+".total");
		
		totalTime += previousTotal;
		
		core.trackerLogConfig.getAleniaConfig().set(
				pl.getUniqueId()+".total", totalTime);
		core.trackerLogConfig.getAleniaConfig().set(pl.getUniqueId().toString()+".currentStartTime", 0);
		core.trackerLogConfig.saveAleniaConfig();
		
		Bukkit.getLogger().info(
				AleniaCore.AleniaPrefix
						+pl.getName()+" ("+pl.getUniqueId().toString()+") left Hardcore with the total time: "
						+totalTime+"ms");
		return true;
	}
	
	public boolean deletePlayerTracking(Player pl) {
		core.trackerLogConfig.getAleniaConfig().set(pl.getUniqueId().toString(), null);
		core.trackerLogConfig.saveAleniaConfig();
		Bukkit.getLogger().info(
				AleniaCore.AleniaPrefix+"Deleting "
						+pl.getName()+" ("+pl.getUniqueId().toString()+") from the Tracking log.");
		return true;
	}
	
	public boolean updateTimes() {
		for (Player pl : Bukkit.getOnlinePlayers()) {
			if (pl.getWorld().getUID()==world.getUID()) {
				stopTracking(pl);
				startTracking(pl);
			}
		}
		return true;
	}
	
	public String listPlayerTimes() {			
		String list = AleniaCore.AleniaPrefix+"Players Time in the "+world.getName()+" World\n";
		
		HashMap<String, Long> map = new HashMap<String, Long>();
		for (String key : core.trackerLogConfig.getAleniaConfig().getKeys(false)) {
			long total = core.trackerLogConfig.getAleniaConfig().getLong(key+".total");
			map.put(key, total);
		}
		
		if (map.isEmpty()) {
			return "There are no players to display!";
		}
		
		File f = new File(core.getDataFolder(), "trackerListOutput.txt");
		FileWriter writer = null;
		try {
			writer = new FileWriter(f);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		//Sort keys by size order.
		int size = map.size();
		for (int z = 0; z < size; z++) {
			long num = -1;
			String key = "";
			
			for (int x = 0; x < map.size(); x++) {
				long numX = (long) map.values().toArray()[x];
				if (numX > num) {
					num = numX;
					key = (String) map.keySet().toArray()[x];
				}
			}

			
			long secondsInMilli = 1000;
			long minutesInMilli = secondsInMilli * 60;
			long hoursInMilli = minutesInMilli * 60;
			long daysInMilli = hoursInMilli * 24;

			long days = num / daysInMilli;
			num = num % daysInMilli;

			long hours = num / hoursInMilli;
			num = num % hoursInMilli;

			long minutes = num / minutesInMilli;
			num = num % minutesInMilli;

			long seconds = num / secondsInMilli;
			
			// Grab the key and display the necessary components.
			OfflinePlayer pl = Bukkit.getOfflinePlayer(UUID.fromString(key));
			String line = "";
			
			if (pl.isOnline()) {
				line += ChatColor.GREEN+pl.getPlayer().getPlayerListName()+ChatColor.RESET+": ";
			} else {
				line += ChatColor.GRAY+pl.getName()+ChatColor.RESET+": ";
			}
			

			if (days > 0) line += days+"d ";
			if (hours > 0) line += hours+"h ";
			if (minutes > 0) line += minutes+"m ";
			if (seconds > 0) line += seconds+"s";
			
			// Only display 10 lines
			if (z < 10) list += line+"\n";
			
			try {
				writer.write(line+"\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			map.remove(key, num);
		}
		
		//Log Output to a file.
		try {
			writer.flush();	
			writer.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return list;
	}
}
