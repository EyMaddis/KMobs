package de.kaleydra.kmobs.skills;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;

import de.kaleydra.kmobs.KMobs;

public class SkillManager {
	Map<String, Class<? extends TriggeredSkill>> skillClasses = new HashMap<String, Class<? extends TriggeredSkill>>();
	KMobs plugin;
	
	public SkillManager(KMobs plugin){
		this.plugin = plugin;
	}
	
	public boolean contains(String identifier){
		return skillClasses.containsKey(identifier.toLowerCase());
	}
	
	public TriggeredSkill getSkillInstance(String identifier, ConfigurationSection section) throws InvalidSkillException{
		Class<? extends TriggeredSkill> skillClass = skillClasses.get(identifier.toLowerCase());
		if(skillClass == null) throw new InvalidSkillException("Unknown Skill \""+identifier+"\"!");
		try {
			return skillClass.getDeclaredConstructor(ConfigurationSection.class).newInstance(section);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void register(String identifier,Class<? extends TriggeredSkill> skillClass){
		skillClasses.put(identifier.toLowerCase(), skillClass);
	}
}
