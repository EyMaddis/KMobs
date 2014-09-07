package de.kaleydra.kmobs.skills.implementedskills;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.kaleydra.licetia.LiCETIA;

import de.kaleydra.kmobs.KMobs;
import de.kaleydra.kmobs.skills.InvalidSkillException;
import de.kaleydra.kmobs.skills.TriggeredSkill;

public class ForcePushSkill extends TriggeredSkill {
	
	String type;
	double power;
	double radius;
	double sneakedPower;
	
	public ForcePushSkill(ConfigurationSection section) throws InvalidSkillException {
		super("forcepush",section);
		
		type = section.getString("type", "area");
		if(!type.equalsIgnoreCase("area") && !type.equalsIgnoreCase("direct") && !type.equalsIgnoreCase("self")){
			throw new InvalidSkillException("invalid type: "+type);
		}
		power = section.getDouble("power", 0.5);
		if(power > 5.0) {
			power = 5.0;
		} else if(power < -5.0) power = -5.0;
		radius = section.getDouble("radius", 5);
		sneakedPower = section.getDouble("sneakedPower", 0);
		if(sneakedPower > 5.0) {
			sneakedPower = 5.0;
		} else if(sneakedPower < -5.0) sneakedPower = -5.0;
	}

	@Override
	public void activate(LivingEntity entity, LivingEntity target) {
		Location location = entity.getEyeLocation();
		if(type.equalsIgnoreCase("area")){
			for(Player player:LiCETIA.getNearbyPlayers(entity, radius)){
				push(location, player);			
			}	
		}else { // direct
			if(target == null){
				KMobs.logDebugMessage("target for direct "+getIdentifier()+" is null!");
				return;
			}
			if(type.equalsIgnoreCase("self")){
				push(target.getEyeLocation(),entity);
			} else {
				push(location,target);
			}
		}
	}
	
	private void push(Location source, LivingEntity entity){
		double x = source.getX();
		double y = source.getY();
		double z = source.getZ();
		double power = this.power;
		if((entity instanceof Player) && ((Player)entity).isSneaking()) power=sneakedPower;  // && sneakedPower != 0.0 disabled
		if(power == 0) return;
		
		Vector vector = new Vector(entity.getLocation().getX()-x, entity.getLocation().getY()-y, entity.getLocation().getZ()-z);
		vector.normalize();
		
		entity.setVelocity(vector.multiply(power));
	}
}
