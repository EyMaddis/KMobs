package de.kaleydra.kmobs.events;

import org.bukkit.entity.LivingEntity;

import de.kaleydra.kmobs.mobs.KMob;


public class KMobSpawnEvent extends KMobEvent {

	public KMobSpawnEvent(KMob mob, LivingEntity mobEntity) {
		super(mob, mobEntity);
	}

}
