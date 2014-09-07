package de.kaleydra.kmobs.radiuschecker;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import de.kaleydra.kmobs.KMobs;
import de.kaleydra.kmobs.metadatavalues.ActiveSkillQueue;
import de.kaleydra.kmobs.mobs.KMob;
import de.kaleydra.kmobs.mobs.MobManager;
import de.kaleydra.kmobs.skills.ActiveSkill;
import de.kaleydra.kmobs.skills.SkillData;
import de.kaleydra.kmobs.skills.SkillTrigger;

public class RadiusCheckTask extends BukkitRunnable {
	
	boolean stop = false;
	
	Player player;
	PlayerTaskManager taskManager;
	
	MobManager mobManager;
	
	int radius = 35;
	
	public RadiusCheckTask(Player player, PlayerTaskManager taskManager){
		this.player = player;
		this.taskManager = taskManager;
		mobManager = KMobs.getInstance().getMobManager();
	}
	
	@Override
	public void run() {
		if(stop || !taskManager.containsPlayer(player)) {
			taskManager.cancelRadiusChecker(player);
			return;
		}
		
		LivingEntity le;
		KMob mob;
		
		for(Entity entity:player.getNearbyEntities(radius, radius, radius)){
			
			if(!(entity instanceof LivingEntity)) continue;
			le = (LivingEntity) entity;

			
			mob = mobManager.getMobByEntity(le);
			if(mob == null) continue;
			
			
			if(mob.getSkills(SkillTrigger.onInterval).isEmpty()) continue; // mob does not have any interval skills
			
			ActiveSkillQueue metaDataValue;
			if(le.hasMetadata(ActiveSkillQueue.identifier)){
				 metaDataValue = (ActiveSkillQueue) le.getMetadata(ActiveSkillQueue.identifier).get(0);
			}else { // start interval for each onIntervall skill
				metaDataValue = new ActiveSkillQueue();
				
				// create active skills for each skill	
				for(SkillData skillData:mob.getSkills(SkillTrigger.onInterval)){
					metaDataValue.add(new ActiveSkill(mob, skillData, le, player, true));
				}
				le.setMetadata(ActiveSkillQueue.identifier, metaDataValue); // save within mob
			}
			
			checkQueue(metaDataValue,le);
			
			
		}

	}
	
	private void checkQueue(ActiveSkillQueue metaDataValue, LivingEntity entity){
		List<ActiveSkill> reusable = new ArrayList<ActiveSkill>(); 						
		
		ActiveSkill activeSkill = metaDataValue.peek();
		
		while(activeSkill != null && activeSkill.isReady()){
			if(activeSkill.isDeactivated()){
				metaDataValue.remove(); // no need to run that skill again, because cooldown -1
				activeSkill = metaDataValue.peek();
				continue;
			}
			activeSkill = metaDataValue.poll();
			ActiveSkill reusableActiveSkill = activeSkill;
			if(activeSkill.canRun() 
					&& player.getLocation().distanceSquared(entity.getLocation()) <= activeSkill.getSkillData().getRadiusSquared()){
				activeSkill.activate();
				
				// re-add the skill with a fresh start
				reusableActiveSkill = new ActiveSkill(activeSkill.getMob(),activeSkill.getSkillData(),entity,player, false);
				
			} 
			
			// skip unnessecary reusing
			if(!activeSkill.getSkillData().oneTimeOnly())
				reusable.add(reusableActiveSkill);
			activeSkill = metaDataValue.peek();
		}
		metaDataValue.addAll(reusable);
	}
}
