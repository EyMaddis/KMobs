package de.kaleydra.kmobs.mobs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.util.StringUtil;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import de.kaleydra.kmobs.KMobs;
import de.kaleydra.kmobs.drops.Drop;
import de.kaleydra.kmobs.events.KMobEvent;
import de.kaleydra.kmobs.events.KMobSpawnEvent;
import de.kaleydra.kmobs.skills.ActiveSkill;
import de.kaleydra.kmobs.skills.SkillData;
import de.kaleydra.kmobs.skills.SkillTrigger;

public class KMob {
	private KMobs plugin;
	
	/**
	 * The string is the name of the class which extends KMobEvent
	 */
	Map<String, HandlerList> eventHandlers = new HashMap<String, HandlerList>();

	private boolean debugMob = false;
	
	private String id;
	
	// Attributes
	private String name = null;
	private MobRarity rarity = MobRarity.COMMON;
	private String filePath;

	private boolean alwaysShowName = false;
	private double health = 20;
	private String mobtype;
	private boolean isBaby = false;
	private String passengerId = null; // Id of the passenger Mob
	
	private int expMax = -1;
	private int expMin = -1;
	
	private MobEquipment equipment = null;
	
	private List<Drop> drops = new ArrayList<Drop>();

	// Spawn checks
	private String world;
	private String area; // NorthWest, SouthWest, SouthEast, NorthEast (map quadrant)
	private double spawnChance;
	private String worldguardRegion = null; 
	private List<String> biomes = null;
	private int minHeight = 0;
	private int maxHeight = 0;
	private int minSafeZoneDistance = 0;
	private int maxSafeZoneDistance = 0;
	
	private Random random = new Random();
	
	Map<SkillTrigger, List<SkillData>> skills = new HashMap<SkillTrigger, List<SkillData>>();
	
	public KMob(KMobs plugin, String id, String filePath) {
		this.id = id;
		this.plugin = plugin;
		this.setFilePath(filePath);
		
		for(SkillTrigger skillType:SkillTrigger.values()){
			skills.put(skillType, new ArrayList<SkillData>());
		}
	}

	public void logDebugMessage(String message){
		if(!debugMob) return;
		KMobs.getInstance().getLogger().info("["+id+"] "+message);
	}
	

	// ------------------------------- //
	// ---------- SKILLS ------------- //
	// ------------------------------- //
	
	public List<SkillData> getSkills(@Nonnull SkillTrigger skillType){
		return skills.get(skillType);
	}
	
	public void addSkill(SkillTrigger skillType, SkillData skillData) {
		
		if(skillType.equals(SkillTrigger.onInterval) && !skillData.isIntervalSkill()){
			// TODO: Exception Handling!
			KMobs.getInstance().getLogger().severe("Tried to add a skill to onInterval which does not have an interval! Mob: "+name);
			return;
		}
		
		skills.get(skillType).add(skillData);
	}

	public void activateSkills(SkillTrigger skillTrigger, LivingEntity mob, LivingEntity target) {
		if(skillTrigger == null || !skills.containsKey(skillTrigger));
		logDebugMessage("activated skill trigger: "+skillTrigger);
		for (SkillData skillData : skills.get(skillTrigger)) {
			logDebugMessage("activating skill: "+skillData.getSkill().getIdentifier());
			activateSkill(skillData,mob,target, skillTrigger.equals(SkillTrigger.onDeath));
		}
	}

	public ActiveSkill activateSkill(SkillData skillData, LivingEntity mob, LivingEntity target, boolean ignoreDeath){
		ActiveSkill activeSkill;
		activeSkill = new ActiveSkill(this, skillData, mob, target, true);
		activeSkill.setIgnoreDeath(ignoreDeath);
		activeSkill.activate();
		return activeSkill;
	}
	

	
	// --------------------------------- //
	// ---------- SPAWNING ------------- //
	// --------------------------------- //
	
	public boolean checkSpawnChance() {
		double random = this.random.nextDouble();
		if (!(random <= spawnChance)){
			logDebugMessage("spawn failed because of the spawn chance. Random: "+random);
			return false;
		}
		return true;
	}

