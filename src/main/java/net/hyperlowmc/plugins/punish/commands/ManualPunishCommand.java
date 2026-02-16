package net.hyperlowmc.plugins.punish.commands;

import net.hyperlowmc.plugins.punish.Punish;
import net.hyperlowmc.plugins.punish.models.Punishment;
import net.hyperlowmc.plugins.punish.models.PunishmentType;
import net.hyperlowmc.plugins.punish.utils.TimeUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ManualPunishCommand extends Command implements TabExecutor {

    private final Punish plugin;

    public ManualPunishCommand(Punish plugin) {
        super("manualpunish", "punishments.manual");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /manualpunish <ban|kick|mute> <player> <reason> [duration]");
            sender.sendMessage(ChatColor.GRAY + "Duration examples: 30m, 2h, 7d, perm");
            return;
        }

        PunishmentType type;
        try {
            type = PunishmentType.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Invalid type! Use: ban, kick, or mute");
            return;
        }

        String targetName = args[1];
        ProxiedPlayer target = plugin.getProxy().getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }

        StringBuilder reasonBuilder = new StringBuilder();
        int durationArgIndex = -1;

        for (int i = 2; i < args.length; i++) {
            if (i == args.length - 1 && (args[i].matches("\\d+[smhd]") || args[i].equalsIgnoreCase("perm"))) {
                durationArgIndex = i;
                break;
            }
            if (reasonBuilder.length() > 0) reasonBuilder.append(" ");
            reasonBuilder.append(args[i]);
        }

        String reason = reasonBuilder.toString();
        long duration = -1; // Default permanent

        if (durationArgIndex != -1) {
            duration = TimeUtil.parseDuration(args[durationArgIndex]);
        } else if (type == PunishmentType.KICK) {
            duration = 0; // Kick has no duration
        }

        String senderName = sender instanceof ProxiedPlayer ? ((ProxiedPlayer) sender).getName() : "Console";
        String ip = target.getAddress().getAddress().getHostAddress();

        Punishment punishment = plugin.getPunishmentManager().manualPunish(
                target.getUniqueId(),
                target.getName(),
                ip,
                type,
                reason,
                duration,
                senderName
        );

        // Execute punishment
        String message;
        switch (type) {
            case BAN:
                message = plugin.getPunishmentManager().formatMessage(
                        punishment,
                        plugin.getConfigManager().getBanMessageTemplate()
                );
                target.disconnect(message);
                sender.sendMessage(ChatColor.GREEN + "Manually banned " + target.getName() + " (ID: #" + punishment.getBanId() + ")");
                break;
            case KICK:
                message = plugin.getPunishmentManager().formatMessage(
                        punishment,
                        plugin.getConfigManager().getKickMessageTemplate()
                );
                target.disconnect(message);
                sender.sendMessage(ChatColor.GREEN + "Manually kicked " + target.getName() + " (ID: #" + punishment.getBanId() + ")");
                break;
            case MUTE:
                sender.sendMessage(ChatColor.GREEN + "Manually muted " + target.getName() + " (ID: #" + punishment.getBanId() + ")");
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
            // Suggest punishment types
            String search = args[0].toLowerCase();
            suggestions = Arrays.asList("ban", "kick", "mute").stream()
                    .filter(type -> type.startsWith(search))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            // Suggest online players
            String search = args[1].toLowerCase();
            suggestions = plugin.getProxy().getPlayers().stream()
                    .map(ProxiedPlayer::getName)
                    .filter(name -> name.toLowerCase().startsWith(search))
                    .collect(Collectors.toList());
        } else if (args.length >= 4) {
            // Suggest duration formats
            String search = args[args.length - 1].toLowerCase();
            List<String> durations = Arrays.asList("30m", "1h", "2h", "6h", "12h", "1d", "3d", "7d", "30d", "perm");
            suggestions = durations.stream()
                    .filter(dur -> dur.startsWith(search))
                    .collect(Collectors.toList());
        }

        return suggestions;
    }
}