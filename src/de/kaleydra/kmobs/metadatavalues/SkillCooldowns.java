package de.kaleydra.kmobs.metadatavalues;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import de.kaleydra.kmobs.KMobs;
import de.kaleydra.kmobs.skills.SkillData;

public class SkillCooldowns implements MetadataValue {
	public static final String identifier = "kmobs_skillCooldowns";
	

	Map<String,Long> cooldowns = new HashMap<String, Long>();

	
	public long getCooldown(SkillData skill){
		return cooldowns.get(skill.getKey());
	}
	public void setCooldown(SkillData skill, long newCooldown){
		cooldowns.put(skill.getKey(), newCooldown);
	}
	public boolean containsCooldown(SkillData skill){
		return cooldowns.containsKey(skill.getKey());
	}
	
	@Override
	public boolean asBoolean() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public byte asByte() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double asDouble() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float asFloat() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int asInt() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long asLong() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public short asShort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String asString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Plugin getOwningPlugin() {
		// TODO Auto-generated method stub
		return KMobs.getInstance();
	}

	@Override
	public void invalidate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object value() {
		// TODO Auto-generated method stub
		return cooldowns;
	}

}