	/**
	 * checks whether a mob can spawn, but ignoring other parameters!
	 * 
	 * @param l
	 * @return
	 */
	public boolean checkSpawnWithRegions(Location l) {
		if (worldguardRegion != null) {
			ApplicableRegionSet regions = plugin.getWorldGuard().getRegionManager(l.getWorld()).getApplicableRegions(l);
			// get the list of regions that contain the given location
			for (ProtectedRegion region : regions) {
				logDebugMessage("Checking Region: "+region.getId());
				if (StringUtil.startsWithIgnoreCase(region.getId(), worldguardRegion) && checkSpawnChance())
					return true;
			}
			logDebugMessage("failed at region check! Not the right Region at this position! Expected "+worldguardRegion);			
			return false;
		}
		return true;
	}

	/**
	 * checks whether a mob can spawn, but ignoring WorldGuard
	 * 
	 * @param l
	 * @return
	 */
	public boolean checkSpawn(Location l) {
		if(!l.getWorld().getName().equalsIgnoreCase(world)){
			logDebugMessage("failed spawn because of world, needed "+world+" got "+l.getWorld().getName());
			return false;
		}
		if (!checkSpawnChance()){
			logDebugMessage("failed spawn because of spawn chance");
			return false;
		}
		if (biomes == null || (!biomes.isEmpty() && !biomes.contains(l.getBlock().getBiome().name().toLowerCase()))) {
			logDebugMessage("failed spawn because of biomes");
			return false;
		}

		if (area != null && !area.equalsIgnoreCase(KMobs.getAreaName(l))) {
			logDebugMessage("failed spawn because of area check");
			return false;
		}
		
		if (minHeight > 0 && l.getBlockY() < minHeight){
			logDebugMessage("failed spawn because of min height ("+minHeight+")");
			return false;
		}

		if (maxHeight > 0 && l.getBlockY() > maxHeight){
			logDebugMessage("failed spawn because of max height ("+maxHeight+")");
			return false;
		}
		if (plugin.useTowny() && minSafeZoneDistance > 0 && maxSafeZoneDistance > 0 && minSafeZoneDistance <= maxSafeZoneDistance) {
			try {
				double distance = KMobs.calculateNearestTownDistance(l);
				if (distance < minSafeZoneDistance || distance > maxSafeZoneDistance){
					logDebugMessage("failed spawn because of town distance ("+distance+")");
					return false;
				}
			} catch (Exception e) { //TownyException
				e.printStackTrace();
				return false;
			}
		} else { // use world spawn
			double distanceSquared = l.getWorld().getSpawnLocation().distanceSquared(l);
			if(distanceSquared < minSafeZoneDistance*minSafeZoneDistance || distanceSquared > maxSafeZoneDistance*maxSafeZoneDistance){
				return false;
			}
		}
		return true;
	}

