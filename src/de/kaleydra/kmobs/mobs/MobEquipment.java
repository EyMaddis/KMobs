package de.kaleydra.kmobs.mobs;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

@SerializableAs("MobEquipment")
public class MobEquipment implements ConfigurationSerializable {
	
	ItemStack helmet = null;
	
	ItemStack chestplate = null;
	
	ItemStack leggings = null;
	
	ItemStack boots = null;
	
	ItemStack itemInHand = null;
	
	public MobEquipment(){
		
	}
	
	public MobEquipment(EntityEquipment equip){
		helmet = equip.getHelmet();		
		chestplate = equip.getChestplate();		
		leggings = equip.getLeggings();		
		boots = equip.getBoots();		
		itemInHand = equip.getItemInHand();
	}
	
	public MobEquipment(Map<String, Object> map){
		helmet = (ItemStack) map.get("helmet");		
		chestplate = (ItemStack) map.get("chestplate");		
		leggings = (ItemStack) map.get("leggings");		
		boots = (ItemStack) map.get("boots");
		itemInHand = (ItemStack) map.get("itemInHand");
	}
	
	public LivingEntity setEquipment(LivingEntity e){
		
		EntityEquipment equip = e.getEquipment();

		equip.setHelmet(helmet);
		equip.setChestplate(chestplate);
		equip.setLeggings(leggings);
		equip.setBoots(boots);
		equip.setItemInHand(itemInHand);		
		return e;
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new LinkedHashMap<String, Object>();

		map.put("helmet", helmet);		
		map.put("chestplate", chestplate);		
		map.put("leggings", leggings);
		map.put("boots", boots);
		map.put("itemInHand", itemInHand);
		return map;
	}

	/**
	 * @return the helmet
	 */
	public ItemStack getHelmet() {
		return helmet;
	}

	/**
	 * @param helmet the helmet to set
	 */
	public void setHelmet(ItemStack helmet) {
		this.helmet = helmet;
	}
	
	/**
	 * @return the chestplate
	 */
	public ItemStack getChestplate() {
		return chestplate;
	}

	/**
	 * @param chestplate the chestplate to set
	 */
	public void setChestplate(ItemStack chestplate) {
		this.chestplate = chestplate;
	}
	
    /**
	 * @return the leggings
	 */
	public ItemStack getLeggings() {
		return leggings;
	}

	/**
	 * @param leggings the leggings to set
	 */
	public void setLeggings(ItemStack leggings) {
		this.leggings = leggings;
	}

	/**
	 * @return the boots
	 */
	public ItemStack getBoots() {
		return boots;
	}

	/**
	 * @param boots the boots to set
	 */
	public void setBoots(ItemStack boots) {
		this.boots = boots;
	}

	/**
	 * @return the itemInHand
	 */
	public ItemStack getItemInHand() {
		return itemInHand;
	}

	/**
	 * @param itemInHand the itemInHand to set
	 */
	public void setItemInHand(ItemStack itemInHand) {
		this.itemInHand = itemInHand;
	}

}
