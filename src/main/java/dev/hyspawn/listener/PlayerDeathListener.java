package dev.hyspawn.listener;

import dev.hyspawn.HySpawn;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public final class PlayerDeathListener implements Listener {

    private final HySpawn plugin;

    public PlayerDeathListener(HySpawn plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!plugin.getSpawnDataManager().hasGlobalSpawn()) return;

        Location spawn = plugin.getSpawnDataManager().getGlobalSpawn();
        event.setRespawnLocation(spawn);
    }
}
