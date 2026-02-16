package net.hyperlowmc.plugins.punishPaper.commands;

import net.hyperlowmc.plugins.punishPaper.PunishPaper;
import net.hyperlowmc.plugins.punishPaper.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class CheckIPCommand implements CommandExecutor, TabCompleter {

    private final PunishPaper plugin;

    public CheckIPCommand(PunishPaper plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /checkip <ip|player>");
            return true;
        }

        String query = args[0];
        String ip;

        // Check if it's a player name
        Player target = Bukkit.getPlayer(query);
        if (target != null) {
            ip = target.getAddress().getAddress().getHostAddress();
        } else if (query.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
            // It's an IP address
            ip = query;
        } else {
            sender.sendMessage(ChatColor.RED + "Invalid player or IP address!");
            return true;
        }

        Set<UUID> players = plugin.getDataManager().getPlayersByIp(ip);

        if (players.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No players found for this IP!");
            return true;
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

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            // Suggest online players
            String search = args[0].toLowerCase();
            suggestions = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(search))
                    .collect(Collectors.toList());
        }

        return suggestions;
    }
}
