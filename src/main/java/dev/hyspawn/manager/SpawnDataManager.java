package dev.hyspawn.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class SpawnDataManager {

    private final JavaPlugin plugin;
    private final File dataFile;
    private FileConfiguration data;

    private Location globalSpawn;
    private final Map<String, Location> voidSpawns = new HashMap<>();

    public SpawnDataManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "spawns.yml");
        reload();
    }

    public void reload() {
        if (!dataFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create spawns.yml: " + e.getMessage());
            }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
        loadSpawns();
    }

    private void loadSpawns() {
        globalSpawn = deserializeLocation("spawn");

        voidSpawns.clear();
        if (data.isConfigurationSection("void-spawns")) {
            for (String worldName : data.getConfigurationSection("void-spawns").getKeys(false)) {
                Location loc = deserializeLocation("void-spawns." + worldName);
                if (loc != null) {
                    voidSpawns.put(worldName, loc);
                }
            }
        }
    }

    private void save() {
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save spawns.yml: " + e.getMessage());
        }
    }

    public void setGlobalSpawn(Location location) {
        this.globalSpawn = location.clone();
        serializeLocation("spawn", location);
        save();
    }

    public void removeGlobalSpawn() {
        this.globalSpawn = null;
        data.set("spawn", null);
        save();
    }

    public Location getGlobalSpawn() {
        return globalSpawn != null ? globalSpawn.clone() : null;
    }

    public boolean hasGlobalSpawn() {
        return globalSpawn != null;
    }

    public void setVoidSpawn(String worldName, Location location) {
        voidSpawns.put(worldName, location.clone());
        serializeLocation("void-spawns." + worldName, location);
        save();
    }

    public void removeVoidSpawn(String worldName) {
        voidSpawns.remove(worldName);
        data.set("void-spawns." + worldName, null);
        save();
    }

    public Location getVoidSpawn(String worldName) {
        Location loc = voidSpawns.get(worldName);
        return loc != null ? loc.clone() : null;
    }

    public boolean hasVoidSpawn(String worldName) {
        return voidSpawns.containsKey(worldName);
    }

    private void serializeLocation(String path, Location loc) {
        data.set(path + ".world", loc.getWorld().getName());
        data.set(path + ".x", loc.getX());
        data.set(path + ".y", loc.getY());
        data.set(path + ".z", loc.getZ());
        data.set(path + ".yaw", (double) loc.getYaw());
        data.set(path + ".pitch", (double) loc.getPitch());
    }

    private Location deserializeLocation(String path) {
        if (!data.contains(path + ".world")) return null;
        String worldName = data.getString(path + ".world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        return new Location(
                world,
                data.getDouble(path + ".x"),
                data.getDouble(path + ".y"),
                data.getDouble(path + ".z"),
                (float) data.getDouble(path + ".yaw"),
                (float) data.getDouble(path + ".pitch")
        );
    }
}
