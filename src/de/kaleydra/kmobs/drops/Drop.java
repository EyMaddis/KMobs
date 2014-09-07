package de.kaleydra.kmobs.drops;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;

@SerializableAs("Drop")
public class Drop implements ConfigurationSerializable {

	ItemStack item;
	double dropChance;
	
	public Drop(ItemStack item, double dropChance) {
		this.item = item;
		setDropChance(dropChance);
	}
	
	public Drop(Map<String,Object> map){
		item = (ItemStack) map.get("item");
		this.setDropChance((Double) map.get("dropChance"));
	}
	
	/**
	 * calculates if it should drop, based on the set chance (0.0 -> 1.0)
	 * @return
	 */
	public boolean shouldDrop(){
		if(Math.random() <= dropChance) return true;
		return false;
	}

	/**
	 * @return the item
	 */
	public ItemStack getItem() {
		return item;
	}

	/**
	 * chance that the item can get dropped 1.0 = 100%
	 * @return the dropChance in 0..1
	 */
	public double getDropChance() {
		return dropChance;
	}

	/**
	 * @param item the item to set
	 */
	public void setItem(ItemStack item) {
		this.item = item;
	}

	/**
	 * chance that the item can get dropped 1.0 = 100%
	 * @param dropChance the dropChance to set between 0.1 and 1.0
	 */
	public void setDropChance(double dropChance) {
		if(dropChance >= 0.0 && dropChance <= 1.0)
			this.dropChance = dropChance;
		else if(dropChance > 1.0) this.dropChance = 1.0;
		else this.dropChance = 0.0;
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> output = new LinkedHashMap<String,Object>();
		output.put("dropChance", dropChance);
		output.put("item", item);
		return output;
	}
	
	@Override 
	public String toString(){
		String output = ""+item.getType();
		if(item.hasItemMeta() && item.hasItemMeta()){
			output += " ("+item.getItemMeta().getDisplayName()+")";
		}
		output += ChatColor.GREEN+" DropChance: "+dropChance;
		return output;
	}
	
}
