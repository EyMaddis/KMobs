package de.kaleydra.kmobs.metadatavalues;

import java.util.Collection;
import java.util.PriorityQueue;

import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import de.kaleydra.kmobs.KMobs;
import de.kaleydra.kmobs.skills.ActiveSkill;

public class ActiveSkillQueue implements MetadataValue {
	public static final String identifier = "kmobs_activeSkills";
	
	PriorityQueue<ActiveSkill> queue;
	
	/** <Skill.key, cooldownEnd (0 = skill in delay)  */
//	TreeMap<String,ActiveSkill> skills = new TreeMap<String,ActiveSkill>();
		
	public ActiveSkillQueue(){
		queue = new PriorityQueue<ActiveSkill>();
	}
	
//	public ActiveSkill get(String skillKey){
//		return skills.get(skillKey);
//	}
	
	public void add(ActiveSkill skill){
		queue.add(skill);
//		putToSkills(skill);
	}

//	public boolean canActivate(SkillData skill){
//		if(skill == null) return false;
//		if(!cooldowns.containsKey(skill.getKey())) return true;
//		long cooldownEnd = cooldowns.get(skill.getKey());
//		if(cooldownEnd < 1) return false;
//		if(cooldownEnd <= System.currentTimeMillis()) return true;
//		return false;
//	}
	
	public ActiveSkill peek(){
		return queue.peek();
	}
	public ActiveSkill poll(){
		ActiveSkill skill = queue.poll();
//		removeFromSkills(skill);
		return skill;
	}

	public void remove() {
//		ActiveSkill skill = queue.peek();
//		if(skill != null) removeFromSkills(skill);
		queue.remove();
	}
	
	public void addAll(Collection<ActiveSkill> collection){
		if(collection == null || collection.isEmpty()) return;
		queue.addAll(collection);
//		for(ActiveSkill skill : collection){
//			queue.add(skill);
////			putToSkills(skill);
//		}
	}

//	private void putToSkills(ActiveSkill skill) {
//		skills.put(skill.getSkillData().getKey(), skill);
//	}
//	
//	private void removeFromSkills(ActiveSkill skill) {
//		skills.remove(skill.getSkillData());
//	}
	
//	public PriorityQueue<ActiveSkill> getQueue(){
//		return queue;
//	}
	
	
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
		return KMobs.getInstance();
	}

	@Override
	public void invalidate() {
		// TODO Auto-generated method stub

	}

	@Override
	public Object value() {
		return (Object) queue;
	}

}
