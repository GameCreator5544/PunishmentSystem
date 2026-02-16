package net.hyperlowmc.plugins.punishPaper.commands;

import net.hyperlowmc.plugins.punishPaper.PunishPaper;
import net.hyperlowmc.plugins.punishPaper.models.Punishment;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class UnbanCommand implements CommandExecutor, TabCompleter {

    private final PunishPaper plugin;

    public UnbanCommand(PunishPaper plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /unban <ban-id>");
            return true;
        }

        String banId = args[0].replace("#", "");
        Punishment punishment = plugin.getDataManager().getPunishmentById(banId);

        if (punishment != null && punishment.getIpAddress() != null) {
            // Remove from Bukkit ban list
            Bukkit.getBanList(BanList.Type.IP).pardon(punishment.getIpAddress());
        }

        plugin.getPunishmentManager().unban(banId);
        sender.sendMessage(ChatColor.GREEN + "Unbanned punishment #" + banId);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
