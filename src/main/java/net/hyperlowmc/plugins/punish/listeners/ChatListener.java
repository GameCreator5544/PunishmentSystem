package net.hyperlowmc.plugins.punish.listeners;

import net.hyperlowmc.plugins.punish.Punish;
import net.hyperlowmc.plugins.punish.models.Punishment;
import net.hyperlowmc.plugins.punish.models.PunishmentType;
import net.hyperlowmc.plugins.punish.utils.TimeUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.List;

public class ChatListener implements Listener {

    private final Punish plugin;

    public ChatListener(Punish plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(ChatEvent event) {
        // Only check player chat messages, not commands
        if (event.isCommand() || event.isCancelled()) {
            return;
        }

        if (!(event.getSender() instanceof ProxiedPlayer)) {
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) event.getSender();

        // Check for active mutes
        List<Punishment> activePunishments = plugin.getDataManager().getActivePunishments(player.getUniqueId());

        for (Punishment punishment : activePunishments) {
            if (punishment.getType() == PunishmentType.MUTE && !punishment.isExpired()) {
                event.setCancelled(true);

                String timeLeft = punishment.getDuration() == -1
                        ? "permanently"
                        : "for " + TimeUtil.formatDuration(punishment.getRemainingTime());

                player.sendMessage(ChatColor.RED + "You are muted " + timeLeft + "!");
                player.sendMessage(ChatColor.GRAY + "Reason: " + ChatColor.WHITE + punishment.getReason());
                player.sendMessage(ChatColor.GRAY + "Mute ID: " + ChatColor.WHITE + "#" + punishment.getBanId());
                return;
            }
        }
    }
}
