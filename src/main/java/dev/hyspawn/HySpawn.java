package dev.hyspawn;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
import dev.hyspawn.command.*;
import dev.hyspawn.config.ConfigManager;
import dev.hyspawn.listener.PlayerDeathListener;
import dev.hyspawn.listener.PlayerJoinListener;
import dev.hyspawn.listener.PlayerMoveListener;
import dev.hyspawn.listener.VoidFallListener;
import dev.hyspawn.manager.SpawnDataManager;
import dev.hyspawn.manager.TeleportManager;
import dev.hyspawn.util.MessageUtil;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("UnstableApiUsage")
public final class HySpawn extends JavaPlugin {

    private ConfigManager configManager;
    private SpawnDataManager spawnDataManager;
    private TeleportManager teleportManager;
    private MessageUtil messageUtil;
    private TaskScheduler scheduler;

    @Override
    public void onEnable() {
        this.scheduler = UniversalScheduler.getScheduler(this);
        this.configManager = new ConfigManager(this);
        this.spawnDataManager = new SpawnDataManager(this);
        this.messageUtil = new MessageUtil(configManager);
        this.teleportManager = new TeleportManager(this);

        registerCommands();
        registerListeners();
        initMetrics();
        printEnableBanner();
    }

    private void initMetrics() {
        // Replace 00000 with your bStats plugin ID from https://bstats.org
        new Metrics(this, 30946);
    }

    @Override
    public void onDisable() {
        if (teleportManager != null) {
            teleportManager.cancelAll();
        }
        printDisableBanner();
    }

    private void printEnableBanner() {
        String version = getPluginMeta().getVersion();
        var logger = getComponentLogger();
        logger.info(net.kyori.adventure.text.Component.empty());
        logger.info(net.kyori.adventure.text.Component.text("  ┌──────────────────────────────────────────┐"));
        logger.info(net.kyori.adventure.text.Component.text("  │                                          │"));
        logger.info(net.kyori.adventure.text.Component.text("  │         HySpawn — Enabled!               │"));
        logger.info(net.kyori.adventure.text.Component.text("  │                                          │"));
        logger.info(net.kyori.adventure.text.Component.text("  │  Developer: Szabolcs                     │"));
        logger.info(net.kyori.adventure.text.Component.text("  │  Discord:   szabolc.s                    │"));
        logger.info(net.kyori.adventure.text.Component.text("  │  Website:   https://yoursit.ee/szabolcs  │"));
        logger.info(net.kyori.adventure.text.Component.text("  │  Version:   " + version + padRight("", 29 - version.length()) + "│"));
        logger.info(net.kyori.adventure.text.Component.text("  │                                          │"));
        logger.info(net.kyori.adventure.text.Component.text("  └──────────────────────────────────────────┘"));
        logger.info(net.kyori.adventure.text.Component.empty());
    }

    private void printDisableBanner() {
        var logger = getComponentLogger();
        logger.info(net.kyori.adventure.text.Component.empty());
        logger.info(net.kyori.adventure.text.Component.text("  HySpawn — Disabled."));
        logger.info(net.kyori.adventure.text.Component.empty());
    }

    private static String padRight(String s, int n) {
        if (n <= 0) return s;
        return s + " ".repeat(n);
    }

    private void registerCommands() {
        registerCommand("setspawn", new SetSpawnCommand(this));
        registerCommand("spawn", new SpawnCommand(this));
        registerCommand("setvoidspawn", new SetVoidSpawnCommand(this));
        registerCommand("unsetspawn", new UnsetSpawnCommand(this));
        registerCommand("unsetvoidspawn", new UnsetVoidSpawnCommand(this));
        registerCommand("hyspawn", new HySpawnCommand(this));
    }

    private void registerListeners() {
        var pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerJoinListener(this), this);
        pm.registerEvents(new PlayerDeathListener(this), this);
        pm.registerEvents(new PlayerMoveListener(this, teleportManager), this);
        pm.registerEvents(new VoidFallListener(this), this);
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public SpawnDataManager getSpawnDataManager() {
        return spawnDataManager;
    }

    public TeleportManager getTeleportManager() {
        return teleportManager;
    }

    public MessageUtil getMessageUtil() {
        return messageUtil;
    }

    public TaskScheduler getScheduler() {
        return scheduler;
    }

    public void reload() {
        configManager.reload();
        spawnDataManager.reload();
    }
}
