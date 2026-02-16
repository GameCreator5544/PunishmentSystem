package net.hyperlowmc.plugins.punishPaper.commands;

import net.hyperlowmc.plugins.punishPaper.PunishPaper;
import net.hyperlowmc.plugins.punishPaper.models.Punishment;
import net.hyperlowmc.plugins.punishPaper.models.PunishmentType;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class PunishCommand implements CommandExecutor, TabCompleter {

    private final PunishPaper plugin;

    public PunishCommand(PunishPaper plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /punish <player> <category>");
            return true;
        }

        String targetName = args[0];
        String category = args[1].toLowerCase();

        // Check permission for specific category
        if (sender instanceof Player && !sender.hasPermission("punishments.punish." + category)) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to punish for this category!");
            return true;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        if (plugin.getConfigManager().getPunishmentConfig(category) == null) {
            sender.sendMessage(ChatColor.RED + "Unknown punishment category! Available: " +
                    String.join(", ", plugin.getConfigManager().getCategories()));
            return true;
        }

        String senderName = sender instanceof Player ? ((Player) sender).getName() : "Console";
        String ip = target.getAddress().getAddress().getHostAddress();

        Punishment punishment = plugin.getPunishmentManager().punishPlayer(
                target.getUniqueId(),
                target.getName(),
                ip,
                category,
                senderName
        );

        if (punishment == null) {
            sender.sendMessage(ChatColor.RED + "Failed to create punishment!");
            return true;
        }

        // Execute punishment
        String message;
        switch (punishment.getType()) {
            case BAN:
                message = plugin.getPunishmentManager().formatMessage(
                        punishment,
                        plugin.getConfigManager().getBanMessageTemplate()
                );

                // Add to Bukkit ban list
                if (punishment.getDuration() == -1) {
                    Bukkit.getBanList(BanList.Type.IP).addBan(ip, message, null, senderName);
                } else {
                    Date expiry = new Date(System.currentTimeMillis() + punishment.getRemainingTime());
                    Bukkit.getBanList(BanList.Type.IP).addBan(ip, message, expiry, senderName);
                }

                target.kickPlayer(message);
                sender.sendMessage(ChatColor.GREEN + "Banned " + target.getName() + " for " + category +
                        " (ID: #" + punishment.getBanId() + ")");
                break;

            case KICK:
                message = plugin.getPunishmentManager().formatMessage(
                        punishment,
                        plugin.getConfigManager().getKickMessageTemplate()
                );
                target.kickPlayer(message);
                sender.sendMessage(ChatColor.GREEN + "Kicked " + target.getName() + " for " + category +
                        " (ID: #" + punishment.getBanId() + ")");
                break;

            case MUTE:
                sender.sendMessage(ChatColor.GREEN + "Muted " + target.getName() + " for " + category +
                        " (ID: #" + punishment.getBanId() + ")");
                message = plugin.getPunishmentManager().formatMessage(
                        punishment,
                        plugin.getConfigManager().getMuteMessageTemplate()
                );
                target.sendMessage(message);
                break;
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
        } else if (args.length == 2) {
            // Suggest categories
            String search = args[1].toLowerCase();
            suggestions = plugin.getConfigManager().getCategories().stream()
                    .filter(cat -> sender.hasPermission("punishments.punish." + cat))
                    .filter(cat -> cat.startsWith(search))
                    .collect(Collectors.toList());
        }

        return suggestions;
    }
}
