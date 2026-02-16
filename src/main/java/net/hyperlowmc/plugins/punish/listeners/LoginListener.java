package net.hyperlowmc.plugins.punish.listeners;

import net.hyperlowmc.plugins.punish.Punish;
import net.hyperlowmc.plugins.punish.models.Punishment;
import net.hyperlowmc.plugins.punish.models.PunishmentType;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.List;

public class LoginListener implements Listener {

    private final Punish plugin;

    public LoginListener(Punish plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLogin(LoginEvent event) {
        String ip = event.getConnection().getAddress().getAddress().getHostAddress();

        // Update player data
        plugin.getDataManager().updatePlayerIp(
                event.getConnection().getUniqueId(),
                event.getConnection().getName(),
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
                event.setCancelled(true);
                event.setCancelReason(message);
                return;
            }
        }
    }
}
