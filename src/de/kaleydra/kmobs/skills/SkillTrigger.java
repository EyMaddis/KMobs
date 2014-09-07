package de.kaleydra.kmobs.skills;

/**
 * hooks when skills can get triggered 
 */
public enum SkillTrigger {
	/** when a mob spawns */
	onSpawn,
	
	/** when a mob gets damaged by somebody */
	onDamage,
	
	/** when a mob attacks an entity */
	onAttack,
	
	/** when a mob gets damaged by a projectile */
	onProjectileDamage,
	
	/** when a mob launches a projectile (target entity = null!) */
	onProjectileLaunch,
	
	/** when a mob looks at an entity.
	 * <b>That is very often</b> (every tick the mob looks at something)!
	 * Use cooldowns.
	 */
	onTarget,
	
	/** when a mob dies (target can be the killer) */
	onDeath,
	
	/** when a interval timer is activated which it does every x ticks!
	 * To use it, there need to be another skill parameter "interval" with the
	 * time (in ticks) how often the skill should be applied. For better performance
	 * it is only possible in steps of 20 (1 second).<br /> 
	 * Another optional parameter is the "intervalRadius", it says how close
	 * a player must be to activate the timer, maximum value is 35 (which is default)!
	 * <h2>Example:</h2>
	 * <pre>
	 * interval: 200 // will be activated every 10 seconds
	 * intervalRadius: 15 // but only, if the player is max 15 blocks away
	 * </pre>
	 */
	onInterval, 
	
	/** when a mob dismounts from another entity (e.g. horse) */
	onDismount,
	
	/** when the passenger of a mob dismounts */
	onPassengerDismount
}

