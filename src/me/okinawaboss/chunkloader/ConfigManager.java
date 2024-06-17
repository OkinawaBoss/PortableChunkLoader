package me.okinawaboss.chunkloader;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Material;

public class ConfigManager {
	private final JavaPlugin plugin;
	private final FileConfiguration config;

	public ConfigManager(JavaPlugin plugin) {
		this.plugin = plugin;
		plugin.saveDefaultConfig();
		this.config = plugin.getConfig();
	}

	public Material getChunkLoaderItem() {
		String itemName = config.getString("chunk_loader_item", "REDSTONE_LAMP");
		try {
			return Material.valueOf(itemName.toUpperCase());
		} catch (IllegalArgumentException e) {
			plugin.getLogger().warning("Invalid material specified for chunk_loader_item: " + itemName);
			return Material.REDSTONE_LAMP; // Default fallback
		}
	}

	public void reloadConfig() {
		plugin.reloadConfig();
	}

	public boolean isItemPowered() {
		return config.getBoolean("item_powered", false);
	}

	public boolean isItemEnderEyeEnabled() {
		return config.getBoolean("item_ender_eye_enabled", false);
	}

	public FileConfiguration getConfig() {
		return config;
	}
}
