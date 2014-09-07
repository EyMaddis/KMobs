package de.kaleydra.kmobs.skills;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import de.kaleydra.kmobs.KMobs;
import de.kaleydra.kmobs.metadatavalues.SkillCooldowns;
import de.kaleydra.kmobs.mobs.KMob;

public class ActiveSkill extends BukkitRunnable implements Comparable<ActiveSkill> {
	
	// TODO: Add delay only at first time!
	
	KMob mob;
	SkillData skillData;
	LivingEntity mobEntity;
	LivingEntity target;
	long triggerTime = System.currentTimeMillis();
	boolean ignoreDeath = false; // will overwrite skill data!

	boolean firstRun = true;
//	long cooldownEnd = 0;
	boolean isDone = false;
	
	public ActiveSkill(KMob mob, SkillData skillData, LivingEntity mobEntity, LivingEntity target, boolean isFirstRun) {
		this.mob = mob;
		this.skillData = skillData;
		this.mobEntity = mobEntity;
		this.target = target;
		this.firstRun = true;

//		if(mobEntity.hasMetadata(ActiveSkillQueue.identifier)){
//			ActiveSkillQueue metaData = (ActiveSkillQueue) mobEntity.getMetadata(ActiveSkillQueue.identifier).get(0);
//			ActiveSkill skill = metaData.get(skillData.getKey());
//			if(skill != null){
//				if(!skill.checkCooldown())
//					throw new SkillAlreadyCoolingDownException();
//			}
//		}
	}
	

	@Override
	public void run() {
		if (!canRun())
			return;

		if (ignoreDeath || skillData.ignoreDeath() || mobEntity.isValid()) {
			if(ignoreDeath) mob.logDebugMessage("Death ignored by ActiveSkill");
			if(skillData.ignoreDeath) mob.logDebugMessage("Death ignored by SkillData");
			
			mob.logDebugMessage("Activating skill "+skillData.getKey());
			skillData.getSkill().activate(mobEntity, target);
		}
		SkillCooldowns cooldowns = new SkillCooldowns();
		if(mobEntity.hasMetadata(SkillCooldowns.identifier)) {
			cooldowns = (SkillCooldowns) mobEntity.getMetadata(SkillCooldowns.identifier).get(0);
		} else {
			mobEntity.setMetadata(SkillCooldowns.identifier, cooldowns);
		}
		if (skillData.getCooldown() > 0)
			cooldowns.setCooldown(skillData, System.currentTimeMillis() + skillData.getCooldown() * 50);
		if (skillData.oneTimeOnly())
			cooldowns.setCooldown(skillData, -1); // deactivate cooldown for further activation with the mob

		try {
			if(skillData.getDelay() > 0) { // this runs as a task, not via direct call!
				mob.logDebugMessage("canceled delay task!");
				this.cancel();
			}
		} catch (IllegalStateException e) {
			 e.printStackTrace();
		}
		firstRun = false;
		isDone = true;
		
//		if (!mobEntity.isValid()) {
//			try {
//				if(skillData.getDelay() > 0) this.cancel();
//			} catch (IllegalStateException e) {
//				 e.printStackTrace();
//			}
//		}
	}

	public boolean canRun() {
		if(!checkCooldown()) return false;
		
		if (Math.random() > skillData.getSkill().getChance()) {
			mob.logDebugMessage("skill "+skillData.getKey()+" can't run (random chance)");
			return false;
		}
		if (mobEntity == null) {
			mob.logDebugMessage("skill "+skillData.getKey()+" can't run because the mob does not exist!");
			return false;
		}
		double healthPercent = mobEntity.getHealth() / mobEntity.getMaxHealth();
		if (skillData.getSkill().getMinHealth() > healthPercent || skillData.getSkill().getMaxHealth() < healthPercent) {
			mob.logDebugMessage("skill "+skillData.getKey()+" can't run, because min- and maxHealth are not fullfilled!)");
			if(skillData.getSkill().getMinHealth() > healthPercent){
				mob.logDebugMessage(skillData.getSkill().getMinHealth() +">"+ healthPercent);
			}else mob.logDebugMessage(skillData.getSkill().getMaxHealth() +"<"+ healthPercent);
			
			return false;
		}
		return true;

	}
	
	public boolean checkCooldown(){
		if(!mobEntity.hasMetadata(SkillCooldowns.identifier)) return true;
		SkillCooldowns cooldowns = (SkillCooldowns) mobEntity.getMetadata(SkillCooldowns.identifier).get(0);
		
		if(!cooldowns.containsCooldown(skillData)) return true;
		
		long cooldownEnd = cooldowns.getCooldown(skillData);
		if(cooldownEnd < 0) {
			mob.logDebugMessage("skill "+skillData.getKey()+" can't run again (deactivated)!");
			return false; // -1 means that the skill is deactivated for further
							// activation with the mob
		}
		
		if (cooldownEnd > System.currentTimeMillis()) {
			mob.logDebugMessage("skill "+skillData.getKey()+" can't run yet (cooldown not done)");
			return false;
		}
		return true;
	}
	
	/**
	 * activate the skill with a delay or run immediately if there is no delay
	 */
	public void activate() {
		if (!firstRun || skillData.getDelay() < 1) {
			mob.logDebugMessage("skill "+skillData.getKey()+" running without delay");
			run();
		} else {
			try {
				this.runTaskLater(KMobs.getInstance(), skillData.getDelay());
				mob.logDebugMessage("skill "+skillData.getKey()+" will run with a delay of "+skillData.getDelay()+" ticks");
			} catch(IllegalStateException e){
				e.printStackTrace();
			}
		}
	}

	public void setIgnoreDeath(boolean ignoreDeath){
		this.ignoreDeath = ignoreDeath;
	}
	public boolean ignoreDeath(){
		return ignoreDeath;
	}
	
	public long getFirstActivation() {
		return triggerTime;
	}

	public long getNextActivationTime() {
		return getFirstActivation() + skillData.getInterval() * 50;
	}
	
	public boolean isReady() {
		long currentTime = System.currentTimeMillis();
		if (currentTime >= getNextActivationTime()) {
			return true;
		}
		return false;
	}
	
	/**
	 * @return the e1
	 */
	public Entity getMobEntity() {
		return mobEntity;
	}

	/**
	 * @return the e2
	 */
	public Entity getTarget() {
		return target;
	}

	/**
	 * @return the mob
	 */
	public KMob getMob() {
		return mob;
	}


	public void setMobEntity(LivingEntity mob) {
		this.mobEntity = mob;
	}

	public void setTarget(LivingEntity target) {
		this.target = target;
	}
	
	public boolean isDeactivated() {
		if(!mobEntity.hasMetadata(SkillCooldowns.identifier)) return false;
		SkillCooldowns cooldowns = (SkillCooldowns) mobEntity.getMetadata(SkillCooldowns.identifier).get(0);
		
		if(!cooldowns.containsCooldown(skillData)) return false;
		
		long cooldownEnd = cooldowns.getCooldown(skillData);
		
		return cooldownEnd < 0;
	}
	
	@Override
	public int compareTo(ActiveSkill activeSkill) {
		if (activeSkill.getNextActivationTime() > getNextActivationTime())
			return 1;
		if (activeSkill.getNextActivationTime() == getNextActivationTime())
			return 0;
		return -1;
	}

	public SkillData getSkillData() {
		return skillData;
	}



}
