package de.kaleydra.kmobs.skills.implementedskills;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;

import de.kaleydra.kmobs.skills.InvalidSkillException;
import de.kaleydra.kmobs.skills.TriggeredSkill;

public class DespawnSkill extends TriggeredSkill {
	
	
	public DespawnSkill(ConfigurationSection section) throws InvalidSkillException {
		super("despawn", section);
	}

	
	@Override
	public void activate(LivingEntity entity, LivingEntity target) {
		entity.remove();
	}
}
