package herr0w.arenaregen.config;

import herr0w.arenaregen.ArenaRegenPlugin;
import java.io.File;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public final class MessageManager {
    private final ArenaRegenPlugin plugin;
    private FileConfiguration messages;

    public MessageManager(ArenaRegenPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        messages = YamlConfiguration.loadConfiguration(file);
    }

    public void send(CommandSender sender, String key) {
        send(sender, key, Map.of());
    }

    public void send(CommandSender sender, String key, Map<String, String> placeholders) {
        sender.sendMessage(format(key, placeholders));
    }

    public String format(String key, Map<String, String> placeholders) {
        String prefix = messages.getString("messages.prefix", "&8[&aArenaRegen&8] ");
        String raw = messages.getString("messages." + key, key);
        String value = prefix + raw;
        return color(replacePlaceholders(value, placeholders));
    }

    public String formatRaw(String key, Map<String, String> placeholders) {
        String raw = messages.getString("messages." + key, key);
        return color(replacePlaceholders(raw, placeholders));
    }

    private String replacePlaceholders(String value, Map<String, String> placeholders) {
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            value = value.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return value;
    }

    private String color(String value) {
        return ChatColor.translateAlternateColorCodes('&', value);
    }
}
