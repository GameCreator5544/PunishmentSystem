package net.hyperlowmc.plugins.punishPaper.commands;

import net.hyperlowmc.plugins.punishPaper.PunishPaper;
import net.hyperlowmc.plugins.punishPaper.gui.CheckGUI;
import net.hyperlowmc.plugins.punishPaper.models.PlayerData;
import net.hyperlowmc.plugins.punishPaper.models.Punishment;
import net.hyperlowmc.plugins.punishPaper.utils.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CheckCommand implements CommandExecutor, TabCompleter {

    private final PunishPaper plugin;

    public CheckCommand(PunishPaper plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /check <ban-id|player> [forgive]");
            return true;
        }

        String query = args[0].replace("#", "");

        // Check if it's a ban ID
        Punishment punishment = plugin.getDataManager().getPunishmentById(query);
        if (punishment != null) {
            // If sender is player and no "forgive" argument, open GUI
            if (sender instanceof Player && args.length == 1) {
                CheckGUI.openPunishmentGUI((Player) sender, punishment, plugin);
            } else {
                displayPunishment(sender, punishment);

                // Handle forgive
                if (args.length > 1 && args[1].equalsIgnoreCase("forgive")) {
                    if (sender.hasPermission("punishments.forgive")) {
                        plugin.getPunishmentManager().forgivePunishment(query);
                        sender.sendMessage(ChatColor.GREEN + "Forgiven punishment #" + query);
                    } else {
                        sender.sendMessage(ChatColor.RED + "No permission to forgive!");
                    }
                }
            }
            return true;
        }

        // Check if it's a player (online or offline)
        Player target = Bukkit.getPlayer(query);
        if (target != null) {
            // Player is online
            if (sender instanceof Player) {
                CheckGUI.openPlayerHistoryGUI((Player) sender, target.getUniqueId(), target.getName(), plugin);
            } else {
                displayPlayerHistory(sender, target.getUniqueId(), target.getName());
            }
            return true;
        }

        // Search offline players by name
        PlayerData foundData = findPlayerByName(query);
        if (foundData != null) {
            if (sender instanceof Player) {
                CheckGUI.openPlayerHistoryGUI((Player) sender, foundData.getUuid(), foundData.getLastKnownName(), plugin);
            } else {
                displayPlayerHistory(sender, foundData.getUuid(), foundData.getLastKnownName());
            }
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Punishment or player not found!");
        return true;
    }

    private PlayerData findPlayerByName(String name) {
        // Search through all stored player data
        for (PlayerData data : plugin.getDataManager().getAllPlayerData()) {
            if (data.getLastKnownName().equalsIgnoreCase(name)) {
                return data;
            }
        }
        return null;
    }

    private void displayPunishment(CommandSender sender, Punishment punishment) {
        sender.sendMessage(ChatColor.GOLD + "=== Punishment #" + punishment.getBanId() + " ===");
        sender.sendMessage(ChatColor.YELLOW + "Player: " + ChatColor.WHITE + punishment.getPlayerName());
        sender.sendMessage(ChatColor.YELLOW + "Type: " + ChatColor.WHITE + punishment.getType());
        sender.sendMessage(ChatColor.YELLOW + "Reason: " + ChatColor.WHITE + punishment.getReason());
        sender.sendMessage(ChatColor.YELLOW + "Category: " + ChatColor.WHITE +
                (punishment.getCategory() != null ? punishment.getCategory() : "Manual"));
        sender.sendMessage(ChatColor.YELLOW + "Punished by: " + ChatColor.WHITE + punishment.getPunishedBy());
        sender.sendMessage(ChatColor.YELLOW + "Active: " + ChatColor.WHITE + punishment.isActive());

        if (punishment.getDuration() == -1) {
            sender.sendMessage(ChatColor.YELLOW + "Duration: " + ChatColor.WHITE + "Permanent");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Remaining: " + ChatColor.WHITE +
                    TimeUtil.formatDuration(punishment.getRemainingTime()));
        }

        sender.sendMessage(ChatColor.GRAY + "Use '/check " + punishment.getBanId() + " forgive' to forgive this punishment");
    }

    private void displayPlayerHistory(CommandSender sender, UUID uuid, String name) {
        PlayerData data = plugin.getDataManager().getPlayerData(uuid);
        if (data == null) {
            sender.sendMessage(ChatColor.RED + "No punishment history found!");
            return;
        }

        sender.sendMessage(ChatColor.GOLD + "=== " + name + "'s Punishment History ===");
        sender.sendMessage(ChatColor.YELLOW + "Total punishments: " + ChatColor.WHITE + data.getPunishments().size());

        List<Punishment> punishments = data.getPunishments();
        for (int i = Math.max(0, punishments.size() - 5); i < punishments.size(); i++) {
            Punishment p = punishments.get(i);
            String status = p.isActive() ? ChatColor.RED + "[ACTIVE]" : ChatColor.GRAY + "[INACTIVE]";
            sender.sendMessage(status + ChatColor.WHITE + " #" + p.getBanId() + " - " +
                    p.getType() + " - " + p.getReason());
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            String search = args[0].toLowerCase();
            // Suggest online players
            suggestions = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(search))
                    .collect(Collectors.toList());

            // Also suggest offline players from stored data
            for (PlayerData data : plugin.getDataManager().getAllPlayerData()) {
                String name = data.getLastKnownName();
                if (name.toLowerCase().startsWith(search) && !suggestions.contains(name)) {
                    suggestions.add(name);
                }
            }
        } else if (args.length == 2) {
            if ("forgive".startsWith(args[1].toLowerCase())) {
                suggestions.add("forgive");
            }
        }

        return suggestions;
    }
}