package me.okinawaboss.chunkloader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;

public class ChunkLoaderManager implements Listener {

	private final JavaPlugin plugin;
	private final File chunkLoaderFile;
	private final FileConfiguration chunkLoaderConfig;
	private final ConfigManager configManager;

	private final Map<Block, Hologram> chunkLoaders = new HashMap<>();
	private final Map<Block, Integer> chunkLoaderTimes = new HashMap<>();
	public final Map<Block, UUID> chunkLoaderOwners = new HashMap<>(); // Store owner info
	public final Map<Block, Integer> plants = new HashMap<>();
	public final Map<Block, Integer> spawners = new HashMap<>();

	private Inventory chunkLoaderInventory;

	public ChunkLoaderManager(JavaPlugin plugin, ConfigManager configManager) {
		this.plugin = plugin;
		this.configManager = configManager; // Initialize the configManager
		chunkLoaderFile = new File(plugin.getDataFolder(), "chunkloaders.yml");
		if (!chunkLoaderFile.exists()) {
			chunkLoaderFile.getParentFile().mkdirs();
			try {
				chunkLoaderFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		chunkLoaderConfig = YamlConfiguration.loadConfiguration(chunkLoaderFile);
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	public int getNextPlantTick() {
		return ThreadLocalRandom.current().nextInt(21, 71);
	}

	public int getNextSpawnerTick() {
		return ThreadLocalRandom.current().nextInt(10, 40);
	}

	public Map<Block, Hologram> getChunkLoaders() {
		return chunkLoaders;
	}

	public Map<Block, Integer> getChunkLoaderTimes() {
		return chunkLoaderTimes;
	}

	public void loadChunkLoaders() {
		for (String worldName : chunkLoaderConfig.getKeys(false)) {
			World world = Bukkit.getWorld(worldName);
			if (world != null) {
				for (String key : chunkLoaderConfig.getConfigurationSection(worldName).getKeys(false)) {
					String[] parts = key.split("_");
					int x = Integer.parseInt(parts[0]);
					int y = Integer.parseInt(parts[1]);
					int z = Integer.parseInt(parts[2]);
					Location loc = new Location(world, x, y, z);
					int time = chunkLoaderConfig.getInt(worldName + "." + key + ".time");
					UUID owner = UUID.fromString(chunkLoaderConfig.getString(worldName + "." + key + ".owner")); // Load the owner uuid
					Block block = loc.getBlock();
					double hologramX = chunkLoaderConfig.getDouble(worldName + "." + key + ".hologramX");
					double hologramY = chunkLoaderConfig.getDouble(worldName + "." + key + ".hologramY");
					double hologramZ = chunkLoaderConfig.getDouble(worldName + "." + key + ".hologramZ");
					Location hologramLocation = new Location(world, hologramX, hologramY, hologramZ);
					setForcedChunk(block, time, hologramLocation);
					chunkLoaderOwners.put(block, owner); // Store the owner uuid in the map
				}
			}
		}
	}

	public void setForcedChunk(Block block, int totalSeconds, Location hologramLocation) {
		block.getChunk().setForceLoaded(true);

		// Remove existing hologram if it exists
		if (chunkLoaders.containsKey(block)) {
			chunkLoaders.get(block).delete();
			chunkLoaders.remove(block);
		}

		Hologram hologram = DHAPI
				.createHologram(hologramLocation.getWorld().getName() + "_" + hologramLocation.getBlockX() + "_"
						+ hologramLocation.getBlockY() + "_" + hologramLocation.getBlockZ(), hologramLocation);
		DHAPI.addHologramLine(hologram, ChatColor.DARK_RED + "Chunk Loader");
		DHAPI.addHologramLine(hologram, ChatColor.AQUA + "Time Left:");
		DHAPI.addHologramLine(hologram, formatTime(totalSeconds));

		chunkLoaders.put(block, hologram);
		chunkLoaderTimes.put(block, totalSeconds);
	}

	public void saveChunkLoaders() {
		for (Block block : chunkLoaderTimes.keySet()) {
			Location loc = block.getLocation();
			int time = chunkLoaderTimes.get(block);
			chunkLoaderConfig.set(loc.getWorld().getName() + "." + loc.getBlockX() + "_" + loc.getBlockY() + "_"
					+ loc.getBlockZ() + ".time", time);
		}
		saveChunkLoaderConfig();
	}

	private void saveChunkLoaderConfig() {
		try {
			chunkLoaderConfig.save(chunkLoaderFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void removeChunkLoaderFromConfig(Location loc) {
		String path = loc.getWorld().getName() + "." + loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ();
		chunkLoaderConfig.set(path, null);
		saveChunkLoaderConfig();
	}

	public void saveChunkLoader(Location loc, int time, UUID uuid) {
		String path = loc.getWorld().getName() + "." + loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ();
		chunkLoaderConfig.set(path + ".time", time);
		chunkLoaderConfig.set(path + ".chunkLoaded", true);
		chunkLoaderConfig.set(path + ".hologramX", loc.getX() + 0.5);
		chunkLoaderConfig.set(path + ".hologramY", loc.getY() + 2);
		chunkLoaderConfig.set(path + ".hologramZ", loc.getZ() + 0.5);
		chunkLoaderConfig.set(path + ".owner", uuid.toString()); // Save the owner's name
		saveChunkLoaderConfig();
	}

	public void setForcedChunk(Block block, int totalSeconds) {
		setForcedChunk(block, totalSeconds, true);
	}

	public void setForcedChunk(Block block, int totalSeconds, boolean adjustLocation) {
		block.getChunk().setForceLoaded(true);
		Location loc = block.getLocation().add(0.5, 0, 0.5); // Adjust location to the center of the block
		Location hologramLocation = adjustLocation ? loc.add(0, 2, 0) : loc;

		// Remove existing hologram if it exists
		if (chunkLoaders.containsKey(block)) {
			chunkLoaders.get(block).delete();
			chunkLoaders.remove(block);
		}

		Hologram hologram = DHAPI.createHologram(
				loc.getWorld().getName() + "_" + loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ(),
				hologramLocation);
		DHAPI.addHologramLine(hologram, ChatColor.DARK_RED + "Chunk Loader");
		DHAPI.addHologramLine(hologram, ChatColor.AQUA + "Time Left:");
		DHAPI.addHologramLine(hologram, formatTime(totalSeconds));

		chunkLoaders.put(block, hologram);
		chunkLoaderTimes.put(block, totalSeconds);
	}

	public void startCountdownTimer() {
		new BukkitRunnable() {
			@Override
			public void run() {
				handleChunkLoaderTimes();
				handlePlantsAndSpawners();
				showChunkBoundaries();
				updateChunkLoaderInventory(); // Update the inventory every second
			}
		}.runTaskTimer(plugin, 0L, 20L);
	}

	private void handleChunkLoaderTimes() {
		for (Block block : new ArrayList<>(chunkLoaderTimes.keySet())) {
			int timeLeft = chunkLoaderTimes.get(block) - 1;
			UUID owner = chunkLoaderOwners.get(block);

			if (timeLeft >= 1) {
				saveChunkLoader(block.getLocation(), timeLeft, owner);
			}

			if (timeLeft <= 0) {
				Hologram hologram = chunkLoaders.get(block);
				if (hologram != null) {
					hologram.delete();
					chunkLoaders.remove(block);
				}
				chunkLoaderTimes.remove(block);
				chunkLoaderOwners.remove(block);

				Block belowBlock = block.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN);
				if (belowBlock.getType().equals(Material.REDSTONE_LAMP)) {
					belowBlock.setType(Material.AIR);
				}
				block.getChunk().setForceLoaded(false);

				Location chunkLoaderLocation = block.getLocation().subtract(0.5, 2, 0.5);
				removeChunkLoaderFromConfig(chunkLoaderLocation);

			} else {
				chunkLoaderTimes.put(block, timeLeft);
				Hologram hologram = chunkLoaders.get(block);
				if (hologram != null) {
					DHAPI.setHologramLine(hologram, 2, formatTime(timeLeft));
				}
			}
		}
	}

	private void handlePlantsAndSpawners() {
		int currentPlantTick = getNextPlantTick();
		int currentSpawnerTick = getNextSpawnerTick();

		handlePlantGrowth(currentPlantTick);
		handleSpawnerSpawning(currentSpawnerTick);
	}

	private void handlePlantGrowth(int currentTick) {
		Iterator<Map.Entry<Block, Integer>> iterator = plants.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Block, Integer> entry = iterator.next();
			Block block = entry.getKey();
			int tick = entry.getValue();

			if (block.getChunk().isForceLoaded()) {
				if (tick == currentTick) {
					growPlant(block);
					iterator.remove();
				}
			} else {
				iterator.remove();
			}
		}
	}

	private void handleSpawnerSpawning(int currentTick) {
		Iterator<Map.Entry<Block, Integer>> iterator = spawners.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Block, Integer> entry = iterator.next();
			Block block = entry.getKey();
			int tick = entry.getValue();
			if (block.getChunk().isForceLoaded()) {
				if (tick == currentTick) {
					spawnMobs(block);
					iterator.remove();
				}
			} else {
				iterator.remove();
			}
		}
	}

	private void showChunkBoundaries() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (isChunkForcer(player.getInventory().getItemInMainHand())) {
				showChunkBoundary(player);
			}
		}
	}

	public void showChunkBoundary(Player player) {
		Chunk chunk = player.getLocation().getChunk();
		World world = chunk.getWorld();
		int chunkX = chunk.getX() << 4;
		int chunkZ = chunk.getZ() << 4;
		int y = player.getLocation().getBlockY();

		// Define red dust particle with color and increased size
		DustOptions dustOptions = new DustOptions(Color.RED, 2.0f); // Increase size to 2.0f

		// Loop through the corners of the chunk to form a square
		for (int i = 0; i <= 16; i++) {
			// Draw the four sides of the chunk
			Location loc1 = new Location(world, chunkX + i, y, chunkZ);
			Location loc2 = new Location(world, chunkX + 16, y, chunkZ + i);
			Location loc3 = new Location(world, chunkX + 16 - i, y, chunkZ + 16);
			Location loc4 = new Location(world, chunkX, y, chunkZ + 16 - i);

			// Spawn multiple particles at each corner to increase density
			spawnParticles(player, loc1, dustOptions);
			spawnParticles(player, loc2, dustOptions);
			spawnParticles(player, loc3, dustOptions);
			spawnParticles(player, loc4, dustOptions);
		}
	}

	private void spawnParticles(Player player, Location loc, DustOptions dustOptions) {
		World world = loc.getWorld();

		// Spawn particles at the player's current Y level
		player.spawnParticle(Particle.DUST, loc, 3, dustOptions);

		// Check for the highest block at this location and spawn particles there
		Location highestLoc = world.getHighestBlockAt(loc).getLocation().add(0, 1, 0);
		player.spawnParticle(Particle.DUST, highestLoc, 3, dustOptions);

		// Spawn particles at the maximum height level
		Location maxYLoc = new Location(world, loc.getX(), world.getMaxHeight() - 1, loc.getZ());
		player.spawnParticle(Particle.DUST, maxYLoc, 3, dustOptions);
	}

	private String formatTime(int totalSeconds) {
		int hours = totalSeconds / 3600;
		int minutes = (totalSeconds % 3600) / 60;
		int seconds = totalSeconds % 60;
		return "" + ChatColor.DARK_GREEN + hours + "h " + minutes + "m " + seconds + "s";
	}

	public void dropChunkLoaderItem(Block block, int remainingTime) {
		int hours = remainingTime / 3600;
		int minutes = (remainingTime % 3600) / 60;
		int seconds = remainingTime % 60;

		ItemStack chunkLoaderItem = createChunkLoaderItem(String.valueOf(hours), String.valueOf(minutes),
				String.valueOf(seconds));
		block.getWorld().dropItemNaturally(block.getLocation(), chunkLoaderItem);
	}

	public ItemStack createChunkLoaderItem(String hours, String minutes, String seconds) {
		// Get the material from the config
		Material chunkLoaderMaterial = configManager.getChunkLoaderItem();
		if (chunkLoaderMaterial == null) {
			chunkLoaderMaterial = Material.REDSTONE_LAMP; // Fallback to default if config value is invalid
		}

		// Create the item stack and meta
		ItemStack itemStack = new ItemStack(chunkLoaderMaterial);
		ItemMeta itemMeta = itemStack.getItemMeta();

		// Set the display name from the config
		String displayName = ChatColor.translateAlternateColorCodes('&',
				configManager.getConfig().getString("item_display_name", "&4Chunk Loader"));
		itemMeta.setDisplayName(displayName);

		// Set the lore from the config and replace the {DURATION} placeholder
		List<String> loreConfig = configManager.getConfig().getStringList("item_lore");
		List<String> lore = new ArrayList<>();
		for (String line : loreConfig) {
			String formattedLine = ChatColor.translateAlternateColorCodes('&',
					line.replace("{DURATION}", hours + "h " + minutes + "m " + seconds + "s"));
			lore.add(formattedLine);
		}
		itemMeta.setLore(lore);

		// Apply the meta to the item stack
		itemStack.setItemMeta(itemMeta);

		return itemStack;
	}

	public void growPlant(Block block) {
		BlockData blockData = block.getBlockData();
		if (blockData instanceof Ageable) {
			Ageable ageable = (Ageable) blockData;
			if (ageable.getAge() < ageable.getMaximumAge()) {
				ageable.setAge(ageable.getAge() + 1);
				block.setBlockData(ageable);
				summonCircleParticle(block.getLocation().add(0.5D, 0.5D, 0.5D));
			}
		}
	}

	public void spawnMobs(Block block) {
		if (block.getType() == Material.SPAWNER) {
			CreatureSpawner spawner = (CreatureSpawner) block.getState();
			EntityType entityType = spawner.getSpawnedType();
			int currentLightLevel = block.getLightLevel();

			if (LightLevelManager.canSpawn(entityType, currentLightLevel)) {
				Location location = block.getLocation().add(0.5, 1, 0.5);
				summonFireHelix(location);
				block.getWorld().spawnEntity(location, entityType);
				block.getWorld().spawnEntity(location, entityType);
				block.getWorld().spawnEntity(location, entityType);
				block.getWorld().spawnEntity(location, entityType);
			}
		}
	}

	public void summonFireHelix(final Location loc) {
		new BukkitRunnable() {
			double phi = 0.0D;

			public void run() {
				this.phi += 0.3141592653589793D;
				for (double theta = 0.0D; theta <= 6.283185307179586D; theta += 0.07853981633974483D) {
					for (double d1 = 0.0D; d1 <= 1.0D; d1++) {
						double r = 0.4D;
						double x = r * (6.283185307179586D - theta) * r * Math.sin(theta + this.phi + d1 * Math.PI);
						double y = r * theta;
						double z = r * (6.283185307179586D - theta) * r * Math.cos(theta + this.phi + d1 * Math.PI);
						loc.add(x, y, z);
						loc.getWorld().spawnParticle(org.bukkit.Particle.DRIPPING_LAVA, loc, 1);
						loc.subtract(x, y, z);
					}
				}
				if (this.phi > Math.PI)
					cancel();
			}
		}.runTaskTimer(this.plugin, 0L, 1L);
	}

	public void summonCircleParticle(final Location loc) {
		new BukkitRunnable() {
			double phi = 0.0D;

			public void run() {
				this.phi += 0.3141592653589793D;
				for (double theta = 0.0D; theta <= 6.283185307179586D; theta += 0.07853981633974483D) {
					double r = 0.75D;
					double x = r * Math.cos(theta) * Math.sin(this.phi);
					double y = r * Math.cos(this.phi) - 0.5D;
					double z = r * Math.sin(theta) * Math.sin(this.phi);
					loc.add(x, y, z);
					loc.getWorld().spawnParticle(org.bukkit.Particle.DRIPPING_WATER, loc, 1);
					loc.subtract(x, y, z);
				}
				if (this.phi > Math.PI)
					cancel();
			}
		}.runTaskTimer(this.plugin, 0L, 1L);
	}

	public boolean isChunkForcer(ItemStack itemStack) {
		Material chunkLoaderItem = configManager.getChunkLoaderItem();
		if (itemStack.getType() != chunkLoaderItem || !itemStack.hasItemMeta()) {
			return false;
		}

		ItemMeta meta = itemStack.getItemMeta();
		String displayName = ChatColor.translateAlternateColorCodes('&',
				configManager.getConfig().getString("item_display_name", "&4Chunk Loader"));

		if (!meta.getDisplayName().equals(displayName)) {
			return false;
		}

		List<String> loreConfig = configManager.getConfig().getStringList("item_lore");
		List<String> formattedLore = new ArrayList<>();
		for (String line : loreConfig) {
			String formattedLine = ChatColor.translateAlternateColorCodes('&', line.replace("{DURATION}", ""));
			formattedLore.add(formattedLine);
		}

		List<String> itemLore = meta.getLore();
		if (itemLore == null || itemLore.size() != formattedLore.size()) {
			return false;
		}

		for (int i = 0; i < itemLore.size(); i++) {
			if (!itemLore.get(i).startsWith(formattedLore.get(i))) {
				return false;
			}
		}

		return true;
	}

	private void moveChunkLoader(Block block, Block newBlock, Location newLocation, Chunk oldChunk, Chunk newChunk,
			UUID ownerUUID, String reason) {
		if (chunkLoaders.containsKey(block)) {
			Hologram hologram = chunkLoaders.get(block);
			if (hologram != null) {
				hologram.delete();
				chunkLoaders.remove(block);
			}

			// Save the current block location and time
			int timeLeft = chunkLoaderTimes.get(block);
			removeChunkLoaderFromConfig(block.getLocation()); // Remove the old location

			// Create a new hologram at the new location
			Hologram newHologram = DHAPI.createHologram(newLocation.getWorld().getName() + "_" + newLocation.getBlockX()
					+ "_" + newLocation.getBlockY() + "_" + newLocation.getBlockZ(), newLocation);
			DHAPI.addHologramLine(newHologram, ChatColor.DARK_RED + "Chunk Loader");
			DHAPI.addHologramLine(newHologram, ChatColor.AQUA + "Time Left:");
			DHAPI.addHologramLine(newHologram, formatTime(timeLeft));

			// Update the maps with the new block and hologram
			chunkLoaders.put(newBlock, newHologram);
			chunkLoaderTimes.put(newBlock, timeLeft);
			chunkLoaderTimes.remove(block);
			chunkLoaderOwners.put(newBlock, ownerUUID); // Retain the owner name

			// Save the new location and time with owner
			saveChunkLoader(newBlock.getLocation(), timeLeft, ownerUUID);

			// Fire the custom event
			if (!oldChunk.equals(newChunk)) {
				ChunkLoaderMoveEvent event = new ChunkLoaderMoveEvent(oldChunk, newChunk, block.getLocation(),
						newBlock.getLocation(), ownerUUID, System.currentTimeMillis(), reason);
				Bukkit.getServer().getPluginManager().callEvent(event);
			}

			// Ensure the new chunk is force loaded
			newChunk.setForceLoaded(true);
		}
	}

	public void findBlocks(Chunk c) {
		World w = c.getWorld();
		int cx = c.getX() << 4;
		int cz = c.getZ() << 4;
		for (int x = cx; x < cx + 16; x++) {
			for (int z = cz; z < cz + 16; z++) {
				for (int y = 0; y < 128; y++) {
					Block block = w.getBlockAt(x, y, z);
					BlockData bd = block.getBlockData();
					if (bd instanceof Ageable && !this.plants.containsKey(block)) {
						Ageable age = (Ageable) bd;
						double progress = (age.getAge() * 100 / age.getMaximumAge());
						if (progress != 100.0D)
							this.plants.put(block, Integer.valueOf(getNextPlantTick()));
					}
					if (block.getType().equals(Material.SPAWNER) && !this.spawners.containsKey(block))
						this.spawners.put(block, Integer.valueOf(getNextSpawnerTick()));
				}
			}
		}
	}

	public Location findSafeLocation(Location loc) {
		World world = loc.getWorld();
		int x = loc.getBlockX();
		int y = loc.getBlockY();
		int z = loc.getBlockZ();

		// First, check for honey blocks in the vicinity
		Location honeyBlockLocation = findNearbyHoneyBlock(loc);
		if (honeyBlockLocation != null) {
			return honeyBlockLocation.add(0.5, 1, 0.5); // Center the player on the honey block
		}

		// If no honey blocks are found, check for other safe spots around the original
		// location
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				for (int dy = -2; dy <= 2; dy++) {
					Location checkLoc = new Location(world, x + dx, y + dy, z + dz);
					if (isSafeLocation(checkLoc)) {
						return checkLoc.add(0.5, 0, 0.5); // Center the player in the block
					}
				}
			}
		}
		// If no safe spot is found, return the original location
		return loc;
	}

	private Location findNearbyHoneyBlock(Location loc) {
		World world = loc.getWorld();
		int x = loc.getBlockX();
		int y = loc.getBlockY();
		int z = loc.getBlockZ();

		// Check for honey blocks within a radius of 2 blocks
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				for (int dy = -2; dy <= 2; dy++) {
					Block checkBlock = new Location(world, x + dx, y + dy, z + dz).getBlock();
					if (checkBlock.getType() == Material.HONEY_BLOCK) {
						return checkBlock.getLocation();
					}
				}
			}
		}
		return null;
	}

	private boolean isSafeLocation(Location loc) {
		Block block = loc.getBlock();
		Block below = block.getRelative(BlockFace.DOWN);
		Block above = block.getRelative(BlockFace.UP);

		// Check if the block and the one above are air and the block below is solid
		return block.getType() == Material.AIR && above.getType() == Material.AIR && below.getType().isSolid()
				&& below.getType() != Material.LAVA && below.getType() != Material.CACTUS;
	}

	public void handlePistonExtendEvent(BlockPistonExtendEvent event) {
		List<Block> movedBlocks = event.getBlocks();
		String reason = "Piston Extend";

		for (Block block : movedBlocks) {
			if (chunkLoaders.containsKey(block)) {
				Block newBlock = block.getRelative(event.getDirection());
				Location newLocation = newBlock.getLocation().add(0.5, 2, 0.5);
				Chunk oldChunk = block.getChunk();
				Chunk newChunk = newBlock.getChunk();
				
				UUID ownerUUID = chunkLoaderOwners.get(block); // Retrieve owner name from the map
				moveChunkLoader(block, newBlock, newLocation, oldChunk, newChunk, ownerUUID, reason);
			}
		}
	}

	public void handlePistonRetractEvent(BlockPistonRetractEvent event) {
		List<Block> movedBlocks = event.getBlocks();
		String reason = "Piston Retract";

		for (Block block : movedBlocks) {
			if (chunkLoaders.containsKey(block)) {
				Block newBlock = block.getRelative(event.getDirection());
				Location newLocation = newBlock.getLocation().add(0.5, 2, 0.5);
				Chunk oldChunk = block.getChunk();
				Chunk newChunk = newBlock.getChunk();
				UUID ownerUUID = chunkLoaderOwners.get(block); // Retrieve owner name from the map
				
				moveChunkLoader(block, newBlock, newLocation, oldChunk, newChunk, ownerUUID, reason);
			}
		}
	}

    public void openChunkLoaderList(Player player) {
        chunkLoaderInventory = Bukkit.createInventory(null, 54, "Active Chunk Loaders");

        List<Block> sortedChunkLoaders = chunkLoaders.keySet().stream()
            .sorted(Comparator.comparing(block -> block.getLocation().toString()))
            .collect(Collectors.toList());

        for (Block block : sortedChunkLoaders) {
            Location loc = block.getLocation();
            UUID owner = chunkLoaderOwners.get(block); // Retrieve owner
            int timeLeft = chunkLoaderTimes.get(block);

            ItemStack item = createChunkLoaderItem(owner, loc, timeLeft);
            chunkLoaderInventory.addItem(item);
        }

        player.openInventory(chunkLoaderInventory);
    }

    public void openChunkLoaderList(Player player, UUID owner) {
        chunkLoaderInventory = Bukkit.createInventory(null, 54, "Active Chunk Loaders");

        List<Block> sortedChunkLoaders = chunkLoaders.keySet().stream()
            .filter(block -> chunkLoaderOwners.get(block).equals(owner))
            .sorted(Comparator.comparing(block -> block.getLocation().toString()))
            .collect(Collectors.toList());

        for (Block block : sortedChunkLoaders) {
            Location loc = block.getLocation();
            int timeLeft = chunkLoaderTimes.get(block);

            ItemStack item = createChunkLoaderItem(owner, loc, timeLeft);
            chunkLoaderInventory.addItem(item);
        }

        player.openInventory(chunkLoaderInventory);
    }

    private ItemStack createChunkLoaderItem(UUID owner, Location loc, int timeLeft) {
        ItemStack itemStack = new ItemStack(Material.REDSTONE_LAMP);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.DARK_RED + "Chunk Loader");

        List<String> lore = Arrays.asList(ChatColor.GREEN + "Owner: " + Bukkit.getOfflinePlayer(owner).getName(),
                ChatColor.GREEN + "Location: " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ(),
                ChatColor.DARK_RED + "Time Left: " + formatTime(timeLeft));

        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private void updateChunkLoaderInventory() {
        if (chunkLoaderInventory == null)
            return;
        chunkLoaderInventory.clear();

        List<Block> sortedChunkLoaders = chunkLoaders.keySet().stream()
            .sorted(Comparator.comparing(block -> block.getLocation().toString()))
            .collect(Collectors.toList());

        for (Block block : sortedChunkLoaders) {
            Location loc = block.getLocation();
            UUID owner = chunkLoaderOwners.get(block); // Retrieve owner
            int timeLeft = chunkLoaderTimes.get(block);

            ItemStack item = createChunkLoaderItem(owner, loc, timeLeft);
            chunkLoaderInventory.addItem(item);
        }
    }

	public void addSpawner(Block block) {
		int tick = getNextSpawnerTick();
		spawners.put(block, tick);
	}

	public void addPlant(Block block) {
		int tick = getNextPlantTick();
		plants.put(block, tick);
	}

	public boolean hasChunkLoaderInChunk(Chunk chunk) {
		for (Block block : chunkLoaders.keySet()) {
			if (block.getChunk().equals(chunk)) {
				return true;
			}
		}
		return false;
	}

	public void removePlantsAndSpawnersInChunk(Chunk chunk) {
		Iterator<Map.Entry<Block, Integer>> plantIterator = plants.entrySet().iterator();
		while (plantIterator.hasNext()) {
			Map.Entry<Block, Integer> entry = plantIterator.next();
			if (entry.getKey().getChunk().equals(chunk)) {
				plantIterator.remove();
			}
		}

		Iterator<Map.Entry<Block, Integer>> spawnerIterator = spawners.entrySet().iterator();
		while (spawnerIterator.hasNext()) {
			Map.Entry<Block, Integer> entry = spawnerIterator.next();
			if (entry.getKey().getChunk().equals(chunk)) {
				spawnerIterator.remove();
			}
		}
	}

	public void addPlantsAndSpawnersInChunk(Chunk chunk) {
		World world = chunk.getWorld();
		int cx = chunk.getX() << 4;
		int cz = chunk.getZ() << 4;

		for (int x = cx; x < cx + 16; x++) {
			for (int z = cz; z < cz + 16; z++) {
				for (int y = 0; y < 128; y++) { // Assuming the relevant height range is 0-128
					Block block = world.getBlockAt(x, y, z);
					BlockData blockData = block.getBlockData();

					if (blockData instanceof Ageable && !plants.containsKey(block)) {
						Ageable ageable = (Ageable) blockData;
						if (ageable.getAge() < ageable.getMaximumAge()) {
							plants.put(block, getNextPlantTick());
						}
					}

					if (block.getType().equals(Material.SPAWNER) && !spawners.containsKey(block)) {
						spawners.put(block, getNextSpawnerTick());
					}
				}
			}
		}
	}
}
