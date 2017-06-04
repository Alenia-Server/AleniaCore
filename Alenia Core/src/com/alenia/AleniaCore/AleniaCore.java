package com.alenia.AleniaCore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class AleniaCore extends JavaPlugin implements Listener {

	AleniaConfig settingsConfig = new AleniaConfig("settings", this);
	AleniaConfig trackerLogConfig = new AleniaConfig("trackerLog", this);
	
	Thread PTT;
	PlayerTracker pt;
	
	static String AleniaPrefix = ChatColor.LIGHT_PURPLE+"[AleniaCore] "+ChatColor.RESET;
	
	@Override
	public void onEnable() {
		Bukkit.getLogger().info("Initializing Alenia-Core v0.1");
		
		String defaultWorld = settingsConfig.getAleniaConfig().getString("DefaultWorld");
		String hardcoreWorld = settingsConfig.getAleniaConfig().getString("HardcoreWorld");
		//Start PlayerTracker in new thread.
		PTT = new Thread(() -> 
			pt = new PlayerTracker(hardcoreWorld, defaultWorld, this));
		PTT.start();
		
		Bukkit.getPluginManager().registerEvents(this, this);
	}
	
	@Override
	public void onDisable() {
		try {
			pt.updateTimes();
		} catch (NullPointerException e) {}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("alenia")) {
			if (args.length == 0) {
				sender.sendMessage(AleniaCore.AleniaPrefix+"Usage: /alenia tracker list");
				return true;
			} else if (args[0].equalsIgnoreCase("tracker")) {
				if (args.length == 2 && args[1].equalsIgnoreCase("list")) {
					sender.sendMessage(AleniaCore.AleniaPrefix+"Processing.");
					
					if (pt == null || pt.ready == false) {
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					
					pt.updateTimes();

					sender.sendMessage(pt.listPlayerTimes());
					return true;
				} else {
					sender.sendMessage(AleniaCore.AleniaPrefix+"PlayerTracker - Commands:\n"
										+ "/alenia tracker list");
					return true;
				}
			}
			return true;
		} 
		return false; 
	}
}
