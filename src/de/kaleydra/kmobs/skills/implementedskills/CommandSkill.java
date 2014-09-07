package de.kaleydra.kmobs.skills.implementedskills;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import de.kaleydra.kmobs.skills.InvalidSkillException;
import de.kaleydra.kmobs.skills.TriggeredSkill;

public class CommandSkill extends TriggeredSkill {
	
	String command;
	Type type;
	int radius;

	public CommandSkill(ConfigurationSection section) throws InvalidSkillException {
		super("command",section);
		command = section.getString("command");
		if(command == null) throw new InvalidSkillException("Missing command!");
		String typeString = section.getString("type");
		if(typeString == null) throw new InvalidSkillException("Missing type!");
		
		if(typeString.equalsIgnoreCase("areaplayers")) {
			type = Type.AreaPlayers;
		}else if(typeString.equalsIgnoreCase("ConsoleCommand")){
			type = Type.ConsoleCommand;
		}else if(typeString.equalsIgnoreCase("consoleAreaPlayers")){
			type = Type.ConsoleAreaPlayers;
		}else throw new InvalidSkillException("invalid command type '"+typeString+"'");
		
		radius = section.getInt("radius", 5);
		if((type.equals(Type.AreaPlayers) || type.equals(Type.ConsoleAreaPlayers)) && radius < 1)
			throw new InvalidSkillException("Radius is needed for areaPlayers and consoleAreaPlayers, must be >1!");
	}

	@Override
	public void activate(LivingEntity entity, LivingEntity target) {
		if(type.equals(Type.ConsoleCommand)) {
			if(entity instanceof Player) command = command.replaceAll("@p", ((Player)entity).getName());
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);	
		}else {
			Player p;
			CommandSender sender;
			String commandBackup = command;
			for(Entity e:entity.getNearbyEntities(radius, radius, radius)){
				if(e instanceof Player) {
					p = (Player)e;
					command = command.replaceAll("@p", p.getName());
					if(type.equals(Type.ConsoleAreaPlayers)) {
						sender = Bukkit.getConsoleSender();
					}else sender = (CommandSender) p;
					Bukkit.getServer().dispatchCommand(sender, command);
					command = commandBackup;
				}
			}
		}
	}
	
	@Override
	public ConfigurationSection serializeToSection(ConfigurationSection section){
		section.set("skill", "command");
		section = super.serializeToSection(section);
		section.set("type", type.toString());
		section.set("radius", radius);
		return section;
	}
	
	public enum Type {
		AreaPlayers, ConsoleCommand,
		ConsoleAreaPlayers
	}

}
