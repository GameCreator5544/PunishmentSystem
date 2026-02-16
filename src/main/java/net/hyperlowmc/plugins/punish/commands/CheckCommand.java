package net.hyperlowmc.plugins.punish.commands;

import net.hyperlowmc.plugins.punish.Punish;
import net.hyperlowmc.plugins.punish.models.PlayerData;
import net.hyperlowmc.plugins.punish.models.Punishment;
import net.hyperlowmc.plugins.punish.utils.TimeUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CheckCommand extends Command implements TabExecutor {

    private final Punish plugin;

    public CheckCommand(Punish plugin) {
        super("check", "punishments.check");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /check <ban-id|player> [forgive]");
            return;
        }

        String query = args[0].replace("#", "");

        // Check if it's a ban ID
        Punishment punishment = plugin.getDataManager().getPunishmentById(query);
        if (punishment != null) {
            displayPunishment(sender, punishment);

            // If sender is player and wants to forgive
            if (args.length > 1 && args[1].equalsIgnoreCase("forgive")) {
                if (sender.hasPermission("punishments.forgive")) {
                    plugin.getPunishmentManager().forgivePunishment(query);
                    sender.sendMessage(ChatColor.GREEN + "Forgiven punishment #" + query);
                } else {
                    sender.sendMessage(ChatColor.RED + "No permission to forgive!");
                }
            }
            return;
        }

        // Check if it's a player
        ProxiedPlayer target = plugin.getProxy().getPlayer(query);
        if (target != null) {
            displayPlayerHistory(sender, target.getUniqueId(), target.getName());
        } else {
            sender.sendMessage(ChatColor.RED + "Punishment or player not found!");
        }
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
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            // Suggest online players
            String search = args[0].toLowerCase();
            suggestions = plugin.getProxy().getPlayers().stream()
                    .map(ProxiedPlayer::getName)
                    .filter(name -> name.toLowerCase().startsWith(search))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            // Suggest "forgive"
            if ("forgive".startsWith(args[1].toLowerCase())) {
                suggestions.add("forgive");
            }
        }

        return suggestions;
    }
}