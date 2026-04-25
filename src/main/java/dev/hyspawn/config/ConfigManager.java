package dev.hyspawn.config;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public int getCountdown() {
        return config.getInt("spawn.countdown", 5);
    }

    public boolean isCancelOnMove() {
        return config.getBoolean("spawn.cancel-on-move", true);
    }

    public int getCooldown() {
        return config.getInt("spawn.cooldown", 30);
    }

    public String getMessage(String key) {
        String prefix = config.getString("messages.prefix", "");
        String message = config.getString("messages." + key, "<red>Missing message: " + key);
        return message;
    }

    public String getPrefix() {
        return config.getString("messages.prefix", "");
    }

    public String getPrefixedMessage(String key) {
        return getPrefix() + getMessage(key);
    }

    /**
     * Play a configured sound for a player.
     * Format in config: "SOUND_NAME:VOLUME:PITCH" or "" to disable.
     */
    public void playSound(Player player, String key) {
        String raw = config.getString("sounds." + key, "");
        if (raw == null || raw.isEmpty()) return;

        String[] parts = raw.split(":");
        if (parts.length < 1) return;

        try {
            NamespacedKey soundKey = NamespacedKey.minecraft(parts[0].toLowerCase());
            Sound sound = Registry.SOUNDS.get(soundKey);
            if (sound == null) {
                plugin.getLogger().warning("Unknown sound in config (sounds." + key + "): " + parts[0]);
                return;
            }
            float volume = parts.length > 1 ? Float.parseFloat(parts[1]) : 1.0f;
            float pitch = parts.length > 2 ? Float.parseFloat(parts[2]) : 1.0f;
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound in config (sounds." + key + "): " + raw);
        }
    }
}
