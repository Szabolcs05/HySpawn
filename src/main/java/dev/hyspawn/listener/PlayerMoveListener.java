package dev.hyspawn.listener;

import dev.hyspawn.HySpawn;
import dev.hyspawn.manager.TeleportManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerMoveListener implements Listener {

    private final HySpawn plugin;
    private final TeleportManager teleportManager;

    public PlayerMoveListener(HySpawn plugin, TeleportManager teleportManager) {
        this.plugin = plugin;
        this.teleportManager = teleportManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.getConfigManager().isCancelOnMove()) return;

        Player player = event.getPlayer();
        if (!teleportManager.isTeleporting(player)) return;

        Location from = event.getFrom();
        Location to = event.getTo();

        // Only cancel if they actually moved blocks, not just looked around
        if (from.getBlockX() != to.getBlockX()
                || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ()) {
            teleportManager.cancelTeleport(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        teleportManager.cancelSilent(event.getPlayer());
    }
}
