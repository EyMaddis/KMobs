package de.kaleydra.kmobs.skills.implementedskills;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.kaleydra.licetia.effects.ParticleEffect;

import de.kaleydra.kmobs.KMobs;
import de.kaleydra.kmobs.skills.InvalidSkillException;
import de.kaleydra.kmobs.skills.TriggeredSkill;

public class ParticleSkill extends TriggeredSkill {
	
	String type;

	ParticleEffect particleEffect;
	double height;
	float offsetX;
	float offsetY;
	float offsetZ;
	float speed;
	int amount;
	
	boolean isTileCrack = false;
	boolean isIconCrack = false;
	
	int id;
	byte data;
	
	public ParticleSkill(ConfigurationSection section) throws InvalidSkillException {
		super("particle",section);

		type = section.getString("type", "area");
		height = section.getDouble("height", 0);

		offsetX = (float)section.getDouble("offsetX", 1.0); 
		offsetY = (float)section.getDouble("offsetY", 1.0); 
		offsetZ = (float)section.getDouble("offsetZ", 1.0); 
		speed  = (float)section.getDouble("speed", 1.0);
		amount = section.getInt("amount", 10); ;
		if(amount < 1){
			throw new InvalidSkillException(super.getIdentifier()+" can't have an amount below 1!");
		}
		if (!type.equalsIgnoreCase("area") && !type.equalsIgnoreCase("direct")){
			throw new InvalidSkillException("invalid effect type '"+type+"'!");
		}
		String particleType	= section.getString("particleType");
		
		
		
		if(particleType == null){
			throw new InvalidSkillException(super.getIdentifier()+": Missing particleType!");
		}
		
		// tile and icon break are not in the enum!
		if(particleType.equalsIgnoreCase("ICON_CRACK")){
			isIconCrack = true;
			id = getId(section);
		}else if (particleType.equalsIgnoreCase("TILE_CRACK")){
			isTileCrack = true;
			id = getId(section);
			data = (byte) (section.getInt("data", 0) % 16);
		} else {
			particleEffect = ParticleEffect.valueOf(particleType.toUpperCase());
			if(particleEffect == null){
					throw new InvalidSkillException("Particle type \""+particleType+"\" does not exist!");
			}
		}				
	}

	private int getId(ConfigurationSection section) throws InvalidSkillException{
		int id = section.getInt("id", -1);
		if(id < 1){
			throw new InvalidSkillException(super.getIdentifier()+": ID must be above 1 or is missing completely!");
		}
		return id;
	}
	

	@Override
	public void activate(LivingEntity entity, LivingEntity target) {
		Location loc = entity.getLocation();
		if(height != 0) loc = loc.add(0, height, 0);
		if(type.equalsIgnoreCase("direct")){
			if(target == null){
				KMobs.getInstance().getLogger().severe(super.getIdentifier()+" uses direct type, but there is no target");
				return;
			} else if (!(target instanceof Player)){
				return;
			}
			Player player =(Player)entity;
			if(isIconCrack){
				ParticleEffect.displayIconCrack(loc, id, offsetX, offsetY, offsetZ, speed, amount, player);
			} else if (isTileCrack){
				ParticleEffect.displayBlockDust(loc, id, data, offsetX, offsetY, offsetZ, speed, amount, player);
			}else {
				particleEffect.display(loc, offsetX, offsetY, offsetZ, speed, amount, player);
			}
		} else { //area
			if(isIconCrack){
				ParticleEffect.displayIconCrack(loc, id, offsetX, offsetY, offsetZ, speed, amount);
			} else if (isTileCrack){
				ParticleEffect.displayBlockDust(loc, id, data, offsetX, offsetY, offsetZ, speed, amount);
			} else {
				particleEffect.display(loc, offsetX, offsetY, offsetZ, speed, amount);
			}
		}
	}
}
