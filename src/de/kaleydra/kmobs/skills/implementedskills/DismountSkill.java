package de.kaleydra.kmobs.skills.implementedskills;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import de.kaleydra.kmobs.skills.InvalidSkillException;
import de.kaleydra.kmobs.skills.TriggeredSkill;

public class DismountSkill extends TriggeredSkill {
	
	String type;
	boolean despawnVehicle;
	boolean despawnPassenger;
	
	public DismountSkill(ConfigurationSection section) throws InvalidSkillException {
		super("dismount",section);
		
		type = section.getString("type", "passenger");
		if(!type.equalsIgnoreCase("passenger") && !type.equalsIgnoreCase("vehicle") 
				&& !type.equalsIgnoreCase("vehicle") && !type.equalsIgnoreCase("vehicleDespawn")){
			throw new InvalidSkillException("invalid type: "+type);
		}
	}

	@Override
	public void activate(LivingEntity entity, LivingEntity target) {
		if(type.equalsIgnoreCase("passenger")){
			entity.eject();	
		} else if(type.equalsIgnoreCase("vehicle")) { // vehicle
			entity.leaveVehicle();
		} else if(type.equalsIgnoreCase("passengerDespawn")){
			Entity passenger = entity.getPassenger();
			if(passenger != null) passenger.remove();
		} else { // vehicleDespawn
			Entity vehicle = entity.getVehicle();
			if(vehicle != null) vehicle.remove();
		}
	}
	
}