	public LivingEntity spawnMob(Location l) {
		String[] part = mobtype.split(":");
		String mobname = part[0];
		String data = null;

		if (part.length > 1)
			data = part[1];

		EntityType type;
		type = EntityType.fromName(mobname);

		boolean isWitherSkeleton = false;
		boolean isSuperCreeper = false;
		boolean isAngry = false;
		boolean isZombieVillager = false;
		boolean isZombieHorse = false;
		boolean isDonkey = false;
		boolean isMule = false;
		boolean isSkeletonHorse = false;
		if (mobname.equalsIgnoreCase("witherskeleton") || mobname.equalsIgnoreCase("wither_skeleton")) {
			type = EntityType.SKELETON;
			isWitherSkeleton = true;
		}if (mobname.equalsIgnoreCase("magmacube")) {
			type = EntityType.MAGMA_CUBE;
		} else if (mobname.equalsIgnoreCase("supercreeper")) {
			isSuperCreeper = true;
			type = EntityType.CREEPER;
		} else if (mobname.equalsIgnoreCase("angrywolf")) {
			isAngry = true;
			type = EntityType.WOLF;
		} else if (mobname.equalsIgnoreCase("zombievillager")) {
			isZombieVillager = true;
			type = EntityType.ZOMBIE;
		} else if (mobname.equalsIgnoreCase("angrypigzombie")) {
			isAngry = true;
			type = EntityType.PIG_ZOMBIE;
		} else if (mobname.equalsIgnoreCase("zombiehorse")){
			isZombieHorse = true;
			type = EntityType.HORSE;
		}else if (mobname.equalsIgnoreCase("mule")){
			isMule = true;
			type = EntityType.HORSE;
		}else if (mobname.equalsIgnoreCase("donkey")){
			isDonkey = true;
			type = EntityType.HORSE;
		}else if (mobname.equalsIgnoreCase("skeletonhorse")){
			isSkeletonHorse = true;
			type = EntityType.HORSE;
		}
		
		if (type == null) {
			plugin.getLogger().warning("Invalid Mobtype for " + name + " given mobtype is: " + mobname);
			return null;
		}
		l.setPitch((float)Math.random());
		l.setYaw((float) Math.random());
		Entity entity = l.getWorld().spawnEntity(l, type);

		if (isWitherSkeleton) {
			((Skeleton) entity).setSkeletonType(SkeletonType.WITHER);
		} else if (isSuperCreeper) {
			((Creeper) entity).setPowered(true);
		}

		if (data != null && data != "") {
			if (entity instanceof Sheep && data != null) {
				((Sheep) entity).setColor(DyeColor.getByWoolData(Byte.parseByte(data)));
			} else if (data != null && entity instanceof Slime) { // MagmaCube extends Slime
				((Slime) entity).setSize(Integer.parseInt(data));
			} else if (mobname.equalsIgnoreCase("witherskeleton") || mobname.equalsIgnoreCase("wither_skeleton")) {
				((Skeleton) entity).setSkeletonType(SkeletonType.WITHER);
			} else if (entity instanceof Wolf) {
				((Wolf) entity).setCollarColor(DyeColor.getByDyeData(Byte.parseByte(data)));
			}
		}

		if (entity instanceof LivingEntity) {
			LivingEntity le = ((LivingEntity) entity);
			le.setCustomName(rarity + name);
			le.setCustomNameVisible(alwaysShowName);

			if(equipment != null)
				equipment.setEquipment(le);
			
			le.setMaxHealth(health);
			le.setHealth(health);
			if (isZombieVillager) {
				((Zombie) le).setVillager(true);
			}
			if (isBaby) {
				if (le instanceof Ageable) {
					((Ageable) le).setBaby();
					((Ageable) le).setAgeLock(true);
				} else if (le instanceof Zombie) {
					((Zombie) le).setBaby(true);

				}

			}
			if (isAngry && le instanceof Wolf) {
				((Wolf) le).setAngry(true);
			} else if (isAngry && le instanceof PigZombie) {
				((PigZombie) le).setAngry(true);
			}else if(le instanceof Horse){
				Horse horse = (Horse) le;
				horse.setTamed(true);
				if(isZombieHorse){
					horse.setVariant(Variant.UNDEAD_HORSE);
				}else if(isDonkey){
					horse.setVariant(Variant.DONKEY);
				}else if(isMule){
					horse.setVariant(Variant.MULE);
				}else if(isSkeletonHorse){
					horse.setVariant(Variant.SKELETON_HORSE);
				}
			}				

			if (passengerId != null) {
				if (plugin.getMobManager().contains(passengerId)) {
					Entity passenger = plugin.getMobManager().getMob(passengerId).spawnMob(l);
					if (passenger != null)
						le.setPassenger(passenger);
				} else {
					plugin.getLogger().warning("Passenger for " + name + " does not exist! Passenger: " + passengerId);
				}

			}
			KMobSpawnEvent spawnEvent = new KMobSpawnEvent(this, le);
			Bukkit.getServer().getPluginManager().callEvent(spawnEvent);
			
			activateSkills(SkillTrigger.onSpawn, le, le);
//			logDebugMessage("Mob successfully spawned!");
//			return le;
		}
		logDebugMessage("Mob successfully spawned!");
		return (LivingEntity) entity;
	}

	

