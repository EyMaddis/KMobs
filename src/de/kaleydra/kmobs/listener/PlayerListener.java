package de.kaleydra.kmobs.listener;

import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.kaleydra.kmobs.KMobs;
import de.kaleydra.kmobs.commands.KMobCommand;
import de.kaleydra.kmobs.mobs.KMob;

public class PlayerListener implements Listener {
	private KMobs plugin;
	public PlayerListener(KMobs plugin){
		this.plugin = plugin;
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerLogin(PlayerLoginEvent e){
		Player p = e.getPlayer();
		boolean setAtFirstSpawn = plugin.getConfig().getBoolean("maxPlayerHealth.onlyAtFirstSpawn");
		double maxHealth = plugin.getConfig().getDouble("maxPlayerHealth.maxHealth");
		
		if(KMobs.isOnWhitelist(p.getWorld())){
			// start radius check runnable			
			plugin.getPlayerTaskManager().startRadiusChecker(p);
		}
		if(plugin.isInDebugMode() || (setAtFirstSpawn && !p.hasPlayedBefore())){
			e.getPlayer().setMaxHealth(maxHealth);
			e.getPlayer().setHealthScaled(false);
		}
	}


	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerLogout(PlayerQuitEvent e){
		// stop radius check runnable if running
		plugin.getPlayerTaskManager().cancelRadiusChecker(e.getPlayer());
	}

	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerWorldChange(PlayerChangedWorldEvent e){
		if(!KMobs.isOnWhitelist(e.getFrom()) && KMobs.isOnWhitelist(e.getPlayer().getWorld())){
			// start radius check runnable
			plugin.getPlayerTaskManager().startRadiusChecker(e.getPlayer());
		}else if(KMobs.isOnWhitelist(e.getFrom()) && !KMobs.isOnWhitelist(e.getPlayer().getWorld())){
			// stop radius check runnable
			plugin.getPlayerTaskManager().cancelRadiusChecker(e.getPlayer());
		}
	}
	
	@EventHandler
	public void mobAnalyzing(PlayerInteractEntityEvent event){
		if(!(event.getRightClicked() instanceof LivingEntity)) return;
		
		final Player player = event.getPlayer();
		if(!KMobCommand.activatedMobInfo.contains(player.getName())) return;
		

		event.setCancelled(true);
		
		KMob mob = plugin.getMobManager().getMobByEntity((LivingEntity)event.getRightClicked());
		if(mob == null){
			player.sendMessage(ChatColor.RED+"The clicked mob is not a KMob!");
		} else {
			player.sendMessage(ChatColor.GREEN+"The KMob is: "+mob.getId());
		}
		
		KMobCommand.activatedMobInfo.remove(player.getName());
		
		
	}
}
