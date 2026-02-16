package net.hyperlowmc.plugins.punishPaper.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.hyperlowmc.plugins.punishPaper.PunishPaper;
import net.hyperlowmc.plugins.punishPaper.models.Punishment;
import net.hyperlowmc.plugins.punishPaper.models.PunishmentType;
import net.hyperlowmc.plugins.punishPaper.utils.TimeUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.List;

public class ChatListener implements Listener {

    private final PunishPaper plugin;

    public ChatListener(PunishPaper plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

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
