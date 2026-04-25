package dev.hyspawn.command;

import dev.hyspawn.HySpawn;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public final class SetSpawnCommand implements BasicCommand {

    private final HySpawn plugin;

    public SetSpawnCommand(HySpawn plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (!(stack.getSender() instanceof Player player)) {
            stack.getSender().sendMessage(MiniMessage.miniMessage().deserialize(
                    plugin.getConfigManager().getMessage("player-only")));
            return;
        }

        plugin.getSpawnDataManager().setGlobalSpawn(player.getLocation());
        plugin.getMessageUtil().sendMessage(player, "spawn-set");
    }

    @Override
    public @Nullable String permission() {
        return "hyspawn.admin";
    }
}
