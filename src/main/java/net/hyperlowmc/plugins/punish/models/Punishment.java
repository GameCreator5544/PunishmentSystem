package net.hyperlowmc.plugins.punish.models;

import java.util.UUID;

public class Punishment {

    private String banId;
    private UUID playerUuid;
    private String playerName;
    private String ipAddress;
    private PunishmentType type;
    private String reason;
    private String category;
    private long timestamp;
    private long duration; // -1 for permanent
    private boolean active;
    private String punishedBy;

    public Punishment(String banId, UUID playerUuid, String playerName, String ipAddress,
                      PunishmentType type, String reason, String category, long duration, String punishedBy) {
        this.banId = banId;
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.ipAddress = ipAddress;
        this.type = type;
        this.reason = reason;
        this.category = category;
        this.timestamp = System.currentTimeMillis();
        this.duration = duration;
        this.active = true;
        this.punishedBy = punishedBy;
    }

    public boolean isExpired() {
        if (duration == -1) return false;
        return System.currentTimeMillis() > (timestamp + duration);
    }

    public long getRemainingTime() {
        if (duration == -1) return -1;
        long remaining = (timestamp + duration) - System.currentTimeMillis();
        return remaining > 0 ? remaining : 0;
    }

    // Getters and setters
    public String getBanId() { return banId; }
    public UUID getPlayerUuid() { return playerUuid; }
    public String getPlayerName() { return playerName; }
    public String getIpAddress() { return ipAddress; }
    public PunishmentType getType() { return type; }
    public String getReason() { return reason; }
    public String getCategory() { return category; }
    public long getTimestamp() { return timestamp; }
    public long getDuration() { return duration; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getPunishedBy() { return punishedBy; }
}
