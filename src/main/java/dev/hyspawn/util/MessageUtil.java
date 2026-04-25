package dev.hyspawn.util;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import dev.hyspawn.config.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MessageUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    // Matches &#RRGGBB or &x&R&R&G&G&B&B (Bukkit-style hex)
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([0-9a-fA-F]{6})");
    private static final Pattern BUKKIT_HEX_PATTERN = Pattern.compile("&x(&[0-9a-fA-F]){6}");

    private final ConfigManager configManager;

    public MessageUtil(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void sendMessage(Player player, String messageKey) {
        String raw = configManager.getPrefixedMessage(messageKey);
        player.sendMessage(deserialize(raw));
    }

    public void sendMessage(Player player, String messageKey, String placeholder, String value) {
        String raw = configManager.getPrefixedMessage(messageKey).replace(placeholder, value);
        player.sendMessage(deserialize(raw));
    }

    public void sendActionBar(Player player, String messageKey, String placeholder, String value) {
        String raw = configManager.getMessage(messageKey).replace(placeholder, value);
        Component component = deserialize(raw);

        WrapperPlayServerSystemChatMessage packet = new WrapperPlayServerSystemChatMessage(
                true,
                component
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

    public Component parse(String input) {
        return deserialize(input);
    }

    /**
     * Deserializes a string supporting both MiniMessage and legacy formats.
     * Checks if the string contains legacy color codes (&, §) and handles accordingly.
     * Supports: &a, §a, &#RRGGBB, &x&R&R&G&G&B&B, and full MiniMessage tags.
     */
    private Component deserialize(String input) {
        if (input == null || input.isEmpty()) return Component.empty();

        // If it contains MiniMessage tags, try MiniMessage first
        if (input.contains("<") && input.contains(">")) {
            // Convert any legacy codes embedded alongside MiniMessage tags
            String converted = convertLegacyToMiniMessage(input);
            return MINI_MESSAGE.deserialize(converted);
        }

        // Pure legacy string
        if (input.contains("&") || input.contains("§")) {
            String processed = convertHexCodes(input);
            // Replace & with § for the legacy serializer
            processed = processed.replace('&', '§');
            return LegacyComponentSerializer.legacySection().deserialize(processed);
        }

        // Fallback to MiniMessage
        return MINI_MESSAGE.deserialize(input);
    }

    /**
     * Converts legacy color codes embedded in a MiniMessage string to MiniMessage format.
     * &#RRGGBB -> <color:#RRGGBB>
     * &x&R&R&G&G&B&B -> <color:#RRGGBB>
     * &a -> <green>, &c -> <red>, etc.
     */
    private String convertLegacyToMiniMessage(String input) {
        // Convert &#RRGGBB to <color:#RRGGBB>
        Matcher hexMatcher = HEX_PATTERN.matcher(input);
        StringBuilder sb = new StringBuilder();
        while (hexMatcher.find()) {
            hexMatcher.appendReplacement(sb, "<color:#" + hexMatcher.group(1) + ">");
        }
        hexMatcher.appendTail(sb);
        input = sb.toString();

        // Convert &x&R&R&G&G&B&B to <color:#RRGGBB>
        Matcher bukkitHexMatcher = BUKKIT_HEX_PATTERN.matcher(input);
        sb = new StringBuilder();
        while (bukkitHexMatcher.find()) {
            String match = bukkitHexMatcher.group().replace("&x", "").replace("&", "");
            bukkitHexMatcher.appendReplacement(sb, "<color:#" + match + ">");
        }
        bukkitHexMatcher.appendTail(sb);
        input = sb.toString();

        // Convert legacy &X codes to MiniMessage tags
        input = input.replace("&0", "<black>").replace("§0", "<black>");
        input = input.replace("&1", "<dark_blue>").replace("§1", "<dark_blue>");
        input = input.replace("&2", "<dark_green>").replace("§2", "<dark_green>");
        input = input.replace("&3", "<dark_aqua>").replace("§3", "<dark_aqua>");
        input = input.replace("&4", "<dark_red>").replace("§4", "<dark_red>");
        input = input.replace("&5", "<dark_purple>").replace("§5", "<dark_purple>");
        input = input.replace("&6", "<gold>").replace("§6", "<gold>");
        input = input.replace("&7", "<gray>").replace("§7", "<gray>");
        input = input.replace("&8", "<dark_gray>").replace("§8", "<dark_gray>");
        input = input.replace("&9", "<blue>").replace("§9", "<blue>");
        input = input.replace("&a", "<green>").replace("§a", "<green>");
        input = input.replace("&b", "<aqua>").replace("§b", "<aqua>");
        input = input.replace("&c", "<red>").replace("§c", "<red>");
        input = input.replace("&d", "<light_purple>").replace("§d", "<light_purple>");
        input = input.replace("&e", "<yellow>").replace("§e", "<yellow>");
        input = input.replace("&f", "<white>").replace("§f", "<white>");
        input = input.replace("&k", "<obfuscated>").replace("§k", "<obfuscated>");
        input = input.replace("&l", "<bold>").replace("§l", "<bold>");
        input = input.replace("&m", "<strikethrough>").replace("§m", "<strikethrough>");
        input = input.replace("&n", "<underlined>").replace("§n", "<underlined>");
        input = input.replace("&o", "<italic>").replace("§o", "<italic>");
        input = input.replace("&r", "<reset>").replace("§r", "<reset>");

        return input;
    }

    /**
     * Converts &#RRGGBB and &x&R&R&G&G&B&B to §x§R§R§G§G§B§B for the legacy serializer.
     */
    private String convertHexCodes(String input) {
        // Convert &#RRGGBB to §x§R§R§G§G§B§B
        Matcher hexMatcher = HEX_PATTERN.matcher(input);
        StringBuilder sb = new StringBuilder();
        while (hexMatcher.find()) {
            String hex = hexMatcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                replacement.append('§').append(c);
            }
            hexMatcher.appendReplacement(sb, replacement.toString());
        }
        hexMatcher.appendTail(sb);
        return sb.toString();
    }
}
