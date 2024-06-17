package me.okinawaboss.chunkloader;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.type.EndPortalFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

public class EventListener implements Listener {

	private final ChunkLoaderManager chunkLoaderManager;
	private final ConfigManager configManager;
	private final JavaPlugin plugin; // Add a plugin instance

	public EventListener(ChunkLoaderManager chunkLoaderManager, ConfigManager configManager, JavaPlugin plugin) {
		this.chunkLoaderManager = chunkLoaderManager;
		this.configManager = configManager;
		this.plugin = plugin;

	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if (chunkLoaderManager.isChunkForcer(event.getItemInHand())) {
			String timeLeft = ChatColor
					.stripColor(event.getItemInHand().getItemMeta().getLore().get(2).replace("Duration: ", ""));

			String[] timeParts = timeLeft.split(" ");
			int hours = Integer.parseInt(timeParts[0].replace("h", ""));
			int minutes = Integer.parseInt(timeParts[1].replace("m", ""));
			int seconds = Integer.parseInt(timeParts[2].replace("s", ""));
			int totalSeconds = hours * 3600 + minutes * 60 + seconds;

			if (configManager.isItemPowered()) {
				BlockData blockData = event.getBlock().getBlockData();
				if (blockData instanceof Lightable) {
					Lightable lightable = (Lightable) blockData;
					lightable.setLit(true);
					event.getBlock().setBlockData(lightable);
				}

			}
			if (configManager.isItemEnderEyeEnabled() && event.getBlock().getType() == Material.END_PORTAL_FRAME) {
				BlockData blockData = event.getBlock().getBlockData();
				if (blockData instanceof EndPortalFrame) {
					EndPortalFrame epf = (EndPortalFrame) blockData;
					epf.setEye(true);
					event.getBlock().setBlockData(epf);
				}
			}

			chunkLoaderManager.chunkLoaderOwners.put(event.getBlock(), event.getPlayer().getUniqueId()); // Store owner
			chunkLoaderManager.setForcedChunk(event.getBlock(), totalSeconds);
			chunkLoaderManager.saveChunkLoader(event.getBlock().getLocation(), totalSeconds,
					event.getPlayer().getUniqueId());
			chunkLoaderManager.findBlocks(event.getBlock().getChunk());
		}
	}

	@EventHandler
	public void onBlockRedstoneChange(BlockRedstoneEvent event) {
		Block block = event.getBlock();
		if (block.getType() == Material.REDSTONE_LAMP && chunkLoaderManager.getChunkLoaders().containsKey(block)) {
			// Schedule a task to set the lamp to lit after 1 tick
			Bukkit.getScheduler().runTaskLater(plugin, () -> {
				BlockData blockData = block.getBlockData();
				if (blockData instanceof Lightable) {
					Lightable lightable = (Lightable) blockData;
					lightable.setLit(true);
					block.setBlockData(lightable);
				}
			}, 1L);
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getView().getTitle().equals("Active Chunk Loaders")) {
			event.setCancelled(true); // Prevent item from being taken

			if (event.getCurrentItem() == null || event.getCurrentItem().getType() != Material.REDSTONE_LAMP) {
				return;
			}

			ItemStack item = event.getCurrentItem();
			ItemMeta meta = item.getItemMeta();
			if (meta == null || meta.getLore() == null) {
				return;
			}

			Player player = (Player) event.getWhoClicked();
			String[] locInfo = ChatColor.stripColor(meta.getLore().get(1)).replace("Location: ", "").split(", ");
			int x = Integer.parseInt(locInfo[0]);
			int y = Integer.parseInt(locInfo[1]);
			int z = Integer.parseInt(locInfo[2]);
			World world = player.getWorld(); // Assuming same world, adjust if needed

			Location teleportLocation = new Location(world, x, y, z);
			Location safeLocation = chunkLoaderManager.findSafeLocation(teleportLocation);
			player.teleport(safeLocation);
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Block brokenBlock = event.getBlock();
		Location blockLocation = brokenBlock.getLocation();

		if (chunkLoaderManager.getChunkLoaders().containsKey(brokenBlock)) {
			chunkLoaderManager.getChunkLoaders().get(brokenBlock).delete();
			chunkLoaderManager.getChunkLoaders().remove(brokenBlock);

			int remainingTime = chunkLoaderManager.getChunkLoaderTimes().get(brokenBlock);
			chunkLoaderManager.getChunkLoaderTimes().remove(brokenBlock);
			chunkLoaderManager.dropChunkLoaderItem(brokenBlock, remainingTime);
			chunkLoaderManager.removeChunkLoaderFromConfig(blockLocation);

			// Stop the chunk from being force-loaded
			if (!chunkLoaderManager.hasChunkLoaderInChunk(event.getBlock().getChunk())) {
				// Remove plants and spawners from the old chunk
				chunkLoaderManager.removePlantsAndSpawnersInChunk(event.getBlock().getChunk());
				blockLocation.getChunk().setForceLoaded(false);
			}
		}
	}

	@EventHandler
	public void onBlockPistonExtend(BlockPistonExtendEvent event) {
		chunkLoaderManager.handlePistonExtendEvent(event);
	}

	@EventHandler
	public void onBlockPistonRetract(BlockPistonRetractEvent event) {
		chunkLoaderManager.handlePistonRetractEvent(event);
	}

	@EventHandler
	public void onChunkLoaderMove(ChunkLoaderMoveEvent event) {
		Chunk oldChunk = event.getOldChunk();
		Chunk newChunk = event.getNewChunk();
		// Find blocks in the new chunk
		chunkLoaderManager.findBlocks(newChunk);
		// Check if there are any chunk loaders left in the old chunk
		if (!chunkLoaderManager.hasChunkLoaderInChunk(oldChunk)) {
			// Remove plants and spawners from the old chunk
			chunkLoaderManager.removePlantsAndSpawnersInChunk(oldChunk);
			oldChunk.setForceLoaded(false);

		}
		newChunk.setForceLoaded(false);
		chunkLoaderManager.addPlantsAndSpawnersInChunk(newChunk);
	}
}
