package net.hyperlowmc.plugins.punishPaper.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.hyperlowmc.plugins.punishPaper.PunishPaper;
import net.hyperlowmc.plugins.punishPaper.models.PunishmentType;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class ConfigManager {

    private final PunishPaper plugin;
    private final File configFile;
    private final Gson gson;
    private Map<String, PunishmentConfig> punishmentConfigs;
    private String banMessageTemplate;
    private String kickMessageTemplate;
    private String muteMessageTemplate;

    public ConfigManager(PunishPaper plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().create();

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        this.configFile = new File(plugin.getDataFolder(), "config.json");
        loadConfig();
    }

    private void loadConfig() {
        if (!configFile.exists()) {
            createDefaultConfig();
        }

        try (Reader reader = new FileReader(configFile)) {
            Type type = new TypeToken<ConfigData>(){}.getType();
            ConfigData data = gson.fromJson(reader, type);

            this.punishmentConfigs = data.punishments;
            this.banMessageTemplate = data.banMessage;
            this.kickMessageTemplate = data.kickMessage;
            this.muteMessageTemplate = data.muteMessage;
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load config: " + e.getMessage());
            createDefaultConfig();
        }
    }

    private void createDefaultConfig() {
        punishmentConfigs = new HashMap<>();

        punishmentConfigs.put("hacking", new PunishmentConfig(
                "hacking",
                "You have been caught hacking.",
                PunishmentType.BAN,
                Arrays.asList(
                        new PunishmentStep(1, 90 * 24 * 60 * 60 * 1000L),
                        new PunishmentStep(2, 120 * 24 * 60 * 60 * 1000L),
                        new PunishmentStep(3, -1L)
                )
        ));

        punishmentConfigs.put("toxicity", new PunishmentConfig(
                "toxicity",
                "Toxic behavior is not tolerated.",
                PunishmentType.MUTE,
                Arrays.asList(
                        new PunishmentStep(1, 24 * 60 * 60 * 1000L),
                        new PunishmentStep(2, 7 * 24 * 60 * 60 * 1000L),
                        new PunishmentStep(3, -1L)
                )
        ));

        punishmentConfigs.put("spam", new PunishmentConfig(
                "spam",
                "Please do not spam.",
                PunishmentType.KICK,
                Arrays.asList(
                        new PunishmentStep(1, 0L),
                        new PunishmentStep(2, 60 * 60 * 1000L),
                        new PunishmentStep(3, 24 * 60 * 60 * 1000L)
                )
        ));

        banMessageTemplate = "§cYou are banned!\\n\\n§7Reason: §f{reason}\\n§7ID: §f#{banId}\\n§7Expires: §f{expiry}";
        kickMessageTemplate = "§cYou have been kicked!\\n\\n§7Reason: §f{reason}\\n§7ID: §f#{banId}";
        muteMessageTemplate = "§cYou are muted!\\n\\n§7Reason: §f{reason}\\n§7ID: §f#{banId}\\n§7Expires: §f{expiry}";

        saveConfig();
    }

    public void saveConfig() {
        try (Writer writer = new FileWriter(configFile)) {
            ConfigData data = new ConfigData();
            data.punishments = punishmentConfigs;
            data.banMessage = banMessageTemplate;
            data.kickMessage = kickMessageTemplate;
            data.muteMessage = muteMessageTemplate;

            gson.toJson(data, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save config: " + e.getMessage());
        }
    }

    public PunishmentConfig getPunishmentConfig(String category) {
        return punishmentConfigs.get(category.toLowerCase());
    }

    public Set<String> getCategories() {
        return punishmentConfigs.keySet();
    }

    public String getBanMessageTemplate() { return banMessageTemplate; }
    public String getKickMessageTemplate() { return kickMessageTemplate; }
    public String getMuteMessageTemplate() { return muteMessageTemplate; }

    private static class ConfigData {
        Map<String, PunishmentConfig> punishments;
        String banMessage;
        String kickMessage;
        String muteMessage;
    }

    public static class PunishmentConfig {
        String category;
        String displayReason;
        PunishmentType type;
        List<PunishmentStep> steps;

        public PunishmentConfig(String category, String displayReason, PunishmentType type, List<PunishmentStep> steps) {
            this.category = category;
            this.displayReason = displayReason;
            this.type = type;
            this.steps = steps;
        }

        public String getCategory() { return category; }
        public String getDisplayReason() { return displayReason; }
        public PunishmentType getType() { return type; }
        public List<PunishmentStep> getSteps() { return steps; }
    }

    public static class PunishmentStep {
        int offense;
        long duration;

        public PunishmentStep(int offense, long duration) {
            this.offense = offense;
            this.duration = duration;
        }

        public int getOffense() { return offense; }
        public long getDuration() { return duration; }
    }
}
