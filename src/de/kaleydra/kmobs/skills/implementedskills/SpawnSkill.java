package de.kaleydra.kmobs.skills.implementedskills;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;

import de.kaleydra.kmobs.KMobs;
import de.kaleydra.kmobs.mobs.KMob;
import de.kaleydra.kmobs.skills.InvalidSkillException;
import de.kaleydra.kmobs.skills.TriggeredSkill;

public class SpawnSkill extends TriggeredSkill {
	String mobId;
	KMob mob;
	int amount;
	int radius; // how much spread?
	
	public SpawnSkill(ConfigurationSection section) throws InvalidSkillException {
		super("spawn",section);
		mobId = section.getString("mobId");
		if(mobId == null) throw new InvalidSkillException("missing mobId!");
		amount = section.getInt("amount",1);
		radius = section.getInt("radius",5);
		radius %= 15; // Limit
	}

	@Override
	public void activate(LivingEntity entity, LivingEntity target) {
		
		this.mob 	=  KMobs.getInstance().getMobManager().getMob(mobId);
		if(this.mob == null)
			try {
				throw new InvalidSkillException("unknown mob id '"+mobId+"'!");
			} catch (InvalidSkillException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		
		if(entity == null) return;
		for(int i=0; i < amount; i++){
			mob.spawnMob(getRandomSpawnPoint(entity.getLocation()));
		}
	}
	
	public Location getRandomSpawnPoint(Location baseLocation){
		double angle;
		int x,y;
		Location backup = baseLocation.clone();
		for(int i=0; i<10;i++){
			angle = Math.random() * Math.PI * 2;
			x = (int) (Math.cos(angle) * radius*Math.random());
			y = (int) (Math.cos(angle) * radius*Math.random());
			
			baseLocation.add(x, 0, y);
			if(baseLocation.getBlock().getType().equals(Material.AIR) && baseLocation.add(0, 1, 0).getBlock().getType().equals(Material.AIR))
				return baseLocation;
			baseLocation = backup;
		}
		return backup;		
	}
	
	@Override
	public ConfigurationSection serializeToSection(ConfigurationSection section) {
		section.set("skill", "spawn");
		section = super.serializeToSection(section);
		section.set("mobId", mob.getId());
		section.set("amount", amount);
		section.set("radius", radius);
		return section;
	}
}
