package dev.hyspawn.util;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import dev.hyspawn.config.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

public final class MessageUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private final ConfigManager configManager;

    public MessageUtil(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void sendMessage(Player player, String messageKey) {
        String raw = configManager.getPrefixedMessage(messageKey);
        Component component = MINI_MESSAGE.deserialize(raw);
        player.sendMessage(component);
    }

    public void sendMessage(Player player, String messageKey, String placeholder, String value) {
        String raw = configManager.getPrefixedMessage(messageKey).replace(placeholder, value);
        Component component = MINI_MESSAGE.deserialize(raw);
        player.sendMessage(component);
    }

    public void sendActionBar(Player player, String messageKey, String placeholder, String value) {
        String raw = configManager.getMessage(messageKey).replace(placeholder, value);
        Component component = MINI_MESSAGE.deserialize(raw);

        WrapperPlayServerSystemChatMessage packet = new WrapperPlayServerSystemChatMessage(
                true,
                component
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

    public Component parse(String miniMessageString) {
        return MINI_MESSAGE.deserialize(miniMessageString);
    }
}
