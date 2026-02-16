package net.hyperlowmc.plugins.punishPaper.managers;

import net.hyperlowmc.plugins.punishPaper.PunishPaper;
import net.hyperlowmc.plugins.punishPaper.models.PlayerData;
import net.hyperlowmc.plugins.punishPaper.models.Punishment;
import net.hyperlowmc.plugins.punishPaper.models.PunishmentType;
import net.hyperlowmc.plugins.punishPaper.utils.TimeUtil;
import org.bukkit.BanList;
import org.bukkit.Bukkit;

import java.security.SecureRandom;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PunishmentManager {

    private final PunishPaper plugin;
    private final SecureRandom random;

    public PunishmentManager(PunishPaper plugin) {
        this.plugin = plugin;
        this.random = new SecureRandom();
    }

    public String generateBanId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public Punishment punishPlayer(UUID uuid, String name, String ip, String category, String punishedBy) {
        ConfigManager.PunishmentConfig config = plugin.getConfigManager().getPunishmentConfig(category);
        if (config == null) return null;

        PlayerData playerData = plugin.getDataManager().getOrCreatePlayerData(uuid, name);

        int offenseCount = getIpOffenseCount(ip, category) + 1;

        ConfigManager.PunishmentStep step = null;
        for (ConfigManager.PunishmentStep s : config.getSteps()) {
            if (s.getOffense() == offenseCount) {
                step = s;
                break;
            }
        }

        if (step == null && !config.getSteps().isEmpty()) {
            step = config.getSteps().get(config.getSteps().size() - 1);
        }

        if (step == null) return null;

        String banId = generateBanId();
        Punishment punishment = new Punishment(
                banId, uuid, name, ip,
                config.getType(),
                config.getDisplayReason(),
                category,
                step.getDuration(),
                punishedBy
        );

        applyPunishmentToIp(ip, punishment, category);

        return punishment;
    }

    public Punishment manualPunish(UUID uuid, String name, String ip, PunishmentType type,
                                   String reason, long duration, String punishedBy) {
        String banId = generateBanId();
        Punishment punishment = new Punishment(
                banId, uuid, name, ip,
                type, reason, null,
                duration, punishedBy
        );

        if (type == PunishmentType.BAN) {
            applyPunishmentToIp(ip, punishment, null);
        } else {
            plugin.getDataManager().addPunishment(punishment);
        }

        return punishment;
    }

    private void applyPunishmentToIp(String ip, Punishment mainPunishment, String category) {
        Set<UUID> players = plugin.getDataManager().getPlayersByIp(ip);

        for (UUID playerUuid : players) {
            PlayerData playerData = plugin.getDataManager().getPlayerData(playerUuid);
            if (playerData != null) {
                if (category != null) {
                    playerData.incrementCategoryOffense(category);
                }
            }
        }

        PlayerData mainPlayerData = plugin.getDataManager().getOrCreatePlayerData(
                mainPunishment.getPlayerUuid(), mainPunishment.getPlayerName()
        );
        if (category != null) {
            mainPlayerData.incrementCategoryOffense(category);
        }

        plugin.getDataManager().addPunishment(mainPunishment);
    }

    private int getIpOffenseCount(String ip, String category) {
        Set<UUID> players = plugin.getDataManager().getPlayersByIp(ip);
        int maxCount = 0;

        for (UUID uuid : players) {
            PlayerData data = plugin.getDataManager().getPlayerData(uuid);
            if (data != null) {
                int count = data.getCategoryOffenseCount(category);
                maxCount = Math.max(maxCount, count);
            }
        }

        return maxCount;
    }

    public void unban(String banId) {
        Punishment punishment = plugin.getDataManager().getPunishmentById(banId);
        if (punishment != null) {
            punishment.setActive(false);

            // Remove from Bukkit ban list if it's a ban
            if (punishment.getType() == PunishmentType.BAN && punishment.getIpAddress() != null) {
                Bukkit.getBanList(BanList.Type.IP).pardon(punishment.getIpAddress());
            }

            plugin.getDataManager().saveAll();
        }
    }

    public void forgivePunishment(String banId) {
        Punishment punishment = plugin.getDataManager().getPunishmentById(banId);
        if (punishment == null) return;

        punishment.setActive(false);

        // Remove from Bukkit ban list if it's a ban
        if (punishment.getType() == PunishmentType.BAN && punishment.getIpAddress() != null) {
            Bukkit.getBanList(BanList.Type.IP).pardon(punishment.getIpAddress());
        }

        // Decrement offense count if it was a category-based punishment
        if (punishment.getCategory() != null) {
            String ip = punishment.getIpAddress();
            Set<UUID> players = plugin.getDataManager().getPlayersByIp(ip);

            for (UUID uuid : players) {
                PlayerData data = plugin.getDataManager().getPlayerData(uuid);
                if (data != null) {
                    data.decrementCategoryOffense(punishment.getCategory());
                }
            }
        }

        plugin.getDataManager().saveAll();
    }

    public String formatMessage(Punishment punishment, String template) {
        String message = template
                .replace("{reason}", punishment.getReason())
                .replace("{banId}", punishment.getBanId())
                .replace("{expiry}", punishment.getDuration() == -1 ? "Never" : TimeUtil.formatDuration(punishment.getRemainingTime()))
                .replace("\\n", "\n");

        return message;
    }
}