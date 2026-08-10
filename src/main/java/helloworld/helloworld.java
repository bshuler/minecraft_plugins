package helloworld;


import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class helloworld extends JavaPlugin{
	public void onEnable() {
		Bukkit.getLogger().log(Level.INFO, ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + "(!!!)------> Hello World ON<------(!!!)");		
	}
	
	public void onDisable() {
		Bukkit.getLogger().log(Level.INFO, ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + "(XxXxXx)------> Hello World OFF <------(XxXxXx)");
	}
}
