package net.hyperlowmc.plugins.punishPaper.commands;

import net.hyperlowmc.plugins.punishPaper.PunishPaper;
import net.hyperlowmc.plugins.punishPaper.models.Punishment;
import net.hyperlowmc.plugins.punishPaper.models.PunishmentType;
import net.hyperlowmc.plugins.punishPaper.utils.TimeUtil;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ManualPunishCommand implements CommandExecutor, TabCompleter {

    private final PunishPaper plugin;

    public ManualPunishCommand(PunishPaper plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /manualpunish <ban|kick|mute> <player> <reason> [duration]");
            sender.sendMessage(ChatColor.GRAY + "Duration examples: 30m, 2h, 7d, perm");
            return true;
        }

        PunishmentType type;
        try {
            type = PunishmentType.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Invalid type! Use: ban, kick, or mute");
            return true;
        }

        String targetName = args[1];
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return true;
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
        long duration = -1;

        if (durationArgIndex != -1) {
            duration = TimeUtil.parseDuration(args[durationArgIndex]);
        } else if (type == PunishmentType.KICK) {
            duration = 0;
        }

        String senderName = sender instanceof Player ? ((Player) sender).getName() : "Console";
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

                if (punishment.getDuration() == -1) {
                    Bukkit.getBanList(BanList.Type.IP).addBan(ip, message, null, senderName);
                } else {
                    Date expiry = new Date(System.currentTimeMillis() + punishment.getRemainingTime());
                    Bukkit.getBanList(BanList.Type.IP).addBan(ip, message, expiry, senderName);
                }

                target.kickPlayer(message);
                sender.sendMessage(ChatColor.GREEN + "Manually banned " + target.getName() + " (ID: #" + punishment.getBanId() + ")");
                break;

            case KICK:
                message = plugin.getPunishmentManager().formatMessage(
                        punishment,
                        plugin.getConfigManager().getKickMessageTemplate()
                );
                target.kickPlayer(message);
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

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
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
            suggestions = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
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
