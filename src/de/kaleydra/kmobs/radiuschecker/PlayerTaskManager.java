package de.kaleydra.kmobs.radiuschecker;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import de.kaleydra.kmobs.KMobs;

public class PlayerTaskManager {
	
	KMobs plugin;
	
	private Map<String, RadiusCheckTask> playerTasks = new HashMap<String, RadiusCheckTask>();
	
	public PlayerTaskManager(KMobs plugin){
		this.plugin = plugin;
	}
	
	private void addRadiusChecker (String player, RadiusCheckTask task){
		RadiusCheckTask oldTask = playerTasks.put(player, task);
		if(oldTask != null) {
			plugin.getLogger().warning("Second RadiusChecker Task started for player '"+player+"'! Should not happen!");
			oldTask.cancel();
		}
	}
	
	public void startRadiusChecker(Player player){
		RadiusCheckTask radiusChecker = new RadiusCheckTask(player, this);
		addRadiusChecker(player.getName(), radiusChecker);
		radiusChecker.runTaskTimer(plugin, 0, 19);
		plugin.getLogger().info("RadiusChecker task started for "+player.getName());
	}
	
	public boolean containsPlayer(Player player){
		return playerTasks.containsKey(player.getName());
	}
	
	public void cancelRadiusChecker(Player player){
		RadiusCheckTask rc = playerTasks.get(player.getName());
		if(rc == null) return;
		rc.cancel();
		playerTasks.remove(player.getName());
		plugin.getLogger().info("RadiusChecker task stopped for "+player.getName());
	}
}
