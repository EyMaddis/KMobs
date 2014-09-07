package de.kaleydra.kmobs.skills.implementedskills;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Lists;
import com.kaleydra.licetia.LiCETIA;
import com.kaleydra.licetia.effects.FireworkEffectPlayer;

import de.kaleydra.kmobs.KMobs;
import de.kaleydra.kmobs.skills.InvalidSkillException;
import de.kaleydra.kmobs.skills.TriggeredSkill;

public class BlizzardSkill extends TriggeredSkill {
	double radius; // how much spread?
	int steps;
	long duration;
	double height;
	FireworkEffect fireworkEffect;
	FireworkEffectPlayer effect;
	
	public BlizzardSkill(ConfigurationSection section) throws InvalidSkillException {
		super("blizzard", section);
		radius = section.getDouble("radius", 5);
		steps = section.getInt("steps", 20);
		duration = section.getLong("duration", 100);
		height = section.getDouble("height", 5.0);
		if(steps < 1) steps = 20;
		if(duration < 1) duration = 100;
		
		FireworkEffect defaultFw = FireworkEffect.builder().withColor(Color.BLUE, Color.AQUA).withFlicker().build();
		fireworkEffect = (FireworkEffect) section.get("fireworkEffect", defaultFw);
		effect = new FireworkEffectPlayer(fireworkEffect);
	}

	@Override
	public void activate(LivingEntity entity, LivingEntity target) {
		List<Location> points = LiCETIA.getCircleCircumferenceLocations(entity.getLocation().add(0, height, 0), radius, steps);
		
		int half = points.size() / 2;

		List<ArrayList<Location>> locationPaths = new ArrayList<ArrayList<Location>>();
		locationPaths.add(new ArrayList<Location>(points.subList(0, half)));
		locationPaths.add(new ArrayList<Location>(Lists.reverse(points.subList(half, points.size()))));
		
		int effectsPerTick = (int) Math.floor(duration/steps);
		if(effectsPerTick < 1) effectsPerTick = 1;
		KMobs.logDebugMessage("BlizzardSkill: duration/steps is below 1");
		EffectRunnable runnable = new EffectRunnable(entity.getLocation(), locationPaths, radius, effectsPerTick);
		KMobs.logDebugMessage("BlizzardSkill: calculated effects per tick is "+effectsPerTick);
		KMobs.logDebugMessage("Runnable " + runnable.runTaskTimer(KMobs.getInstance(), 0L, 1).getTaskId());

	}

	@Override
	public ConfigurationSection serializeToSection(ConfigurationSection section) {
		section = super.serializeToSection(section);
		section.set("radius", radius);
		section.set("steps", steps);
		section.set("duration", duration);
		section.set("height", height);
		section.set("fireworkEffect", fireworkEffect);
		return section;
	}

	private class EffectRunnable extends BukkitRunnable {
		Location center;
		List<ArrayList<Location>> points;
		int counter = 0;

		double radius;
		int effectsPerTick;

		boolean finished = false;

		EffectRunnable(Location center, List<ArrayList<Location>> points, double radius, int effectsPerTick) {
			this.center = center;
			this.points = points;
			this.radius = radius;
			this.effectsPerTick = effectsPerTick;
		}

		@Override
		public void run() {
			if (finished) {
				World world = center.getWorld();
				for (Entity entity : LiCETIA.getNearbyEntities(center, radius)) {
					if (!(entity instanceof LivingEntity) || entity instanceof Monster)
						continue;
					world.strikeLightning(entity.getLocation());
				}
				this.cancel();
				return;
			}
			for (List<Location> locations : points) {
				final int size = locations.size();
				if (counter >= size) {
					finished = true;
					return; // play effect with one tick delay
				}
				for(int i=0; i < effectsPerTick && i < size; i++){
					effect.play(locations.get(counter));
				}
			}
			if(effectsPerTick > 0) {
				counter+=effectsPerTick;
			} else {
				counter++;
			}
		}
	}

}
