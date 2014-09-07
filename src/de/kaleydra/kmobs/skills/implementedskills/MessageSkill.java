package de.kaleydra.kmobs.skills.implementedskills;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import de.kaleydra.kmobs.skills.InvalidSkillException;
import de.kaleydra.kmobs.skills.TriggeredSkill;

public class MessageSkill extends TriggeredSkill {

	List<String> message;
	Type type;
	int radius;
	
	public MessageSkill(ConfigurationSection section) throws InvalidSkillException {
		super("message",section);
		section.getString("message");
		String typeString = section.getString("type");
		if(typeString == null) throw new InvalidSkillException("Missing type!");
		if(typeString.equalsIgnoreCase("directmessage") || typeString.equalsIgnoreCase("direct")){
			type = Type.DirectMessage;
		}else if(typeString.equalsIgnoreCase("areaMessage") || typeString.equalsIgnoreCase("area")){
			type = Type.AreaMessage;
		} else throw new InvalidSkillException("Invalid type '"+typeString+"'");
		message = section.getStringList("message");
		if(message == null) throw new InvalidSkillException("Missing message!");
		radius = section.getInt("radius", 35);
	}

	@Override
	public void activate(LivingEntity entity, LivingEntity target) {
		if(type.equals(Type.DirectMessage)){
			if(target instanceof Player) {
				Player p = (Player) target;
				for (String line:message){
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
				}
			}
		} else {
			Player p;
			for(Entity e:entity.getNearbyEntities(radius, radius, radius)){
				if(e instanceof Player){
					p = (Player)e;
					for (String line:message){
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
					}
				}
			}
		}
	}
	
	public enum Type{
		DirectMessage, AreaMessage
	}
}
