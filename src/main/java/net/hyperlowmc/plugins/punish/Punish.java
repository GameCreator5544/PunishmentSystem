package net.hyperlowmc.plugins.punish;

import net.hyperlowmc.plugins.punish.commands.*;
import net.hyperlowmc.plugins.punish.listeners.ChatListener;
import net.hyperlowmc.plugins.punish.listeners.LoginListener;
import net.hyperlowmc.plugins.punish.managers.ConfigManager;
import net.hyperlowmc.plugins.punish.managers.DataManager;
import net.hyperlowmc.plugins.punish.managers.PunishmentManager;
import net.md_5.bungee.api.plugin.Plugin;

public class Punish extends Plugin {

    private static Punish instance;
    private ConfigManager configManager;
    private DataManager dataManager;
    private PunishmentManager punishmentManager;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.dataManager = new DataManager(this);
        this.punishmentManager = new PunishmentManager(this);

        // Register commands
        getProxy().getPluginManager().registerCommand(this, new PunishCommand(this));
        getProxy().getPluginManager().registerCommand(this, new UnbanCommand(this));
        getProxy().getPluginManager().registerCommand(this, new CheckCommand(this));
        getProxy().getPluginManager().registerCommand(this, new ManualPunishCommand(this));
        getProxy().getPluginManager().registerCommand(this, new CheckIPCommand(this));

        // Register listeners
        getProxy().getPluginManager().registerListener(this, new LoginListener(this));
        getProxy().getPluginManager().registerListener(this, new ChatListener(this));  // <-- NEW

        getLogger().info("HyperLowMC Punishment System enabled!");
    }

    @Override
    public void onDisable() {
        // Save all data
        dataManager.saveAll();
        getLogger().info("HyperLowMC Punishment System disabled!");
    }

    public static Punish getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public PunishmentManager getPunishmentManager() {
        return punishmentManager;
    }
}