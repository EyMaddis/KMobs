package de.kaleydra.kmobs.listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.SlimeSplitEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import de.kaleydra.kmobs.KMobs;
import de.kaleydra.kmobs.drops.Drop;
import de.kaleydra.kmobs.mobs.KMob;
import de.kaleydra.kmobs.mobs.MobManager;
import de.kaleydra.kmobs.skills.SkillTrigger;

public class MobListener implements Listener {

	private KMobs plugin;

	MobManager mobManager;
	boolean regionsOnly;
	boolean enableMobspawners;
	boolean useAnimalSpawns;

	public MobListener(KMobs plugin){
		this.plugin = plugin;
		mobManager = plugin.getMobManager();
		regionsOnly 		= plugin.getConfig().getBoolean("regionSpawnOnly");
		enableMobspawners	= plugin.getConfig().getBoolean("enableMobspawners");
		useAnimalSpawns 	= plugin.getConfig().getBoolean("useAnimalSpawns");
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onMobSpawn(CreatureSpawnEvent e){
		if(e.isCancelled()) return;
		
		
		final SpawnReason spawnReason = e.getSpawnReason();
		if(regionsOnly && !spawnReason.equals(SpawnReason.CUSTOM)){
			e.setCancelled(true); // cancel all mobspawn!
		}
		if((spawnReason.equals(SpawnReason.NATURAL) || spawnReason.equals(SpawnReason.CHUNK_GEN)  
				|| spawnReason.equals(SpawnReason.VILLAGE_INVASION) 
				|| spawnReason.equals(SpawnReason.SPAWNER)) 
				&& (useAnimalSpawns || e.getEntity() instanceof Monster)){
			
			ApplicableRegionSet regions = plugin.getWorldGuard().getRegionManager(e.getLocation().getWorld())
					.getApplicableRegions(e.getLocation());
			// get the list of regions that contain the given location
			for(ProtectedRegion region:regions){
				List<KMob> mobs = mobManager.getRegionMobs(region.getId());
				Collections.shuffle(mobs);
				//plugin.getLogger().info(region.getId());
				for(KMob mob:mobs){
					boolean canSpawn = mob.checkSpawnChance();
					//plugin.getLogger().info("canSpawn:" + canSpawn);
					if(canSpawn) {
						mob.spawnMob(e.getLocation());
						e.setCancelled(true);
						return;
					}
				}
			}
			
			if(regionsOnly || (!enableMobspawners && spawnReason.equals(SpawnReason.SPAWNER))) {
				return;
			}
			
			// TODO: Efficiency!
			ArrayList<KMob> mobs = new ArrayList<KMob>(mobManager.getMobsWithoutRegion());
			Collections.shuffle(mobs,new Random());
			if(mobs.size() <= 0) return;
			for(int i=0; i<5; i++){ // try five times to span anything
				for(KMob mob : mobs){
					if(mob.getWorldguardRegion() != null) continue; // mobs for regions are blocked
				
					if (mob.checkSpawn(e.getLocation())) {
						mob.spawnMob(e.getLocation());
						e.setCancelled(true);
						return;
					}
					
				}
			}
		}
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onMobDamage(EntityDamageByEntityEvent e){
		if(e.isCancelled()) return;
		if(!(e.getEntity() instanceof LivingEntity) || e.getEntity() instanceof Player) return;
		Entity damager = e.getDamager();
		if(damager == null) return;
		if(damager instanceof Projectile){
			
		}
		LivingEntity entity = (LivingEntity)e.getEntity();
		
		KMob mob = mobManager.getMobByEntity(entity);
		if(mob==null) return;
		if(damager instanceof Projectile) {
			Projectile projectile = (Projectile)damager;
			if(projectile.getShooter() == null){
				KMobs.logDebugMessage("Mob "+mob.getName()+" was hit by a projectile without a shooter!");
				return;
			}
			mob.activateSkills(SkillTrigger.onProjectileDamage,entity, projectile.getShooter());
		} 
		else if(!(damager instanceof LivingEntity)){
			//KMobs.logDebugMessage("Mob "+mob.getName()+" was hit by a non living entity!"); // ex. lightning
			return;
		} else {
			mob.activateSkills(SkillTrigger.onDamage, entity, (LivingEntity)damager);
		}
//		if(e.getDamager() == null || !(e.getDamager() instanceof Player)) return;
		
//		PlayerDamageMetadataValue playerDamage;
//		if(entity.hasMetadata(PlayerDamageMetadataValue.KEY) && !entity.getMetadata(PlayerDamageMetadataValue.KEY).isEmpty()){
//			playerDamage = (PlayerDamageMetadataValue) entity.getMetadata(PlayerDamageMetadataValue.KEY).get(0);
//		}else {
//			playerDamage = new PlayerDamageMetadataValue(plugin);
//			entity.setMetadata(PlayerDamageMetadataValue.KEY,playerDamage);
//		}
//		playerDamage.add(e.getDamage());
		
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onDamageByBlock(EntityDamageByBlockEvent e){
		if(e.isCancelled()) return;
		if(plugin.blockAllCactusDamage() && e.getEntity() instanceof LivingEntity 
			&& !(e.getEntity() instanceof Player)) {
			if(e.getCause().equals(DamageCause.CONTACT)) // block only cactus
				e.setCancelled(true);
		}
	}

	
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onProjectileLaunch(ProjectileLaunchEvent e){
		if(!(e.getEntity().getShooter() instanceof LivingEntity)) return;
			LivingEntity le = (LivingEntity)e.getEntity().getShooter();
			KMob mob = mobManager.getMobByEntity(le);		
			if(mob == null) return;
			mob.activateSkills(SkillTrigger.onProjectileLaunch, le, le);
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onMobAttack(EntityDamageByEntityEvent e){
		if(e.isCancelled()) return;
		if(!(e.getDamager() instanceof LivingEntity)) return;
		if(!(e.getEntity() instanceof LivingEntity)) return;
		LivingEntity entity = (LivingEntity)e.getEntity();
		LivingEntity damager = (LivingEntity)e.getDamager();
		KMob mob = mobManager.getMobByEntity((LivingEntity) e.getDamager());
		if(mob==null) return;
		
		mob.activateSkills(SkillTrigger.onAttack, damager,entity);
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onMobDeath(EntityDeathEvent e){
		if(!(e.getEntity() instanceof LivingEntity) || e.getEntity() instanceof Player) return;
		LivingEntity entity = (LivingEntity)e.getEntity();
		KMob mob = mobManager.getMobByEntity(entity);
		
		
		if(mob==null) return;

		if(plugin.getConfig().getBoolean("blockAllDrops")){
			e.getDrops().clear();
			entity.getEquipment().clear(); // do not drop, god dammit!
		}
		mob.activateSkills(SkillTrigger.onDeath, entity,entity.getKiller());
		
		Entity passenger = entity.getPassenger();
		if(passenger != null){
			if(passenger instanceof LivingEntity){
				LivingEntity livingPassenger = (LivingEntity)passenger;
				KMob passengerMob = plugin.getMobManager().getMobByEntity(livingPassenger);
				if(passengerMob != null)
					passengerMob.activateSkills(SkillTrigger.onDismount, livingPassenger, entity);
				mob.activateSkills(SkillTrigger.onPassengerDismount, entity, livingPassenger);
			} else {
				mob.activateSkills(SkillTrigger.onPassengerDismount, entity, null);
			}
		} 
		Entity vehicle = entity.getVehicle();
		if(vehicle != null){
			if(vehicle instanceof LivingEntity){
				final LivingEntity livingVehicle = (LivingEntity)vehicle;
				KMob vehicleMob = plugin.getMobManager().getMobByEntity(livingVehicle);
				if(vehicleMob != null)
					vehicleMob.activateSkills(SkillTrigger.onPassengerDismount, livingVehicle, entity);
				mob.activateSkills(SkillTrigger.onDismount, entity, livingVehicle);
			} else {
				mob.activateSkills(SkillTrigger.onDismount, entity, null);
			}
		}
		
//		if(!entity.hasMetadata(PlayerDamageMetadataValue.KEY) || entity.getMetadata(PlayerDamageMetadataValue.KEY).isEmpty()){
//			e.setDroppedExp(0); // not killed by player
//			return;
//		}
		
		// TODO: Further testing, not accurate enough
//		PlayerDamageMetadataValue playerDamage = (PlayerDamageMetadataValue) entity.getMetadata(PlayerDamageMetadataValue.KEY).get(0);
//		double maxLife = entity.getMaxHealth();
//		double takenDamage = playerDamage.calculateDamagePercentage(maxLife);
//		double minDamageRationFromPlayer = plugin.getConfig().getDouble("minDamageRatioFromPlayer");
//		if(takenDamage < minDamageRationFromPlayer){
//			return; // player did not do enough damage, don't drop.
//		}
		
		int expDrops = mob.calculateExpDrops();
		if(expDrops >= 0) e.setDroppedExp(expDrops);
		
		for(Drop drop:mob.getDrops()){
			if(Math.random() <= drop.getDropChance()){
				e.getDrops().add(drop.getItem());
			}
		}
			
				
		
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onMobTargeting(EntityTargetEvent e){
		if(!(e.getEntity() instanceof LivingEntity)) return;
		if(!(e.getTarget() instanceof Player)) return;
		LivingEntity entity = (LivingEntity)e.getEntity();
		KMob mob = mobManager.getMobByEntity(entity);
		if(mob==null) return;
		mob.activateSkills(SkillTrigger.onTarget, entity,(LivingEntity) e.getTarget());		
	}
	
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onVehicleExit(VehicleExitEvent e){		
		LivingEntity entity = (LivingEntity)e.getExited();
		KMob mob = mobManager.getMobByEntity(entity);
		if(mob==null) return;
		LivingEntity vehicle = entity;
		if(entity.getVehicle() instanceof LivingEntity){
			vehicle = (LivingEntity) entity.getVehicle();
			mob.activateSkills(SkillTrigger.onPassengerDismount, vehicle, entity);
			mob.activateSkills(SkillTrigger.onDismount, entity, vehicle);
		} else mob.activateSkills(SkillTrigger.onDismount, entity,null);
	}
	
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onRename(PlayerInteractEntityEvent event){
		final ItemStack itemInHand = event.getPlayer().getItemInHand();
		if(itemInHand == null) return;
		if(!itemInHand.getType().equals(Material.NAME_TAG)) return;
		
		LivingEntity entity = (LivingEntity)event.getRightClicked();
		KMob mob = mobManager.getMobByEntity(entity);
		if(mob==null) return;
		
		event.setCancelled(true);
		
		event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION,900, 3), true);
		event.getPlayer().sendMessage(ChatColor.DARK_RED+"Haha, KMobs is too clever for that.");
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onSlimeSplit(SlimeSplitEvent e){		
		LivingEntity entity = (LivingEntity)e.getEntity();
		KMob mob = mobManager.getMobByEntity(entity);
		if(mob==null) return;
		e.setCancelled(true);
	}
	
	
}
