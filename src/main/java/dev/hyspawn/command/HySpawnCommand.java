package dev.hyspawn.command;

import dev.hyspawn.HySpawn;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public final class HySpawnCommand implements BasicCommand {

    private final HySpawn plugin;

    public HySpawnCommand(HySpawn plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            plugin.reload();
            if (stack.getSender() instanceof Player player) {
                plugin.getMessageUtil().sendMessage(player, "reload-success");
            } else {
                stack.getSender().sendMessage("HySpawn configuration reloaded.");
            }
            return;
        }

        stack.getSender().sendMessage("Usage: /hyspawn reload");
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("reload");
        }
        return List.of();
    }

    @Override
    public @Nullable String permission() {
        return "hyspawn.admin";
    }
}
