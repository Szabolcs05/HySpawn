package dev.hyspawn.command;

import dev.hyspawn.HySpawn;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public final class UnsetVoidSpawnCommand implements BasicCommand {

    private final HySpawn plugin;

    public UnsetVoidSpawnCommand(HySpawn plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (!(stack.getSender() instanceof Player player)) {
            stack.getSender().sendMessage(MiniMessage.miniMessage().deserialize(
                    plugin.getConfigManager().getMessage("player-only")));
            return;
        }

        String worldName = args.length > 0 ? args[0] : player.getWorld().getName();

        if (!plugin.getSpawnDataManager().hasVoidSpawn(worldName)) {
            plugin.getMessageUtil().sendMessage(player, "void-spawn-not-set", "{world}", worldName);
            return;
        }

        plugin.getSpawnDataManager().removeVoidSpawn(worldName);
        plugin.getMessageUtil().sendMessage(player, "void-spawn-unset", "{world}", worldName);
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (args.length == 1) {
            return plugin.getServer().getWorlds().stream()
                    .map(w -> w.getName())
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }

    @Override
    public @Nullable String permission() {
        return "hyspawn.admin";
    }
}
