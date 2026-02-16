package net.hyperlowmc.plugins.punishPaper;

import net.hyperlowmc.plugins.punishPaper.commands.*;
import net.hyperlowmc.plugins.punishPaper.listeners.ChatListener;
import net.hyperlowmc.plugins.punishPaper.listeners.LoginListener;
import net.hyperlowmc.plugins.punishPaper.managers.ConfigManager;
import net.hyperlowmc.plugins.punishPaper.managers.DataManager;
import net.hyperlowmc.plugins.punishPaper.managers.PunishmentManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PunishPaper extends JavaPlugin {

    private static PunishPaper instance;
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
        PunishCommand punishCommand = new PunishCommand(this);
        getCommand("punish").setExecutor(punishCommand);
        getCommand("punish").setTabCompleter(punishCommand);

        UnbanCommand unbanCommand = new UnbanCommand(this);
        getCommand("unban").setExecutor(unbanCommand);
        getCommand("unban").setTabCompleter(unbanCommand);

        CheckCommand checkCommand = new CheckCommand(this);
        getCommand("check").setExecutor(checkCommand);
        getCommand("check").setTabCompleter(checkCommand);

        ManualPunishCommand manualCommand = new ManualPunishCommand(this);
        getCommand("manualpunish").setExecutor(manualCommand);
        getCommand("manualpunish").setTabCompleter(manualCommand);

        CheckIPCommand checkIPCommand = new CheckIPCommand(this);
        getCommand("checkip").setExecutor(checkIPCommand);
        getCommand("checkip").setTabCompleter(checkIPCommand);

        // Register listeners
        getServer().getPluginManager().registerEvents(new LoginListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);

        getLogger().info("HyperLowMC Punishment System (Paper) enabled!");
    }

    @Override
    public void onDisable() {
        // Save all data
        if (dataManager != null) {
            dataManager.saveAll();
        }
        getLogger().info("HyperLowMC Punishment System (Paper) disabled!");
    }

    public static PunishPaper getInstance() {
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
