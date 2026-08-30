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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class SpawnDataManager {

    private final JavaPlugin plugin;
    private final File dataFile;
    private FileConfiguration data;

    private StoredLocation globalSpawn;
    private final Map<String, StoredLocation> voidSpawns = new HashMap<>();
    private final Set<String> warnedWorlds = ConcurrentHashMap.newKeySet();

    /**
     * A location whose world is resolved lazily on each use, so spawns in
     * worlds that load after this plugin enables still work.
     */
    private record StoredLocation(String worldName, double x, double y, double z, float yaw, float pitch) {

        static StoredLocation of(Location loc) {
            return new StoredLocation(loc.getWorld().getName(),
                    loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        }

        Location resolve() {
            World world = Bukkit.getWorld(worldName);
            if (world == null) return null;
            return new Location(world, x, y, z, yaw, pitch);
        }
    }

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
        warnedWorlds.clear();
        loadSpawns();
    }

    private void loadSpawns() {
        globalSpawn = deserializeLocation("spawn");

        voidSpawns.clear();
        if (data.isConfigurationSection("void-spawns")) {
            for (String worldName : data.getConfigurationSection("void-spawns").getKeys(false)) {
                StoredLocation loc = deserializeLocation("void-spawns." + worldName);
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
        this.globalSpawn = StoredLocation.of(location);
        serializeLocation("spawn", location);
        save();
    }

    public void removeGlobalSpawn() {
        this.globalSpawn = null;
        data.set("spawn", null);
        save();
    }

    public Location getGlobalSpawn() {
        return resolve(globalSpawn);
    }

    public boolean hasGlobalSpawn() {
        return getGlobalSpawn() != null;
    }

    public void setVoidSpawn(String worldName, Location location) {
        voidSpawns.put(worldName, StoredLocation.of(location));
        serializeLocation("void-spawns." + worldName, location);
        save();
    }

    public void removeVoidSpawn(String worldName) {
        voidSpawns.remove(worldName);
        data.set("void-spawns." + worldName, null);
        save();
    }

    public Location getVoidSpawn(String worldName) {
        return resolve(voidSpawns.get(worldName));
    }

    public boolean hasVoidSpawn(String worldName) {
        return getVoidSpawn(worldName) != null;
    }

    private Location resolve(StoredLocation stored) {
        if (stored == null) return null;
        Location loc = stored.resolve();
        if (loc == null && warnedWorlds.add(stored.worldName())) {
            plugin.getLogger().warning("Spawn world '" + stored.worldName()
                    + "' is not loaded — that spawn is unavailable until the world loads.");
        }
        return loc;
    }

    private void serializeLocation(String path, Location loc) {
        data.set(path + ".world", loc.getWorld().getName());
        data.set(path + ".x", loc.getX());
        data.set(path + ".y", loc.getY());
        data.set(path + ".z", loc.getZ());
        data.set(path + ".yaw", (double) loc.getYaw());
        data.set(path + ".pitch", (double) loc.getPitch());
    }

    private StoredLocation deserializeLocation(String path) {
        if (!data.contains(path + ".world")) return null;
        return new StoredLocation(
                data.getString(path + ".world"),
                data.getDouble(path + ".x"),
                data.getDouble(path + ".y"),
                data.getDouble(path + ".z"),
                (float) data.getDouble(path + ".yaw"),
                (float) data.getDouble(path + ".pitch")
        );
    }
}
