package net.hyperlowmc.plugins.punish.commands;

import net.hyperlowmc.plugins.punish.Punish;
import net.hyperlowmc.plugins.punish.models.Punishment;
import net.hyperlowmc.plugins.punish.models.PunishmentType;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PunishCommand extends Command implements TabExecutor {

    private final Punish plugin;

    public PunishCommand(Punish plugin) {
        super("punish", "punishments.*");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /punish <player> <category>");
            return;
        }

        String targetName = args[0];
        String category = args[1].toLowerCase();

        // Check permission for specific category
        if (sender instanceof ProxiedPlayer && !sender.hasPermission("punishments.punish." + category)) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to punish for this category!");
            return;
        }

        ProxiedPlayer target = plugin.getProxy().getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }

        if (plugin.getConfigManager().getPunishmentConfig(category) == null) {
            sender.sendMessage(ChatColor.RED + "Unknown punishment category! Available: " +
                    String.join(", ", plugin.getConfigManager().getCategories()));
            return;
        }

        String senderName = sender instanceof ProxiedPlayer ? ((ProxiedPlayer) sender).getName() : "Console";
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
            return;
        }

        // Execute punishment
        String message;
        switch (punishment.getType()) {
            case BAN:
                message = plugin.getPunishmentManager().formatMessage(
                        punishment,
                        plugin.getConfigManager().getBanMessageTemplate()
                );
                target.disconnect(message);
                sender.sendMessage(ChatColor.GREEN + "Banned " + target.getName() + " for " + category +
                        " (ID: #" + punishment.getBanId() + ")");
                break;
            case KICK:
                message = plugin.getPunishmentManager().formatMessage(
                        punishment,
                        plugin.getConfigManager().getKickMessageTemplate()
                );
                target.disconnect(message);
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