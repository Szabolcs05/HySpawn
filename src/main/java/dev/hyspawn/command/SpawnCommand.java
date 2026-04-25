package dev.hyspawn.command;

import dev.hyspawn.HySpawn;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public final class SpawnCommand implements BasicCommand {

    private final HySpawn plugin;

    public SpawnCommand(HySpawn plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (!(stack.getSender() instanceof Player player)) {
            stack.getSender().sendMessage(MiniMessage.miniMessage().deserialize(
                    plugin.getConfigManager().getMessage("player-only")));
            return;
        }

        Location spawn = plugin.getSpawnDataManager().getGlobalSpawn();
        if (spawn == null) {
            plugin.getMessageUtil().sendMessage(player, "spawn-not-set");
            return;
        }

        // Check cooldown
        if (!player.hasPermission("hyspawn.bypass.cooldown")) {
            int remaining = plugin.getTeleportManager().getRemainingCooldown(player);
            if (remaining > 0) {
                plugin.getMessageUtil().sendMessage(player, "spawn-cooldown", "{seconds}", String.valueOf(remaining));
                return;
            }
        }

        plugin.getTeleportManager().startTeleport(player, spawn);
    }

    @Override
    public @Nullable String permission() {
        return "hyspawn.spawn";
    }
}
