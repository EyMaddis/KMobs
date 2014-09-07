package de.kaleydra.kmobs.mobs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;

import de.kaleydra.kmobs.KMobs;
import de.kaleydra.kmobs.drops.Drop;
import de.kaleydra.kmobs.skills.InvalidSkillException;
import de.kaleydra.kmobs.skills.SkillData;
import de.kaleydra.kmobs.skills.SkillManager;
import de.kaleydra.kmobs.skills.SkillTrigger;
import de.kaleydra.kmobs.skills.TriggeredSkill;

public class MobManager {

	private KMobs plugin;

	Map<String, KMob> 		mobById 			= new HashMap<String, KMob>();
	Map<String, KMob> 		mobByName 			= new HashMap<String, KMob>();
	List<KMob> 				mobsWithoutRegion 	= new ArrayList<KMob>();
//	Map<String, List<KMob>> mobsByBiome 		= new HashMap<String, List<KMob>>();
	Map<String,List<KMob>> 	mobsByRegion 		= new HashMap<String, List<KMob>>();

	
	public MobManager(KMobs plugin) {
		this.plugin = plugin;
	}

	public void addMob(KMob mob) {
		mobById.put(mob.getId(), mob);
		mobByName.put(mob.getRarity()+mob.getName(), mob);
		
//		for(String biome:mob.getBiomes()){
//			if (!mobsByBiome.containsKey(biome))
//				mobsByBiome.put(biome, new ArrayList<KMob>());
//			mobsByBiome.get(biome).add(mob);
//		}
				
		if(mob.getWorldguardRegion() == null){
			mobsWithoutRegion.add(mob);
			return;
		}
		if(mobsByRegion.containsKey(mob.getWorldguardRegion())){
			mobsByRegion.get(mob.getWorldguardRegion()).add(mob);
		} else {
			List<KMob> l = new ArrayList<KMob>();
			l.add(mob);
			mobsByRegion.put(mob.getWorldguardRegion(), l);
		}
			
	}
	
	public List<String> getMobIds(){
		return new ArrayList<String>(mobById.keySet());
	}

	public boolean contains(String mobId) {
		return mobById.containsKey(mobId);
	}
	public boolean containsByName(String mobName) {
		mobById.isEmpty();
		return mobByName.containsKey(mobName);
	}
	
	public List<KMob> getMobsWithoutRegion(){
		return mobsWithoutRegion;
	}

	/**
	 * get all mobs (except region based)
	 * @return
	 */
	public Collection<KMob> getAllMobs() {
		return mobById.values();
	}
	/**
	 * gets all mobs for a specific region
	 * @param regionId
	 * @return
	 */
	public List<KMob> getRegionMobs(String regionId){
		if(mobsByRegion.containsKey(regionId)){
			return mobsByRegion.get(regionId);
		}
		return new ArrayList<KMob>();
	}

	public KMob getMob(String mobId) {
		return mobById.get(mobId);
	}
	
	public KMob getMobByName(String mobName){
		return mobByName.get(mobName);
	}
	
	/**
	 * 
	 * @param e
	 * @return null if not a KMob
	 */
	public KMob getMobByEntity(LivingEntity entity){
		if(entity == null) return null;
		String name = entity.getCustomName();
		if(name == null) return null;
		if(!containsByName(name)) return null;
		
		return getMobByName(entity.getCustomName());
	}

	public void load(String mobFolder) {
		mobById.clear();
		mobByName.clear();
//		mobsByBiome.clear();
		mobsByRegion.clear();
		mobsWithoutRegion.clear();
		
		
		File folder = new File(mobFolder);
		
		//TODO: Recursive to enable multiple folders
		List<File> listOfMobFiles = getMobFiles(folder);//folder.listFiles(new MobFileFilter());
		
//		YamlConfiguration currentYML;
//		KMob currentMob;
		for (File mobFile : listOfMobFiles) {
			Exception exception = null;
			try {
				loadMobFile(mobFile);
			} catch (InvalidMobConfigException e) {
				exception = e;
			} catch (InvalidSkillException e) {
				exception = e;
			}
			if(exception == null) continue;
			plugin.getLogger().severe("=============== MOBLOAD EXCEPTION ================");
			plugin.getLogger().severe("Error Message: "+exception.getMessage());
			logCauses(exception.getCause());
			plugin.getLogger().severe("==================================================");
		}

		plugin.getLogger().info("Loaded " + mobById.size() + " custom mobs and for "+mobsByRegion.size()+" specific regions");
	}

