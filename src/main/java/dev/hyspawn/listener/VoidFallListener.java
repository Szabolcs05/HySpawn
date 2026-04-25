package dev.hyspawn.listener;

import dev.hyspawn.HySpawn;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class VoidFallListener implements Listener {

    private final HySpawn plugin;
    // Prevent double-teleporting while async tp is in flight
    private final Set<UUID> processing = ConcurrentHashMap.newKeySet();

    public VoidFallListener(HySpawn plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        String worldName = player.getWorld().getName();
        if (!plugin.getSpawnDataManager().hasVoidSpawn(worldName)) return;

        double minY = player.getWorld().getMinHeight();
        if (event.getTo().getY() >= minY) return;

        rescuePlayer(player, worldName);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVoidDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.VOID) return;

        String worldName = player.getWorld().getName();
        if (!plugin.getSpawnDataManager().hasVoidSpawn(worldName)) return;

        event.setCancelled(true);
        event.setDamage(0);
        player.setFallDistance(0F);

        rescuePlayer(player, worldName);
    }

    private void rescuePlayer(Player player, String worldName) {
        if (!processing.add(player.getUniqueId())) return;

        Location voidSpawn = plugin.getSpawnDataManager().getVoidSpawn(worldName);
        player.setFallDistance(0F);

        // Delay by 1 tick so we're not teleporting inside an event handler
        plugin.getScheduler().runTaskLater(() -> {
            if (player.isOnline()) {
                player.teleportAsync(voidSpawn).thenAccept(success -> {
                    processing.remove(player.getUniqueId());
                    if (success) {
                        player.setFallDistance(0F);
                    }
                });
            } else {
                processing.remove(player.getUniqueId());
            }
        }, 1L);
    }
}
