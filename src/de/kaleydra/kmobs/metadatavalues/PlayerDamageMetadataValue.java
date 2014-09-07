package de.kaleydra.kmobs.metadatavalues;

import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import de.kaleydra.kmobs.KMobs;

public class PlayerDamageMetadataValue implements MetadataValue {
	public static final String KEY = "kmobs_playerdamage";
	
	
	private KMobs plugin;
	double takenDamageByPlayers = 0;

	public PlayerDamageMetadataValue(KMobs plugin){
		this(plugin,0.0);
	}
	public PlayerDamageMetadataValue(KMobs plugin, double takenDamageByPlayers) {
		this.plugin = plugin;
		this.takenDamageByPlayers = takenDamageByPlayers;
	}
	
	public void add(double damage){
		takenDamageByPlayers += damage;
	}
	
	public double calculateDamagePercentage(double maxLife){
		return maxLife/takenDamageByPlayers;
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
		return takenDamageByPlayers;
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
		return plugin;
	}

	@Override
	public void invalidate() {
		// TODO Auto-generated method stub

	}

	@Override
	public Object value() {
		// TODO Auto-generated method stub
		return null;
	}

}