	@SuppressWarnings("unchecked")
	private void loadMobFile(File mobFile) throws InvalidMobConfigException, InvalidSkillException {

		String defaultWorld = plugin.getDefaultWorld();

		YamlConfiguration currentYML = new YamlConfiguration();
		try {
			currentYML.load(mobFile);
		} catch (Exception e) {
			throw new InvalidMobConfigException("MobFile Exception at " + mobFile.getName());
		}
		
		String name  = currentYML.getString("name");
		if(name == null){
			throw new InvalidMobConfigException("MobFile Exception at " + mobFile.getName() + "! Missing name value!");
		}
		name 		 = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', currentYML.getString("name")));
		

		boolean debugMob = currentYML.getBoolean("debugMob",false);
		
		Boolean alwaysShowName = currentYML.getBoolean("alwaysShowName", false);
		
		String mobRarity = currentYML.getString("rarity");
		
		double health = currentYML.getDouble("health", 10);
		String mobtype = currentYML.getString("mobtype");
		if(mobtype==null){
			throw new InvalidMobConfigException("MobFile Exception at " + mobFile.getName() + "! Missing mobtype value!");
		}
		boolean isBaby = currentYML.getBoolean("isBaby", false);
		String passengerId = currentYML.getString("passengerId");

		// equipment
		
		MobEquipment equip = (MobEquipment) currentYML.get("equipment");

		List<Drop> drops;
		if(currentYML.contains("drops")){
			drops = (List<Drop>) currentYML.getList("drops");
		} else {
			drops = new ArrayList<Drop>();
		}
		

		int expMax = currentYML.getInt("expMax", -1);
		int expMin = currentYML.getInt("expMin", -1);
		
		// spawn conditions
		String world = currentYML.getString("spawn.world", defaultWorld);
		double spawnChance = currentYML.getDouble("spawn.chance",0);
		String worldguardRegion = currentYML.getString("spawn.worldguardRegion");
		String area = currentYML.getString("spawn.area");
		List<String> biomes = currentYML.getStringList("spawn.biomes");
		int maxHeight = currentYML.getInt("spawn.maxHeight");
		int minHeight = currentYML.getInt("spawn.minHeight");
		int minSafeZoneDistance = currentYML.getInt("spawn.minSafeZoneDistance");
		int maxSafeZoneDistance = currentYML.getInt("spawn.maxSafeZoneDistance");

		if (name == null) {
			throw new InvalidMobConfigException("Missing name for " + mobFile.getName());
		} else if (spawnChance == 0) {
			plugin.getLogger().info("Spawn chance for " + mobFile.getName() + " is zero! Mob won't spawn naturally");
		} else if (spawnChance < 0.0) {
			throw new InvalidMobConfigException("Spawn chance for " + mobFile.getName() + " can not be below 0. Is currently "+spawnChance);
		}
		KMob currentMob = new KMob(plugin, getMobIdFromFile(mobFile), mobFile.getPath()); //.getName().split("\\.(?=[^\\.]+$)")[0]);
		currentMob.setName(name);
		
		currentMob.setDebugMob(debugMob);
		
		currentMob.setAlwaysShowName(alwaysShowName);
		
		currentMob.setRarity(MobRarity.getRarityFromName(mobRarity));
		currentMob.setExpMax(expMax);
		currentMob.setExpMin(expMin);
		
		currentMob.setHealth(health);
		currentMob.setMobtype(mobtype);
		currentMob.setBaby(isBaby);
		currentMob.setPassengerId(passengerId);
		
		currentMob.setEquipment(equip);
		
		for(Drop drop:drops)
			currentMob.addDrop(drop);

		currentMob.setWorld(world);
		currentMob.setSpawnChance(spawnChance);
		currentMob.setWorldguardRegion(worldguardRegion);
		currentMob.setArea(area);
		currentMob.setBiomes(biomes);
		currentMob.setMaxHeight(maxHeight);
		currentMob.setMinHeight(minHeight);
		currentMob.setMinSafeZoneDistance(minSafeZoneDistance);
		currentMob.setMaxSafeZoneDistance(maxSafeZoneDistance);
		
//		try {
			loadSkills(currentMob, currentYML.getConfigurationSection("skills"));
//		} catch (InvalidSkillException e) {
//			plugin.getLogger().severe("=============== SKILL EXCEPTION ================");
//			plugin.getLogger().severe(ChatColor.RED+"Could not load skills for: "+name);
//			plugin.getLogger().severe(ChatColor.RED+"Error message: "+e.getMessage());
//			Throwable cause = e.getCause();
//			logCauses(cause);
//			plugin.getLogger().severe("================================================");
//			
//		}
		addMob(currentMob);
		KMobs.logDebugMessage("Mob with id " + currentMob.getId() + " (" + currentMob.getName() 
				+ ChatColor.RESET.toString()+ ") loaded");
		
	}

