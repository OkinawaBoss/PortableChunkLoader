package me.okinawaboss.chunkloader;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

	private ChunkLoaderManager chunkLoaderManager;
	private EventListener eventListener;
	private CommandHandler commandHandler;
	private ConfigManager configManager;

	@Override
	public void onEnable() {
		configManager = new ConfigManager(this); // Initialize configManager first
		chunkLoaderManager = new ChunkLoaderManager(this, configManager); // Pass the initialized configManager
		eventListener = new EventListener(chunkLoaderManager, configManager, this);
		commandHandler = new CommandHandler(chunkLoaderManager, configManager);

		getCommand("cl").setExecutor(commandHandler); // Register the /cl command
	    getCommand("cl").setTabCompleter(commandHandler);

		chunkLoaderManager.loadChunkLoaders();
		chunkLoaderManager.startCountdownTimer();

		getServer().getPluginManager().registerEvents(eventListener, this);
	}

	@Override
	public void onDisable() {
		chunkLoaderManager.saveChunkLoaders();
	}

	public ConfigManager getConfigManager() {
		return configManager;
	}
}
