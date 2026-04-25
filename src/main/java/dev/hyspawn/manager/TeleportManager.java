package dev.hyspawn.manager;

import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import dev.hyspawn.HySpawn;
import dev.hyspawn.config.ConfigManager;
import dev.hyspawn.util.MessageUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TeleportManager {

    private final HySpawn plugin;
    private final Map<UUID, MyScheduledTask> pendingTeleports = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public TeleportManager(HySpawn plugin) {
        this.plugin = plugin;
    }

    public boolean isTeleporting(Player player) {
        return pendingTeleports.containsKey(player.getUniqueId());
    }

    /**
     * Returns remaining cooldown in seconds, or 0 if no cooldown active.
     */
    public int getRemainingCooldown(Player player) {
        Long expiry = cooldowns.get(player.getUniqueId());
        if (expiry == null) return 0;
        long remaining = (expiry - System.currentTimeMillis()) / 1000;
        if (remaining <= 0) {
            cooldowns.remove(player.getUniqueId());
            return 0;
        }
        return (int) remaining + 1; // +1 so it shows "1" instead of "0" on the last second
    }

    public void startTeleport(Player player, Location destination) {
        ConfigManager config = plugin.getConfigManager();
        MessageUtil msg = plugin.getMessageUtil();
        int countdown = config.getCountdown();

        if (isTeleporting(player)) {
            msg.sendMessage(player, "spawn-already-teleporting");
            return;
        }

        // Bypass countdown
        if (countdown <= 0 || player.hasPermission("hyspawn.bypass.countdown")) {
            teleport(player, destination);
            return;
        }

        final int[] remaining = {countdown};

        // Send initial actionbar + countdown tick sound
        msg.sendActionBar(player, "countdown-actionbar", "{seconds}", String.valueOf(remaining[0]));
        msg.sendMessage(player, "spawn-countdown", "{seconds}", String.valueOf(remaining[0]));
        config.playSound(player, "countdown-tick");

        MyScheduledTask task = plugin.getScheduler().runTaskTimer(() -> {
            if (!player.isOnline()) {
                cancel(player);
                return;
            }

            remaining[0]--;

            if (remaining[0] <= 0) {
                cancel(player);
                teleport(player, destination);
                return;
            }

            msg.sendActionBar(player, "countdown-actionbar", "{seconds}", String.valueOf(remaining[0]));

            // Play final tick sound on last second, normal tick otherwise
            if (remaining[0] == 1) {
                config.playSound(player, "countdown-final");
            } else {
                config.playSound(player, "countdown-tick");
            }
        }, 20L, 20L);

        pendingTeleports.put(player.getUniqueId(), task);
    }

    public void cancelTeleport(Player player) {
        if (isTeleporting(player)) {
            cancel(player);
            plugin.getMessageUtil().sendMessage(player, "spawn-cancelled");
            plugin.getConfigManager().playSound(player, "cancelled");
        }
    }

    public void cancelSilent(Player player) {
        cancel(player);
    }

    private void cancel(Player player) {
        MyScheduledTask task = pendingTeleports.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    private void teleport(Player player, Location destination) {
        player.teleportAsync(destination).thenAccept(success -> {
            if (success) {
                plugin.getMessageUtil().sendMessage(player, "spawn-teleported");
                plugin.getConfigManager().playSound(player, "teleport");

                int cooldownSeconds = plugin.getConfigManager().getCooldown();
                if (cooldownSeconds > 0 && !player.hasPermission("hyspawn.bypass.cooldown")) {
                    cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + (cooldownSeconds * 1000L));
                }
            }
        });
    }

    public void cancelAll() {
        pendingTeleports.values().forEach(MyScheduledTask::cancel);
        pendingTeleports.clear();
    }
}
