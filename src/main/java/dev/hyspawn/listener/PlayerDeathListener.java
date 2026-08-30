package dev.hyspawn.listener;

import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import dev.hyspawn.HySpawn;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerDeathListener implements Listener {

    // Folia's region-threaded respawn logic doesn't reliably fire
    // PlayerRespawnEvent / PlayerPostRespawnEvent and mishandles
    // setRespawnLocation (PaperMC/Folia#229, #337). On Folia the respawn is
    // corrected by watching the dead player instead of using the event.
    private static final boolean FOLIA = detectFolia();

    private final HySpawn plugin;
    private final Set<UUID> watching = ConcurrentHashMap.newKeySet();

    public PlayerDeathListener(HySpawn plugin) {
        this.plugin = plugin;
    }

    private static boolean detectFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (FOLIA) return;
        if (!plugin.getSpawnDataManager().hasGlobalSpawn()) return;

        // A valid bed or respawn anchor takes priority over the global spawn
        if (plugin.getConfigManager().isRespectBedSpawn()
                && (event.isBedSpawn() || event.isAnchorSpawn())) {
            return;
        }

        event.setRespawnLocation(plugin.getSpawnDataManager().getGlobalSpawn());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!FOLIA) return;
        if (!plugin.getSpawnDataManager().hasGlobalSpawn()) return;

        Player player = event.getEntity();
        Location bedSpawn = plugin.getConfigManager().isRespectBedSpawn()
                ? player.getRespawnLocation()
                : null;
        watchRespawn(player, bedSpawn);
    }

    /**
     * Poll the dead player until they respawn, then move them to the global
     * spawn unless they came back at their own bed/anchor.
     */
    private void watchRespawn(Player player, Location bedSpawn) {
        if (!watching.add(player.getUniqueId())) return;

        MyScheduledTask[] task = new MyScheduledTask[1];
        task[0] = plugin.getScheduler().runTaskTimer(() -> {
            if (!player.isOnline()) {
                stopWatching(player, task[0]);
                return;
            }
            if (player.isDead()) return;

            stopWatching(player, task[0]);

            Location spawn = plugin.getSpawnDataManager().getGlobalSpawn();
            if (spawn == null) return;

            Location current = player.getLocation();
            // Respawned at their bed/anchor — leave them there
            if (bedSpawn != null && current.getWorld().equals(bedSpawn.getWorld())
                    && current.distanceSquared(bedSpawn) <= 25.0) {
                return;
            }
            // Already at the global spawn — nothing to correct
            if (current.getWorld().equals(spawn.getWorld())
                    && current.distanceSquared(spawn) <= 4.0) {
                return;
            }
            player.teleportAsync(spawn);
        }, 2L, 2L);
    }

    private void stopWatching(Player player, MyScheduledTask task) {
        watching.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }
}
