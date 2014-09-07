package de.kaleydra.kmobs;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import de.kaleydra.kmobs.commands.KMobCommand;
import de.kaleydra.kmobs.commands.KMobCommandTabCompleter;
import de.kaleydra.kmobs.drops.Drop;
import de.kaleydra.kmobs.listener.MobListener;
import de.kaleydra.kmobs.listener.PlayerListener;
import de.kaleydra.kmobs.metadatavalues.TownDistanceMetaValue;
import de.kaleydra.kmobs.mobs.MobEquipment;
import de.kaleydra.kmobs.mobs.MobManager;
import de.kaleydra.kmobs.radiuschecker.PlayerTaskManager;
import de.kaleydra.kmobs.skills.SkillManager;
import de.kaleydra.kmobs.skills.implementedskills.BlizzardSkill;
import de.kaleydra.kmobs.skills.implementedskills.CommandSkill;
import de.kaleydra.kmobs.skills.implementedskills.DespawnSkill;
import de.kaleydra.kmobs.skills.implementedskills.DismountSkill;
import de.kaleydra.kmobs.skills.implementedskills.EffectSkill;
import de.kaleydra.kmobs.skills.implementedskills.ExplosionSkill;
import de.kaleydra.kmobs.skills.implementedskills.ForcePushSkill;
import de.kaleydra.kmobs.skills.implementedskills.LightningSkill;
import de.kaleydra.kmobs.skills.implementedskills.MessageSkill;
import de.kaleydra.kmobs.skills.implementedskills.ParticleSkill;
import de.kaleydra.kmobs.skills.implementedskills.PotionSkill;
import de.kaleydra.kmobs.skills.implementedskills.SpawnSkill;

public class KMobs extends JavaPlugin{
	SkillManager skillManager;
	MobManager mobManager;
	WorldGuardPlugin worldGuard;
	PlayerTaskManager playerTaskManager;
	
	boolean useTowny = false;
	
	private static KMobs instance;
	private static List<String> worldWhitelist = new ArrayList<String>();
	boolean debugMode = false;
	
	boolean blockAllCactusDamage = false;
	String defaultWorld;
	
