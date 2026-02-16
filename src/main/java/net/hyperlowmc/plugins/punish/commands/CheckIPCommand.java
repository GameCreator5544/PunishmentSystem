package net.hyperlowmc.plugins.punish.commands;

import net.hyperlowmc.plugins.punish.Punish;
import net.hyperlowmc.plugins.punish.models.PlayerData;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class CheckIPCommand extends Command implements TabExecutor {

    private final Punish plugin;

    public CheckIPCommand(Punish plugin) {
        super("checkip", "punishments.checkip");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /checkip <ip|player>");
            return;
        }

        String query = args[0];
        String ip;

        // Check if it's a player name
        ProxiedPlayer target = plugin.getProxy().getPlayer(query);
        if (target != null) {
            ip = target.getAddress().getAddress().getHostAddress();
        } else if (query.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
            // It's an IP address
            ip = query;
        } else {
            sender.sendMessage(ChatColor.RED + "Invalid player or IP address!");
            return;
        }

        Set<UUID> players = plugin.getDataManager().getPlayersByIp(ip);

        if (players.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No players found for this IP!");
            return;
        }

        sender.sendMessage(ChatColor.GOLD + "=== Players with IP " + ip + " ===");
        sender.sendMessage(ChatColor.YELLOW + "Total accounts: " + ChatColor.WHITE + players.size());

        for (UUID uuid : players) {
            PlayerData data = plugin.getDataManager().getPlayerData(uuid);
            if (data != null) {
                sender.sendMessage(ChatColor.WHITE + "- " + data.getLastKnownName() +
                        ChatColor.GRAY + " (" + uuid + ")");
            }
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
        }

        return suggestions;
    }
}
