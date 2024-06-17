package me.okinawaboss.chunkloader;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandHandler implements CommandExecutor, TabCompleter {

    private final ChunkLoaderManager chunkLoaderManager;
    private final ConfigManager configManager;

    public CommandHandler(ChunkLoaderManager chunkLoaderManager, ConfigManager configManager) {
        this.chunkLoaderManager = chunkLoaderManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("cl") || command.getName().equalsIgnoreCase("chunkloader")) {
            if (args.length == 0) {
                showHelp(sender);
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "give":
                    if (!sender.hasPermission("chunkloader.give")) {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                        return true;
                    }

                    if (args.length < 5) {
                        sender.sendMessage(ChatColor.RED + "Usage: /cl give <player> <hours> <minutes> <seconds>");
                        return true;
                    }

                    Player player = Bukkit.getPlayer(args[1]);
                    if (player == null) {
                        sender.sendMessage(ChatColor.RED + "Player not found");
                        return true;
                    }

                    String hours = args[2];
                    String minutes = args[3];
                    String seconds = args[4];
                    player.getInventory().addItem(chunkLoaderManager.createChunkLoaderItem(hours, minutes, seconds));
                    player.sendMessage(ChatColor.GREEN + "You have been given a chunk loader");
                    return true;

                case "list":
                    if (!sender.hasPermission("chunkloader.list")) {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                        return true;
                    }

                    if (args.length == 1) {
                        if (!(sender instanceof Player)) {
                            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                            return true;
                        }

                        Player listPlayer = (Player) sender;
                        chunkLoaderManager.openChunkLoaderList(listPlayer);
                        return true;
                    } else if (args.length == 2) {
                        @SuppressWarnings("deprecation")
                        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(args[1]);
                        if (targetPlayer == null || !targetPlayer.hasPlayedBefore()) {
                            sender.sendMessage(ChatColor.RED + "Player not found");
                            return true;
                        }

                        chunkLoaderManager.openChunkLoaderList((Player) sender, targetPlayer.getUniqueId());
                        return true;
                    } else {
                        sender.sendMessage(ChatColor.RED + "Usage: /cl list [player]");
                        return true;
                    }

                case "reload":
                    if (!sender.hasPermission("chunkloader.reload")) {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                        return true;
                    }

                    configManager.reloadConfig();
                    sender.sendMessage(ChatColor.GREEN + "Configuration reloaded.");
                    return true;

                default:
                    showHelp(sender);
                    return true;
            }
        }

        return false;
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Chunk Loader Commands ===");
        sender.spigot().sendMessage(createClickableCommandSuggestion(ChatColor.YELLOW + "/cl give <player> <hours> <minutes> <seconds>", "/cl give <player> <hours> <minutes> <seconds>"));
        sender.spigot().sendMessage(createClickableCommandSuggestion(ChatColor.YELLOW + "/cl list [player]", "/cl list [player]"));
        sender.spigot().sendMessage(createClickableCommandSuggestion(ChatColor.YELLOW + "/cl reload", "/cl reload"));
    }

    private BaseComponent createClickableCommandSuggestion(String text, String command) {
        TextComponent message = new TextComponent(text);
        message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
        return message;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("cl") || command.getName().equalsIgnoreCase("chunkloader")) {
            if (args.length == 1) {
                return Arrays.asList("give", "list", "reload");
            } else if (args[0].equalsIgnoreCase("give")) {
                if (args.length == 2) {
                    List<String> playerNames = new ArrayList<>();
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        playerNames.add(player.getName());
                    }
                    return playerNames;
                } else if (args.length > 5) {
                    return Collections.emptyList(); // No more suggestions if there are too many arguments
                } else {
                    return Collections.emptyList();
                }
            } else if (args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("reload")) {
                if (args.length > 1) {
                    return Collections.emptyList(); // No more suggestions if there are too many arguments
                }
            }
        }
        return Collections.emptyList();
    }
}