	// --------------------------------- //
	// ---------- MOBDEATH ------------- //
	// --------------------------------- //
	
	public Integer calculateExpDrops(){
		if(expMax < 0 && expMin <0) return -1;
		if(expMax < 0) return expMin;
		if(expMin < 0) return expMax;
		if(expMax < expMin) return expMin;
		
		return (int) Math.round((double)expMin+Math.random()*((double)(expMax-expMin)));
	}
	

	// ------------------------------------------ //
	// ---------- GETTERS & SETTERS ------------- //
	// ------------------------------------------ //
	
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the filePath
	 */
	public String getFilePath() {
		return filePath;
	}

	/**
	 * @param filePath the filePath to set
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * @return the debugMob
	 */
	public boolean isDebugMob() {
		return debugMob;
	}


	/**
	 * @param debugMob the debugMob to set
	 */
	public void setDebugMob(boolean debugMob) {
		this.debugMob = debugMob;
	}


	/**
	 * @return the name without(!) rarity in front of it!
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the name inclusive color codes from rarity options
	 */
	public String getDisplayName() {
		return rarity + name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	public MobRarity getRarity() {
		return rarity;
	}

	public void setRarity(MobRarity rarity) {
		if (rarity == null)
			rarity = MobRarity.COMMON;
		this.rarity = rarity;
	}

	/**
	 * @return the health
	 */
	public double getHealth() {
		return health;
	}

	/**
	 * @param health
	 *            the health to set
	 */
	public void setHealth(double health) {
		this.health = health;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return mobtype;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String mobtype) {
		this.mobtype = mobtype;
	}
	
	public MobEquipment getEquipment(){
		if(equipment == null){
			this.equipment = new MobEquipment();
		}
		return equipment;
	}

	/**
	 * @return the expMax
	 */
	public int getExpMax() {
		return expMax;
	}

	/**
	 * @param expMax the expMax to set
	 */
	public void setExpMax(int expMax) {
		this.expMax = expMax;
	}

	/**
	 * @return the expMin
	 */
	public int getExpMin() {
		return expMin;
	}

	/**
	 * @param expMin the expMin to set
	 */
	public void setExpMin(int expMin) {
		this.expMin = expMin;
	}

	
	public void setEquipment(MobEquipment equipment){
		this.equipment = equipment;
	}
	
	public String getWorld(){
		return world;
	}
	public void setWorld(String world){
		this.world = world;
	}
	
	
	/**
	 * @return the biomes
	 */
	public List<String> getBiomes() {
		return biomes;
	}

	/** the biomes to set
	 * @param biomes
	 */
	public void setBiomes(List<String> biomes) {
		this.biomes = new ArrayList<String>();
		if(biomes == null) return;
		for(String biome: biomes){
			this.biomes.add(biome.toLowerCase());
		}
	}

	/**
	 * @return the minimal safezone distance
	 */
	public int getMinSafeZoneDistance() {
		return minSafeZoneDistance;
	}

	/**
	 * @param power
	 *            the distance the spawnpoint has to be away
	 */
	public void setMinSafeZoneDistance(int minSafeZoneDistance) {
		this.minSafeZoneDistance = minSafeZoneDistance;
	}

	/**
	 * @return the power
	 */
	public int getMaxSafeZoneDistance() {
		return maxSafeZoneDistance;
	}

	/**
	 * @param power
	 *            the power to set
	 */
	public void setMaxSafeZoneDistance(int maxSafeZoneDistance) {
		this.maxSafeZoneDistance = maxSafeZoneDistance;
	}

	/**
	 * @return the chance that the mob can spawn
	 */
	public double getSpawnChance() {
		return spawnChance;
	}

	/**
	 * @param spawnrate
	 *            the chance that the mob can spawn
	 */
	public void setSpawnChance(double spawnChance) {
		this.spawnChance = spawnChance;
	}

	/**
	 * @return the area
	 */
	public String getArea() {
		return area;
	}

	/**
	 * @param area
	 *            the area to set
	 */
	public void setArea(String area) {
		this.area = area;
	}

	/**
	 * @return the worldguardRegion
	 */
	public String getWorldguardRegion() {
		return worldguardRegion;
	}

	/**
	 * @param worldguardRegion
	 *            the worldguardRegion to set
	 */
	public void setWorldguardRegion(String worldguardRegion) {
		this.worldguardRegion = worldguardRegion;
	}

	/**
	 * @return the minY
	 */
	public int getMinHeight() {
		return minHeight;
	}

	/**
	 * @param minHeight
	 *            the minY to set
	 */
	public void setMinHeight(int minHeight) {
		this.minHeight = minHeight;
	}

	/**
	 * @return the maxY
	 */
	public int getMaxHeight() {
		return maxHeight;
	}

	/**
	 * @param maxHeight
	 *            the maxY to set
	 */
	public void setMaxHeight(int maxHeight) {
		this.maxHeight = maxHeight;
	}

	/**
	 * @return the alwaysShowName
	 */
	public boolean getAlwaysShowName() {
		return alwaysShowName;
	}

	/**
	 * @param showName
	 *            set
	 */
	public void setAlwaysShowName(boolean alwaysShowName) {
		this.alwaysShowName = alwaysShowName;
	}

	/**
	 * @return the mobtype
	 */
	public String getMobtype() {
		return mobtype;
	}

	/**
	 * @param mobtype
	 *            the mobtype to set
	 */
	public void setMobtype(String mobtype) {
		this.mobtype = mobtype;
	}

	/**
	 * @return the isBaby
	 */
	public boolean isBaby() {
		return isBaby;
	}

	/**
	 * @return the passengerId
	 */
	public String getPassengerId() {
		return passengerId;
	}

	/**
	 * @param passengerId
	 *            the passengerId to set
	 */
	public void setPassengerId(String passengerId) {
		this.passengerId = passengerId;
	}

	/**
	 * @param isBaby
	 *            the isBaby to set
	 */
	public void setBaby(boolean isBaby) {
		this.isBaby = isBaby;
	}

	public void addDrop(Drop drop){
		drops.add(drop);
	}
	public void setDrops(List<Drop> drops){
		if(drops == null) 
			drops = new ArrayList<Drop>();
		this.drops = drops;
	}
	
	public List<Drop> getDrops(){
		return drops;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "KMob [plugin=" + plugin + ", id=" + id + ", name=" + name + ", rarity=" + rarity + ", alwaysShowName="
				+ alwaysShowName + ", health=" + health + ", mobtype=" + mobtype + ", isBaby=" + isBaby
				+ ", passengerId=" + passengerId + ", expMax=" + expMax + ", expMin=" + expMin + ", equipment="
				+ equipment + ", drops=" + drops + ", world=" + world + ", area=" + area + ", spawnChance="
				+ spawnChance + ", worldguardRegion=" + worldguardRegion + ", biomes=" + biomes + ", minHeight="
				+ minHeight + ", maxHeight=" + maxHeight + ", minSafeZoneDistance=" + minSafeZoneDistance
				+ ", maxSafeZoneDistance=" + maxSafeZoneDistance + ", skills=" + skills + "]";
	}

	public HandlerList getHandlers(Class<? extends KMobEvent> class1) {
		HandlerList handlerList = eventHandlers.get(class1.getName());
		if(handlerList == null) return new HandlerList();
		return handlerList;
	}
	
	public void registerEventHandler(Class<? extends KMobEvent> class1, RegisteredListener listener){
		String className = class1.getName();
		if(eventHandlers.containsKey(className)){
			eventHandlers.get(className).register(listener);
		} else {
			HandlerList handlers  = new HandlerList();
			handlers.register(listener);
			eventHandlers.put(className, handlers);
		}
		
	}
	
}
