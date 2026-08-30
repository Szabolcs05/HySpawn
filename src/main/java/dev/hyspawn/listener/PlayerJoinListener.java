package dev.hyspawn.listener;

import dev.hyspawn.HySpawn;
import io.papermc.paper.event.player.AsyncPlayerSpawnLocationEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class PlayerJoinListener implements Listener {

    private final HySpawn plugin;

    public PlayerJoinListener(HySpawn plugin) {
        this.plugin = plugin;
    }

    /**
     * Fires async BEFORE the player spawns in the world.
     * Player loads directly at spawn — no teleport flash.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpawnLocation(AsyncPlayerSpawnLocationEvent event) {
        if (!plugin.getSpawnDataManager().hasGlobalSpawn()) return;

        // New players spawn at the global spawn. Returning players whom vanilla
        // dumped at the world spawnpoint (invalid/lost logout position) are
        // redirected too, so nobody lands at the camped world spawn.
        if (event.isNewPlayer() || isAtWorldSpawn(event.getSpawnLocation())) {
            event.setSpawnLocation(plugin.getSpawnDataManager().getGlobalSpawn());
        }
    }

    private static boolean isAtWorldSpawn(Location loc) {
        Location worldSpawn = loc.getWorld().getSpawnLocation();
        return worldSpawn.getBlockX() == loc.getBlockX()
                && worldSpawn.getBlockY() == loc.getBlockY()
                && worldSpawn.getBlockZ() == loc.getBlockZ();
    }

    /**
     * Send the welcome message after they've actually joined.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore() && plugin.getSpawnDataManager().hasGlobalSpawn()) {
            plugin.getScheduler().runTaskLater(() -> {
                if (player.isOnline()) {
                    plugin.getMessageUtil().sendMessage(player, "spawn-first-join");
                }
            }, 5L);
        }
    }
}
