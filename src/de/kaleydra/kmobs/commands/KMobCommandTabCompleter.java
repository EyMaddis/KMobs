package de.kaleydra.kmobs.commands;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import de.kaleydra.kmobs.KMobs;

public class KMobCommandTabCompleter implements TabCompleter {
	
	private KMobs plugin;

	List<String> subcommands = Arrays.asList("equipment","drop");
	List<String> equips = Arrays.asList("helmet", "chestplate", "leggings", "boots", "itemInHand");
	
	public KMobCommandTabCompleter(KMobs plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		
		if(args.length == 1){
			return findBasedOnArgs(args, plugin.getMobManager().getMobIds());
		}else if(args.length == 2) {
			return findBasedOnArgs(args, subcommands);
		}else if(args.length == 3 && args[1].equalsIgnoreCase("equipment")){
			return findBasedOnArgs(args, equips);
		}
		return null;
	}
	
	private List<String> findBasedOnArgs(String[] args, List<String> possibilities){
		LinkedList<String> output = new LinkedList<String>();
		String lastArg = args[args.length-1];
		if(lastArg.isEmpty()) return possibilities;
		for(String possibility: possibilities){
			if(StringUtil.startsWithIgnoreCase(possibility, lastArg)){
				output.add(possibility);
			}
		}
		return output;		
	}

}
