package de.kaleydra.kmobs.skills.implementedskills;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import de.kaleydra.kmobs.skills.InvalidSkillException;
import de.kaleydra.kmobs.skills.TriggeredSkill;

public class LightningSkill extends TriggeredSkill {
	
	int radius;
	String type;
	
	public LightningSkill(ConfigurationSection section) throws InvalidSkillException {
		super("lightning", section);
		radius = section.getInt("radius", 15);
		type = section.getString("type", "direct");
		if(!type.equalsIgnoreCase("direct") && !type.equalsIgnoreCase("area")){
			throw new InvalidSkillException("unknown type");
		}
	}

	@Override
	public void activate(LivingEntity entity, LivingEntity target) {
		if(type.equalsIgnoreCase("direct")){
			if(target == null || !(target instanceof LivingEntity)) return;
			target.getWorld().strikeLightning(target.getLocation());
		}
		else{
			for(Entity e:entity.getNearbyEntities(radius, radius, radius)){
				if(!(e instanceof Player)) continue;
				e.getLocation().getWorld().strikeLightning(e.getLocation());
			}
		}
		
	}
}
