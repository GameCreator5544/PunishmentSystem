package net.hyperlowmc.plugins.punish.commands;

import net.hyperlowmc.plugins.punish.Punish;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.List;

public class UnbanCommand extends Command implements TabExecutor {

    private final Punish plugin;

    public UnbanCommand(Punish plugin) {
        super("unban", "punishments.unban");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /unban <ban-id>");
            return;
        }

        String banId = args[0].replace("#", "");
        plugin.getPunishmentManager().unban(banId);
        sender.sendMessage(ChatColor.GREEN + "Unbanned punishment #" + banId);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        // No tab completion for ban IDs (they're random)
        return new ArrayList<>();
    }
}