	public void loadSkills(KMob mob, ConfigurationSection skillsSection) throws InvalidSkillException {
		if(skillsSection == null) return;
		
		SkillManager skillManager = plugin.getSkillManager();
		ConfigurationSection skillTypeSection;
		int interval, radius, cooldown;
		long delay;
		
		for(String key: skillsSection.getKeys(false)){
			KMobs.logDebugMessage("Mob "+mob.getId()+" uses event: "+key);		
			SkillTrigger skillTrigger;
			try{
				skillTrigger = SkillTrigger.valueOf(key);
				
			} catch (IllegalArgumentException e){
				skillTrigger = null;
			}
			if(skillTrigger == null){
				plugin.getLogger().warning("Mob "+mob.getId()+" tries to use a non-existing event \""+key+"\"!");				
				continue;
			}
			skillTypeSection = skillsSection.getConfigurationSection(skillTrigger.toString());
			ConfigurationSection currentSection;
			TriggeredSkill skill = null;
			if(skillTypeSection == null) continue;
			
			for(String skillKey:skillTypeSection.getKeys(false)){
				currentSection = skillTypeSection.getConfigurationSection(skillKey);
									
				if(currentSection == null) continue;
				if(!currentSection.contains("skill")){
					plugin.getLogger().warning("Mob "+mob.getId()+" has an invalid skill! Missing skill definition!");
					continue;
				}

				delay = currentSection.getLong("delay", 0);
				cooldown = currentSection.getInt("cooldown", 0);
				
				// identify and load skills!
				String skillName = currentSection.getString("skill");
				if(skillName == null){
					throw new InvalidSkillException("Skill name is missing!");
				}
				
				if(!skillManager.contains(skillName)){
					throw new InvalidSkillException("Unknown skill: "+skillName);
				}
				skill = skillManager.getSkillInstance(skillName, currentSection);
				if(skill == null) 
					throw new InvalidSkillException("Unable to register skill \""+skillName+"\"! Please check all spawn parameters for mob: "+mob.getId());
				
				if(skillTrigger.equals(SkillTrigger.onInterval)){
					interval = currentSection.getInt("interval", -1);
					radius = currentSection.getInt("intervalRadius", 35);

					if(interval < 1) 
						throw new InvalidSkillException("Missing or invalid interval in \""+skillName+"\" ("+mob.getId()+")!");
					mob.addSkill(SkillTrigger.onInterval, new SkillData(key+"."+skillKey, skill, cooldown, delay, interval, radius));
				}
				else mob.addSkill(skillTrigger,new SkillData(key+"."+skillKey, skill, cooldown, delay));
			
				skill = null;
			}
			
		}
		
	}
	
	/**
	 * saves equipment and drops
	 * @param mob
	 * @return
	 */
	public boolean saveItems(KMob mob){		
		File ymlFile = new File(mob.getFilePath());
		if(!ymlFile.exists()) return false;
		YamlConfiguration yml = new YamlConfiguration();
		
		try {
			yml.load(ymlFile);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		yml.set("equipment", mob.getEquipment());
		yml.set("drops", mob.getDrops());
		
		try {
			yml.save(ymlFile);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void save(KMob mob) {
		File ymlFile = new File(mob.getFilePath());

		if (!ymlFile.exists()) {

			try {
				ymlFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		YamlConfiguration yml = new YamlConfiguration();
		try {
			yml.load(ymlFile);

			yml.set("name", mob.getName());
			yml.set("alwaysShowName", mob.getAlwaysShowName());
			yml.set("health", mob.getHealth());
			yml.set("damage", mob.getHealth());
			yml.set("mobtype", mob.getMobtype());
			yml.set("isBaby", mob.isBaby());
			yml.set("passengerId", mob.getPassengerId());

			yml.set("equipment", mob.getEquipment());
			
			yml.set("spawn.spawnChance", mob.getSpawnChance());
			yml.set("spawn.worldguardRegion", mob.getWorldguardRegion());
			yml.set("spawn.area", mob.getArea());
			yml.set("spawn.biomes", mob.getBiomes());
			yml.set("spawn.minHeight", mob.getMinHeight());
			yml.set("spawn.maxHeight", mob.getMaxHeight());
			yml.set("spawn.minSafeZoneDistance", mob.getMinSafeZoneDistance());
			yml.set("spawn.maxSafeZoneDistance", mob.getMaxSafeZoneDistance());
			
			
			yml.save(ymlFile);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void logCauses(Throwable cause){
		while(cause != null){
			plugin.getLogger().severe(ChatColor.RED+"Caused by: "+cause.getMessage());
			cause = cause.getCause();
		}
	}
	
	/**
	 * loads .mob files recursively
	 * @param baseFolder the mobfolder to start.
	 * @return
	 */
	private List<File> getMobFiles(File baseFolder){
		ArrayList<File> output = new ArrayList<File>();
		for(File file:baseFolder.listFiles(new MobFileFilter())){
			if(file.isDirectory()){
				output.addAll(getMobFiles(file));
			} else{
				output.add(file);
			}
		}
		return output;
	}
	
	private String getMobIdFromFile(@Nonnull File file){
		String fileName = file.getPath();
		String basePath = new File(plugin.getDataFolder().getPath()+"/mobs/").getPath();
		
		fileName = fileName.substring(basePath.length()+1);
		fileName = fileName.replace(File.separatorChar, '.');
		fileName = fileName.replace(' ', '_');
		fileName = fileName.substring(0, fileName.length()-".mob".length());
		return fileName.toLowerCase();
	}

}
