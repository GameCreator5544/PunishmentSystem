package net.hyperlowmc.plugins.punishPaper.listeners;

import net.hyperlowmc.plugins.punishPaper.PunishPaper;
import net.hyperlowmc.plugins.punishPaper.models.Punishment;
import net.hyperlowmc.plugins.punishPaper.models.PunishmentType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.List;

public class LoginListener implements Listener {

    private final PunishPaper plugin;

    public LoginListener(PunishPaper plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogin(AsyncPlayerPreLoginEvent event) {
        String ip = event.getAddress().getHostAddress();

        // Update player data
        plugin.getDataManager().updatePlayerIp(
                event.getUniqueId(),
                event.getName(),
                ip
        );

        // Check for active bans (including IP bans)
        List<Punishment> activeBans = plugin.getDataManager().getActivePunishmentsByIp(ip);

        for (Punishment punishment : activeBans) {
            if (punishment.getType() == PunishmentType.BAN && !punishment.isExpired()) {
                String message = plugin.getPunishmentManager().formatMessage(
                        punishment,
                        plugin.getConfigManager().getBanMessageTemplate()
                );
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, message);
                return;
            }
        }
    }
}
