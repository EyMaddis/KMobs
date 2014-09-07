package de.kaleydra.kmobs.mobs;

import org.bukkit.ChatColor;

public class MobRarity {
	ChatColor color;
	String label;
	
	public MobRarity(ChatColor color, String label) {
		this.color = color;
		this.label = label;
	}
	
	@Override
	public String toString(){
		return (color == null)? null : color.toString();
	}
	
	public String getName(){
		return label;
	}
	
	public static final MobRarity COMMON 	= new MobRarity(ChatColor.WHITE 		, "common");
	public static final MobRarity RARE 		= new MobRarity(ChatColor.YELLOW		, "rare");
	public static final MobRarity LEGENDARY = new MobRarity(ChatColor.GOLD			, "legendary");
	public static final MobRarity MAGICAL	= new MobRarity(ChatColor.BLUE			, "magical");
	public static final MobRarity EPIC		= new MobRarity(ChatColor.DARK_PURPLE	, "epic");
	public static final MobRarity SECRET	= new MobRarity(ChatColor.MAGIC, "magic");
	
	
	/**
	 * get the MobRarity instance from the corresponding label
	 * @param label
	 * @return null if not found
	 */
	public static MobRarity getRarityFromName(String label){
		if(label == null) return null;
		if(label.equalsIgnoreCase("COMMON")) 	return COMMON;
		if(label.equalsIgnoreCase("RARE")) 		return RARE;
		if(label.equalsIgnoreCase("LEGENDARY")) return LEGENDARY;
		if(label.equalsIgnoreCase("MAGICAL")) 	return MAGICAL;
		if(label.equalsIgnoreCase("EPIC")) 		return EPIC;
		if(label.equalsIgnoreCase("SECRET")) 	return SECRET;
		
		return null;
	}
}
