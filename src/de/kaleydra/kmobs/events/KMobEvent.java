package de.kaleydra.kmobs.events;

import javax.annotation.Nonnull;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import de.kaleydra.kmobs.mobs.KMob;

public abstract class KMobEvent extends Event{    
    KMob mob;
    LivingEntity mobEntity;
    
    public KMobEvent(@Nonnull KMob mob, LivingEntity mobEntity){
    	this.mob = mob;
    	this.mobEntity = mobEntity;
    }

	@Override
	public HandlerList getHandlers() {
		return mob.getHandlers(getClass());
	}
	
	
}
