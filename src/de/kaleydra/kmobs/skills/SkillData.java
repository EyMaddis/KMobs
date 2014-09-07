package de.kaleydra.kmobs.skills;

/**
 * holds the context for every skill, like delay or interval information *
 */
public class SkillData{

	TriggeredSkill skill;
	
	/**
	 * the identifier the mob author assigned as key for the skill inside the .mob
	 */
	String key;
	/** delay in ticks */
	long delay;

	boolean ignoreDeath = false;
	int interval = 0; // seconds
	int radius = 35;
	long radiusSquared = radius*radius;
	boolean isIntervalSkill = false;
	int cooldown = 0;

	public SkillData(String key, TriggeredSkill skill, int cooldown, long delay) {
		this.key = key;
		this.skill = skill;
		this.delay = delay;
		this.cooldown = cooldown;
	}

	public SkillData(String key, TriggeredSkill skill, int cooldown, long delay, int interval) {
		this(key,skill, cooldown, delay);
		this.interval = interval;
		if(interval>0) isIntervalSkill = true;
	}

	public SkillData(String key, TriggeredSkill skill, int cooldown, long delay, int interval, int radius) {
		this(key,skill,cooldown,delay,interval);
		this.radius = radius;
		this.radiusSquared = radius*radius;
	}

	/**
	 * @return the key that identifies each skill
	 */
	public String getKey() {
		return key;
	}

	public boolean isIntervalSkill() {
		return isIntervalSkill;
	}
	
	public boolean hasCooldown(){
		return cooldown != 0;
	}

	/**
	 * @return the skill
	 */
	public TriggeredSkill getSkill() {
		return skill;
	}

	/**
	 * @return the delay
	 */
	public long getDelay() {
		return delay;
	}

	public Integer getInterval() {
		return interval;
	}


	public void ignoreDeath(boolean ignoreDeath) {
		this.ignoreDeath = ignoreDeath;
	}

	public boolean ignoreDeath() {
		return ignoreDeath;
	}
	
	public boolean oneTimeOnly(){
		return getCooldown() < 0;
	}

	public int getCooldown() {
		return cooldown;
	}

	public int getRadius() {
		return radius;
	}
	
	/**
	 * used for better performance
	 * @return radius^2
	 */
	public long getRadiusSquared() {
		return radius;
	}

}
