package de.kaleydra.kmobs.skills.implementedskills;

import org.bukkit.Effect;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;

import de.kaleydra.kmobs.skills.InvalidSkillException;
import de.kaleydra.kmobs.skills.TriggeredSkill;

public class EffectSkill extends TriggeredSkill {
	String type = null;
	Effect effect = null;
	int data;
	int radius;
	
	public EffectSkill(ConfigurationSection section) throws InvalidSkillException {
		super("effect",section);
		
		type = section.getString("type");
		if(type == null) throw new InvalidSkillException("missing effect type!");
		effect = Effect.valueOf(type);
		if(effect == null) throw new InvalidSkillException("invalid effect type '"+type+"'!");
		data = section.getInt("data",0);
		radius = section.getInt("radius", 35);		
	}

	

	@Override
	public void activate(LivingEntity entity, LivingEntity target) {
		entity.getLocation().getWorld().playEffect(entity.getLocation(), effect, data, radius);
	}
	
	@Override
	public ConfigurationSection serializeToSection(ConfigurationSection section){
		section.set("skill", "effect");
		section = super.serializeToSection(section);
		section.set("effect", effect.getType().name());
		section.set("data", data);
		section.set("radius", radius);
		
		return section;		
	}
}
