package net.hyperlowmc.plugins.punishPaper.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.hyperlowmc.plugins.punishPaper.PunishPaper;
import net.hyperlowmc.plugins.punishPaper.models.PlayerData;
import net.hyperlowmc.plugins.punishPaper.models.Punishment;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataManager {

    private final PunishPaper plugin;
    private final File dataFolder;
    private final File punishmentsFile;
    private final File playersFile;
    private final File ipMapFile;
    private final Gson gson;

    private Map<String, Punishment> punishmentsById;
    private Map<UUID, PlayerData> playerDataMap;
    private Map<String, Set<UUID>> ipToPlayers;

    public DataManager(PunishPaper plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().create();

        this.dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        this.punishmentsFile = new File(dataFolder, "punishments.json");
        this.playersFile = new File(dataFolder, "players.json");
        this.ipMapFile = new File(dataFolder, "ip-map.json");

        this.punishmentsById = new ConcurrentHashMap<>();
        this.playerDataMap = new ConcurrentHashMap<>();
        this.ipToPlayers = new ConcurrentHashMap<>();

        loadData();
    }

    private void loadData() {
        loadPlayers(); // Load players first
        loadPunishments(); // Then load punishments (will rebuild index)
        loadIpMap();
    }

    private void loadPunishments() {
        // Clear the map first
        punishmentsById.clear();

        // Rebuild the punishments index from player data
        for (PlayerData playerData : playerDataMap.values()) {
            for (Punishment punishment : playerData.getPunishments()) {
                punishmentsById.put(punishment.getBanId(), punishment);
            }
        }

        plugin.getLogger().info("Loaded " + punishmentsById.size() + " punishments from player data");
    }

    private void loadPlayers() {
        if (!playersFile.exists()) return;

        try (Reader reader = new FileReader(playersFile)) {
            Type type = new TypeToken<Map<UUID, PlayerData>>(){}.getType();
            Map<UUID, PlayerData> loaded = gson.fromJson(reader, type);
            if (loaded != null) {
                playerDataMap.putAll(loaded);
                plugin.getLogger().info("Loaded " + playerDataMap.size() + " players");
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load players: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadIpMap() {
        if (!ipMapFile.exists()) return;

        try (Reader reader = new FileReader(ipMapFile)) {
            Type type = new TypeToken<Map<String, Set<UUID>>>(){}.getType();
            Map<String, Set<UUID>> loaded = gson.fromJson(reader, type);
            if (loaded != null) {
                ipToPlayers.putAll(loaded);
                plugin.getLogger().info("Loaded IP mappings for " + ipToPlayers.size() + " IPs");
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load IP map: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void saveAll() {
        savePlayers();
        saveIpMap();
        plugin.getLogger().info("Saved all punishment data");
    }

    private void savePlayers() {
        try (Writer writer = new FileWriter(playersFile)) {
            gson.toJson(playerDataMap, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save players: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveIpMap() {
        try (Writer writer = new FileWriter(ipMapFile)) {
            gson.toJson(ipToPlayers, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save IP map: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void addPunishment(Punishment punishment) {
        // Add to the ID index
        punishmentsById.put(punishment.getBanId(), punishment);

        // Add to player data
        PlayerData playerData = getOrCreatePlayerData(punishment.getPlayerUuid(), punishment.getPlayerName());
        playerData.addPunishment(punishment);
        playerData.addIpAddress(punishment.getIpAddress());

        // Track IP
        ipToPlayers.computeIfAbsent(punishment.getIpAddress(), k -> new HashSet<>()).add(punishment.getPlayerUuid());

        // Save immediately
        saveAll();

        plugin.getLogger().info("Added punishment #" + punishment.getBanId() + " for " + punishment.getPlayerName());
    }

    public Punishment getPunishmentById(String banId) {
        // Try exact match first
        Punishment punishment = punishmentsById.get(banId);
        if (punishment != null) {
            return punishment;
        }

        // Try case-insensitive search
        for (Map.Entry<String, Punishment> entry : punishmentsById.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(banId)) {
                return entry.getValue();
            }
        }

        return null;
    }

    public PlayerData getOrCreatePlayerData(UUID uuid, String name) {
        PlayerData data = playerDataMap.computeIfAbsent(uuid, k -> new PlayerData(uuid, name));
        data.setLastKnownName(name); // Update name if it changed
        return data;
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.get(uuid);
    }

    public Collection<PlayerData> getAllPlayerData() {
        return playerDataMap.values();
    }

    public Set<UUID> getPlayersByIp(String ip) {
        return ipToPlayers.getOrDefault(ip, new HashSet<>());
    }

    public List<Punishment> getActivePunishments(UUID uuid) {
        PlayerData data = playerDataMap.get(uuid);
        if (data == null) return new ArrayList<>();

        List<Punishment> active = new ArrayList<>();
        for (Punishment p : data.getPunishments()) {
            if (p.isActive() && !p.isExpired()) {
                active.add(p);
            }
        }
        return active;
    }

    public List<Punishment> getActivePunishmentsByIp(String ip) {
        Set<UUID> players = getPlayersByIp(ip);
        List<Punishment> active = new ArrayList<>();

        for (UUID uuid : players) {
            active.addAll(getActivePunishments(uuid));
        }
        return active;
    }

    public void updatePlayerIp(UUID uuid, String name, String ip) {
        PlayerData data = getOrCreatePlayerData(uuid, name);
        data.setLastKnownName(name);
        data.addIpAddress(ip);
        ipToPlayers.computeIfAbsent(ip, k -> new HashSet<>()).add(uuid);
        saveAll(); // Save after updating
    }
}