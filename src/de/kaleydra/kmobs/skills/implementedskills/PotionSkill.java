package de.kaleydra.kmobs.skills.implementedskills;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.kaleydra.licetia.LiCETIA;

import de.kaleydra.kmobs.skills.InvalidSkillException;
import de.kaleydra.kmobs.skills.TriggeredSkill;

@SerializableAs("PotionSkill")
public class PotionSkill extends TriggeredSkill {
	
	String type;
	int amplifier		= 1;
	int duration 		= 1;
	int radius;
	boolean isAmbient 	= false;
	PotionEffect effect;
	
	public PotionSkill(ConfigurationSection section) throws InvalidSkillException {	
		super("potion",section);
		type = section.getString("type");
		String potionType = section.getString("potionType");
		duration = section.getInt("duration",20);
		amplifier = section.getInt("amplifier", 1);
		isAmbient = section.getBoolean("isAmbient", true);
		radius = section.getInt("radius", 15);
		
		if(!type.equalsIgnoreCase("direct") && !type.equalsIgnoreCase("self")
				&& !type.equalsIgnoreCase("area")){
			throw new InvalidSkillException("Invalid skill type: "+type);			
		}
		
		if(potionType == null){
			throw new InvalidSkillException("No potion type given!");
		}
		PotionEffectType potion = PotionEffectType.getByName(potionType);
		if(potion == null){
			throw new InvalidSkillException("invalid potion type: "+potionType);
		}
		effect = new PotionEffect(potion, duration, amplifier, isAmbient);	
	}

	@Override
	public void activate(LivingEntity entity,LivingEntity target) {
		if(type.equalsIgnoreCase("self")){
			entity.addPotionEffect(effect, true);
		} else if(type.equalsIgnoreCase("area")){
			for(Player player : LiCETIA.getNearbyPlayers(entity, radius)){
				player.addPotionEffect(effect,true);
			}
		} else if(type.equalsIgnoreCase("direct")){
			if(target != null)
				target.addPotionEffect(effect,true);
		}
	}
	
	@Override
	public ConfigurationSection serializeToSection(ConfigurationSection section){
		section.set("skill", "potion");
		section = super.serializeToSection(section);
		section.set("duration", duration);
		section.set("amplifier", amplifier);
		return section;
	}
}
