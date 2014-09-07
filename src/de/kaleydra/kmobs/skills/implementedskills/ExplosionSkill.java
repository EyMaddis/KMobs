package de.kaleydra.kmobs.skills.implementedskills;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;

import com.kaleydra.licetia.LiCETIA;

import de.kaleydra.kmobs.skills.InvalidSkillException;
import de.kaleydra.kmobs.skills.TriggeredSkill;

public class ExplosionSkill extends TriggeredSkill {
	
	int radius;
	float power;
	boolean setFire;
	String type;
	int amount;
	
	public ExplosionSkill(ConfigurationSection section) throws InvalidSkillException {
		super("explosion", section);
		radius = section.getInt("radius", 15);
		type = section.getString("type", "self");
		power = (float)section.getDouble("power", 2.0);
		setFire = section.getBoolean("setFire", false);
		amount = section.getInt("amount", 4);
		if(!type.equalsIgnoreCase("direct") && !type.equalsIgnoreCase("area") 
				&& !type.equalsIgnoreCase("self")){
			throw new InvalidSkillException("unknown type");
		}
	}
	
	@Override
	public void activate(LivingEntity entity, LivingEntity target) {
		if(type.equalsIgnoreCase("direct")){
			if(target == null || !(target instanceof LivingEntity)) return;
			explode(target.getLocation());
		}
		else if(type.equalsIgnoreCase("area")){
			List<Location> points = LiCETIA.getCircleCircumferenceLocations(entity.getLocation(), radius, amount);
			for(Location loc:points){
				explode(loc);
			}
		} else { // suicide/self
			explode(entity.getLocation());
		}
	}
	
	private void explode(final Location location){
		location.getWorld().createExplosion(location.getX(),location.getY(),location.getZ(),power,setFire,false);
	}
}
