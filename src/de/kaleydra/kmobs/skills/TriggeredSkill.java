package de.kaleydra.kmobs.skills;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;


public abstract class TriggeredSkill{
	String identifier;
	//percentages
	double chance = 1.0;
	double minHealth = 0; 
	double maxHealth = 1.0;

	public TriggeredSkill(String identifier,ConfigurationSection section) throws InvalidSkillException{
		//throw new InvalidSkillException("invalid skill constructor");
		this.identifier = identifier;
		if(identifier == null || identifier.isEmpty()) throw new InvalidSkillException("missing identifier!");
		chance = section.getDouble("chance", 1.0);
		minHealth = section.getDouble("minHealth", 0);
		maxHealth = section.getDouble("maxHealth", 1.0);
	}

	public abstract void activate(LivingEntity mob, LivingEntity target);

	/**
	 * should get overwritten and the overwriting method should call super.serializeToSection()!
	 * @param section
	 * @return the updated section
	 */
	public ConfigurationSection serializeToSection(ConfigurationSection section) {
		section.set("skill", getIdentifier());
		section.set("chance", chance);
		section.set("minHealth", minHealth);
		section.set("maxHealth", maxHealth);
		return section;
	}
	public double getChance(){
		return chance;
	}
	public double getMinHealth(){
		return minHealth;
	}
	public double getMaxHealth(){
		return maxHealth;
	}
	
	public String getIdentifier(){
		return identifier;
	}
}
