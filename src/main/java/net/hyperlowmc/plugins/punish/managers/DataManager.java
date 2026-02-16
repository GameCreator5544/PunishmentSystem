package net.hyperlowmc.plugins.punish.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.hyperlowmc.plugins.punish.Punish;
import net.hyperlowmc.plugins.punish.models.PlayerData;
import net.hyperlowmc.plugins.punish.models.Punishment;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataManager {

    private final Punish plugin;
    private final File dataFolder;
    private final File punishmentsFile;
    private final File playersFile;
    private final File ipMapFile;
    private final Gson gson;

    private Map<String, Punishment> punishmentsById; // banId -> Punishment
    private Map<UUID, PlayerData> playerDataMap; // UUID -> PlayerData
    private Map<String, Set<UUID>> ipToPlayers; // IP -> Set of UUIDs

    public DataManager(Punish plugin) {
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
        loadPunishments();
        loadPlayers();
        loadIpMap();
    }

    private void loadPunishments() {
        if (!punishmentsFile.exists()) return;

        try (Reader reader = new FileReader(punishmentsFile)) {
            Type type = new TypeToken<Map<String, Punishment>>(){}.getType();
            Map<String, Punishment> loaded = gson.fromJson(reader, type);
            if (loaded != null) {
                punishmentsById.putAll(loaded);
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load punishments: " + e.getMessage());
        }
    }

    private void loadPlayers() {
        if (!playersFile.exists()) return;

        try (Reader reader = new FileReader(playersFile)) {
            Type type = new TypeToken<Map<UUID, PlayerData>>(){}.getType();
            Map<UUID, PlayerData> loaded = gson.fromJson(reader, type);
            if (loaded != null) {
                playerDataMap.putAll(loaded);
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load players: " + e.getMessage());
        }
    }

    private void loadIpMap() {
        if (!ipMapFile.exists()) return;

        try (Reader reader = new FileReader(ipMapFile)) {
            Type type = new TypeToken<Map<String, Set<UUID>>>(){}.getType();
            Map<String, Set<UUID>> loaded = gson.fromJson(reader, type);
            if (loaded != null) {
                ipToPlayers.putAll(loaded);
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load IP map: " + e.getMessage());
        }
    }

    public void saveAll() {
        savePunishments();
        savePlayers();
        saveIpMap();
    }

    private void savePunishments() {
        try (Writer writer = new FileWriter(punishmentsFile)) {
            gson.toJson(punishmentsById, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save punishments: " + e.getMessage());
        }
    }

    private void savePlayers() {
        try (Writer writer = new FileWriter(playersFile)) {
            gson.toJson(playerDataMap, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save players: " + e.getMessage());
        }
    }

    private void saveIpMap() {
        try (Writer writer = new FileWriter(ipMapFile)) {
            gson.toJson(ipToPlayers, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save IP map: " + e.getMessage());
        }
    }

    // Data access methods
    public void addPunishment(Punishment punishment) {
        punishmentsById.put(punishment.getBanId(), punishment);

        PlayerData playerData = getOrCreatePlayerData(punishment.getPlayerUuid(), punishment.getPlayerName());
        playerData.addPunishment(punishment);
        playerData.addIpAddress(punishment.getIpAddress());

        // Track IP
        ipToPlayers.computeIfAbsent(punishment.getIpAddress(), k -> new HashSet<>()).add(punishment.getPlayerUuid());

        saveAll();
    }

    public Punishment getPunishmentById(String banId) {
        return punishmentsById.get(banId);
    }

    public PlayerData getOrCreatePlayerData(UUID uuid, String name) {
        return playerDataMap.computeIfAbsent(uuid, k -> new PlayerData(uuid, name));
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.get(uuid);
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
    }
}