	@Override
	public void onEnable(){
		
		// TODO: Config Klasse!
		
		
		instance = this;
		skillManager = new SkillManager(this);
		
		// should always get registered before the mobs are loaded.
		skillManager.register("command",	CommandSkill.class);
		skillManager.register("effect",  	EffectSkill.class);
		skillManager.register("forcepush", 	ForcePushSkill.class);
		skillManager.register("message", 	MessageSkill.class);
		skillManager.register("potion", 	PotionSkill.class);
		skillManager.register("spawn", 		SpawnSkill.class);
		skillManager.register("lightning",	LightningSkill.class);
		skillManager.register("blizzard",	BlizzardSkill.class);
		skillManager.register("explosion",	ExplosionSkill.class);
		skillManager.register("despawn",	DespawnSkill.class);
		skillManager.register("particle",	ParticleSkill.class);
		skillManager.register("dismount",	DismountSkill.class);

		ConfigurationSerialization.registerClass(MobEquipment.class, "MobEquipment");
		ConfigurationSerialization.registerClass(Drop.class, "Drop");
		

		getConfig().addDefault("useTowny", false);
		getConfig().addDefault("blockAllDrops", false);
		getConfig().addDefault("regionSpawnOnly", false);
		getConfig().addDefault("enableMobspawners", false);
		getConfig().addDefault("useAnimalSpawns", false);
		getConfig().addDefault("minDamageRatioFromPlayer", 0.5);
		getConfig().addDefault("defaultWorld", "world");
		getConfig().addDefault("maxPlayerHealth.maxHealth", 10.0);
		getConfig().addDefault("maxPlayerHealth.onlyAtFirstSpawn", false);
		getConfig().addDefault("debugMode", false);
		getConfig().addDefault("mobs.blockAllCactusDamage", false);
		
		getConfig().addDefault("worldWhitelist", Arrays.asList("world", "world_nether", "world_the_end"));
//		getConfig().addDefault("worldCenter", value)
		getConfig().options().copyDefaults(true);
		loadConfig();
		
		useTowny = getConfig().getBoolean("useTowny");
		
		if(useTowny) useTowny = Bukkit.getPluginManager().getPlugin("Towny") != null;
		
		if(useTowny){
			getLogger().info("Enable Towny features");
		} else {
			getLogger().info("Towny not found, disabling towny features!");
		}
		
		getCommand("kmob").setExecutor(new KMobCommand(this));
		getCommand("kmob").setTabCompleter(new KMobCommandTabCompleter(this));
		
		mobManager = new MobManager(this);
		worldGuard = (WorldGuardPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
		
		playerTaskManager = new PlayerTaskManager(this);
		
		(new File(getDataFolder()+"/mobs/")).mkdirs();
		mobManager.load(getDataFolder()+"/mobs/");
		getServer().getPluginManager().registerEvents(new MobListener(this), this);
		getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
		
	}
	
	@Override
	public void onDisable(){
		instance = null;
		saveConfig();
	}
	
	@Override
	public void reloadConfig() {
		super.reloadConfig();
		saveConfig(); // adds missing defaults
		worldWhitelist.clear();
		loadConfig();
	}
	
	public void loadConfig(){
		this.debugMode = getConfig().getBoolean("debugMode");	
		this.blockAllCactusDamage = getConfig().getBoolean("mobs.blockAllCactusDamage");
		this.defaultWorld = getConfig().getString("defaultWorld");
		worldWhitelist.addAll(getConfig().getStringList("worldWhitelist"));
	}
	
	public SkillManager getSkillManager(){
		return skillManager;
	}
	
	public MobManager getMobManager() {
		return mobManager;
	}
	public WorldGuardPlugin getWorldGuard() {
		return worldGuard;
	}
	public PlayerTaskManager getPlayerTaskManager() {
		return playerTaskManager;
	}

	public boolean isInDebugMode(){
		return debugMode;
	}
	public boolean blockAllCactusDamage(){
		return blockAllCactusDamage;
	}
	
	public static String getAreaName(Location l){
		if (l.getZ() < 0){ // NORTH
			if(l.getX() < 0){ // NORTHWEST
				return "NORTHWEST";
			} else { // NORTHEAST
				return "NORTHEAST";
			}
		} else { // SOUTH
			if(l.getX() < 0){ // SOUTHHWEST
				return "SOUTHWEST";
			} else { // SOUTHEAST
				return "SOUTHWEST";
			}
		}
	}
	
	public static double calculateNearestTownDistance(Location l) throws TownyException {
		return calculateNearestTownDistance(TownyUniverse.getDataSource().getTowns(),l);
	}

	public static double calculateNearestTownDistance(List<Town> towns, Location l) throws TownyException {
		Block block = l.getChunk().getBlock(0, 0, 0);
		
		if(block.hasMetadata("kmobs_distance")){
			List<MetadataValue> values = block.getMetadata("kmobs_distance");
			if(values.size() > 0){
				
				double d = values.get(0).asDouble();
				Bukkit.getLogger().info("Found existing distance "+d+" for "+l);
				return d;
			} else 
				Bukkit.getLogger().warning("Distance MetaData not properly saved! hasMetadata == true but size 0! At "+l);
		}
		
		double minDistance = -1;
		int i = 0;
		double currentDistance = -1;
		for (Town town : towns) {
		
			currentDistance = calculateDistanceTownToPlayer(town, l);
			if (i == 0) {
				minDistance = currentDistance;
			}else if(currentDistance<currentDistance){
				minDistance = currentDistance;
			}
			i++;

			
		}
		block.removeMetadata("kmobs_distance", getInstance());
		block.setMetadata("kmobs_distance", new TownDistanceMetaValue(minDistance));
		return minDistance;
	}
	
	// static
	public static boolean isOnWhitelist(World world){
		return isOnWhitelist(world.getName());
	}
	
	public static boolean isOnWhitelist(String world){
		return worldWhitelist.contains(world);
	}

	public static double calculateDistanceTownToPlayer(Town town, Location l)
			throws TownyException {
		int x = town.getHomeBlock().getCoord().getX();
		int z = town.getHomeBlock().getCoord().getZ();
		
		int px = l.getChunk().getX();
		int pz = l.getChunk().getZ();
		

		return Math.sqrt(Math.pow((x - px), 2) + Math.pow((z - pz), 2));
	}
	
	
	/**
	 * string to lowercase, strip colors and remove spaces
	 * @param string
	 * @return
	 */
	public static String nameToId(String string){
		string = ChatColor.stripColor(string);
		string = string.replaceAll("\\s+", "");
		string = string.toLowerCase();
		return 	string;
	}
	
	public boolean useTowny(){
		return useTowny;
	}
	
	/**
	 * @return the defaultWorld
	 */
	public String getDefaultWorld() {
		return defaultWorld;
	}

	public static KMobs getInstance(){
		return instance;
	}
	
	public static void logDebugMessage(String message){
		if(instance.isInDebugMode())
			instance.getLogger().info("[DEBUG] "+message);
	}
}
