package net.hyperlowmc.plugins.punish.models;

import java.util.*;

public class PlayerData {

    private UUID uuid;
    private String lastKnownName;
    private Set<String> ipAddresses;
    private Map<String, Integer> categoryOffenses; // category -> count
    private List<Punishment> punishments;

    public PlayerData(UUID uuid, String lastKnownName) {
        this.uuid = uuid;
        this.lastKnownName = lastKnownName;
        this.ipAddresses = new HashSet<>();
        this.categoryOffenses = new HashMap<>();
        this.punishments = new ArrayList<>();
    }

    public void addIpAddress(String ip) {
        ipAddresses.add(ip);
    }

    public void incrementCategoryOffense(String category) {
        categoryOffenses.put(category, categoryOffenses.getOrDefault(category, 0) + 1);
    }

    public void decrementCategoryOffense(String category) {
        int count = categoryOffenses.getOrDefault(category, 0);
        if (count > 0) {
            categoryOffenses.put(category, count - 1);
        }
    }

    public int getCategoryOffenseCount(String category) {
        return categoryOffenses.getOrDefault(category, 0);
    }

    public void addPunishment(Punishment punishment) {
        punishments.add(punishment);
    }

    // Getters
    public UUID getUuid() { return uuid; }
    public String getLastKnownName() { return lastKnownName; }
    public void setLastKnownName(String name) { this.lastKnownName = name; }
    public Set<String> getIpAddresses() { return ipAddresses; }
    public Map<String, Integer> getCategoryOffenses() { return categoryOffenses; }
    public List<Punishment> getPunishments() { return punishments; }
}
