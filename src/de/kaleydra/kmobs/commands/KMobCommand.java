package de.kaleydra.kmobs.commands;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.kaleydra.kmobs.KMobs;
import de.kaleydra.kmobs.drops.Drop;
import de.kaleydra.kmobs.mobs.KMob;
import de.kaleydra.kmobs.mobs.MobEquipment;


public class KMobCommand implements CommandExecutor {
	public static Set<String> activatedMobInfo = new HashSet<String>();
	
	private KMobs plugin;

	public KMobCommand(KMobs plugin){
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label,
			String[] args) {
		if(!sender.isOp()){
			sender.sendMessage(ChatColor.DARK_RED+"No permission");
			return true;
		}
		if(!(sender instanceof Player)) return false;
		Player p = (Player)sender;
		
		if(args.length > 0) {
			if(args[0].equalsIgnoreCase("list")){
				sender.sendMessage("===========( KMobs )===========");
				for(KMob mob:plugin.getMobManager().getAllMobs()){
					sender.sendMessage(mob.getId()+" ("+mob.getName()+ChatColor.RESET.toString()+")");
				}
				sender.sendMessage("===============================");
				return true;
			} else if(args[0].equalsIgnoreCase("reload")){
				plugin.reloadConfig();
				plugin.getMobManager().load(plugin.getDataFolder()+"/mobs/");
				sender.sendMessage(ChatColor.DARK_GREEN+"Mobs & Config reloaded!");
				return true;
			}

			KMob mob = plugin.getMobManager().getMob(args[0]);
			if (mob == null){
				sender.sendMessage("unknown Mob");
				return false;
			}
			
			
			
			if(args.length >= 2){
				if(args[1].equalsIgnoreCase("drop")){
					if(args.length < 3 || args[2].equalsIgnoreCase("list")){
						sender.sendMessage(ChatColor.GREEN+"=== Drops ===");
						for(Drop drop:mob.getDrops()){
							sender.sendMessage(drop.toString());
						}
						sender.sendMessage(ChatColor.GREEN+"=============");
						sender.sendMessage(ChatColor.GREEN+"Item in der Hand hinzufügen, durch "+
										   ChatColor.GOLD+"/kmob <mob> drop 1.0"+ChatColor.GREEN+" (Drop Chance)");
						return true;
					}
					
					if(p.getItemInHand() == null){
						sender.sendMessage(ChatColor.DARK_RED+"Kein Item in der Hand!");
						return false;
					}
					double chance = 1.0;
					try{
						chance = Double.valueOf(args[2]);
					} catch(NumberFormatException e){
						sender.sendMessage(ChatColor.DARK_RED+args[2]+" ist keine Kommazahl! (Dropchance)");
						return true;
					}
					mob.getDrops().add(new Drop(p.getItemInHand(),chance));
					
					if(plugin.getMobManager().saveItems(mob)) {
						p.sendMessage(ChatColor.GREEN+"Equipment aktualisiert und ins Dateisystem gespeichert!");
					}else {
						p.sendMessage(ChatColor.DARK_RED+"Es ist etwas beim Speichern schief gelaufen!");
					}
					return true;					
				}
				else if(args[1].equalsIgnoreCase("equipment")){
					if(args.length >= 4){
						sender.sendMessage(ChatColor.DARK_RED+"Equipment kann nicht gedroppt werden. (keine chance angeben)");
						return false;
					}
					String equipPart = args[2];
					MobEquipment equip = mob.getEquipment();
					
					if(equipPart.equalsIgnoreCase("helmet")){
						equip.setHelmet(p.getItemInHand());
					}else if(equipPart.equalsIgnoreCase("chestplate")){
						equip.setChestplate(p.getItemInHand());
					}else if(equipPart.equalsIgnoreCase("leggings")){
						equip.setLeggings(p.getItemInHand());
					}else if(equipPart.equalsIgnoreCase("boots")){
						equip.setBoots(p.getItemInHand());
					}else if(equipPart.equalsIgnoreCase("iteminhand")){
						equip.setItemInHand(p.getItemInHand());			
					}else {
						p.sendMessage(ChatColor.DARK_RED+"unbekannter Sub-Befehl! Benutze die Tabvervollständigung!");
						return true;
					}
					mob.setEquipment(equip);
					if(plugin.getMobManager().saveItems(mob)) {
						p.sendMessage(ChatColor.GREEN+"Equipment aktualisiert und ins Dateisystem gespeichert!");
					}else {
						p.sendMessage(ChatColor.DARK_RED+"Es ist etwas beim Speichern schiefgelaufen!");
					}
					return true;
				
				}
			}
			
			Location l = p.getTargetBlock(null, 50).getLocation().add(0, 1, 0);
			
			mob.spawnMob(l);
			
			p.sendMessage("Mob with id \""+mob.getId()+"\" ("+mob.getName()+ChatColor.RESET.toString()+") spawned.");
			
			p.sendMessage("Spawn parameter check: "+mob.checkSpawn(l));
			if(mob.getWorldguardRegion() != null){
				p.sendMessage("Mob is limited to region: "+mob.getWorldguardRegion()+" - check: "
						+mob.checkSpawnWithRegions(l));
			} 
			return true;
		}else {	
			if(activatedMobInfo.contains(p.getName())){
				activatedMobInfo.remove(p.getName());
				sender.sendMessage("You will no longer analyze the next mob!");
				return true;
			}
			activatedMobInfo.add(p.getName());
			sender.sendMessage(ChatColor.GREEN+"The next mob you right click will get analyzed!");
			return true;
//				if(plugin.getMobManager().contains("test")) return false;
//				
//				KMob mob = new KMob(plugin,"test", new File(plugin.getDataFolder()+"/mobs/test.mob").getPath());
//				ItemStack helmet = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
//			    SkullMeta skullMeta = (SkullMeta) helmet.getItemMeta();
//				skullMeta.setOwner("Mulcho");
//				helmet.setItemMeta(skullMeta);
//				 
//				ItemStack chest  = new ItemStack(299);
//				chest.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);
//				
//				ItemStack legs   = new ItemStack(300);
//				ItemStack boots	 = new ItemStack(301);
//				ItemStack hand 	 = new ItemStack(Material.DIAMOND_AXE);
//				hand.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
//				hand.addUnsafeEnchantment(Enchantment.ARROW_FIRE, 1);
//				ItemMeta meta = hand.getItemMeta();
//				meta.setDisplayName(ChatColor.GOLD+"Imba Sword!");
//				
//				mob.setType("zombie");
//				mob.setHealth(200);
//				
//				MobEquipment equip = new MobEquipment();
//				equip.setHelmet(helmet);
//				equip.setBoots(boots);
//				equip.setChestplate(chest);
//				equip.setLeggings(legs);
//				equip.setBoots(boots);
//				equip.setItemInHand(hand);
//				
//				mob.setEquipment(equip);
//				
//				mob.setName("Günther");
//				
//				mob.setRarity(MobRarity.RARE);
//				
//				mob.setExpMax(3);
//				mob.setExpMin(1);
//				mob.setBiomes(Arrays.asList("PLAINS"));
//				
//				
//				mob.setBaby(true);
//				mob.setSpawnChance(1.0);
//				
//				
//				if(sender instanceof Player){
//					Player p = (Player) sender;
//					mob.spawnMob(p.getLocation());
//				}
//				
//				plugin.getMobManager().save(mob);
//				sender.sendMessage("Test Mob created!");
//				return true;
			}
	}

}
