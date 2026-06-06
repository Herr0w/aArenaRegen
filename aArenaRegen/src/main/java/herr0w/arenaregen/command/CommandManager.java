package herr0w.arenaregen.command;

import herr0w.arenaregen.ArenaRegenPlugin;
import herr0w.arenaregen.arena.Arena;
import herr0w.arenaregen.util.ArenaName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

public final class CommandManager implements TabExecutor {
    private static final List<String> SUBCOMMANDS = List.of("wand", "save", "regen", "list", "delete", "reload", "info");
    private final ArenaRegenPlugin plugin;

    public CommandManager(ArenaRegenPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            plugin.messageManager().send(sender, "usage");
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "wand" -> wand(sender);
            case "save" -> save(sender, args);
            case "regen" -> regen(sender, args);
            case "list" -> list(sender);
            case "delete" -> delete(sender, args);
            case "reload" -> reload(sender);
            case "info" -> info(sender, args);
            default -> plugin.messageManager().send(sender, "usage");
        }
        return true;
    }

    private void wand(CommandSender sender) {
        if (!has(sender, "arenargen.wand")) {
            return;
        }
        if (!(sender instanceof Player player)) {
            plugin.messageManager().send(sender, "only-players");
            return;
        }
        player.getInventory().addItem(plugin.selectionManager().createWand());
        plugin.messageManager().send(player, "wand-given");
    }

    private void save(CommandSender sender, String[] args) {
        if (!has(sender, "arenargen.save")) {
            return;
        }
        if (!(sender instanceof Player player)) {
            plugin.messageManager().send(sender, "only-players");
            return;
        }
        if (args.length < 2) {
            plugin.messageManager().send(sender, "usage");
            return;
        }
        plugin.arenaManager().saveArena(player, args[1], plugin.selectionManager().selection(player));
    }

    private void regen(CommandSender sender, String[] args) {
        if (!has(sender, "arenargen.regen")) {
            return;
        }
        if (args.length < 2) {
            plugin.messageManager().send(sender, "usage");
            return;
        }
        plugin.arenaManager().requestRegen(args[1], sender);
    }

    private void list(CommandSender sender) {
        if (!has(sender, "arenargen.info")) {
            return;
        }
        List<String> names = plugin.arenaManager().arenas().stream().map(Arena::name).sorted().toList();
        if (names.isEmpty()) {
            plugin.messageManager().send(sender, "list-empty");
            return;
        }
        plugin.messageManager().send(sender, "list-header", Map.of("arenas", String.join(", ", names)));
    }

    private void delete(CommandSender sender, String[] args) {
        if (!has(sender, "arenargen.delete")) {
            return;
        }
        if (args.length < 2) {
            plugin.messageManager().send(sender, "usage");
            return;
        }
        Arena arena = plugin.arenaManager().arena(args[1]);
        if (arena == null) {
            plugin.messageManager().send(sender, "arena-not-found");
            return;
        }
        plugin.arenaManager().deleteArena(arena.name());
        plugin.messageManager().send(sender, "arena-deleted", Map.of("arena", arena.name()));
    }

    private void reload(CommandSender sender) {
        if (!has(sender, "arenargen.reload")) {
            return;
        }
        plugin.arenaManager().reload();
        plugin.messageManager().send(sender, "reload-complete");
    }

    private void info(CommandSender sender, String[] args) {
        if (!has(sender, "arenargen.info")) {
            return;
        }
        if (args.length < 2) {
            plugin.messageManager().send(sender, "usage");
            return;
        }
        Arena arena = plugin.arenaManager().arena(args[1]);
        if (arena == null) {
            plugin.messageManager().send(sender, "arena-not-found");
            return;
        }
        plugin.messageManager().send(sender, "info", Map.of(
                "arena", arena.name(),
                "world", arena.worldName(),
                "size", arena.sizeX() + "x" + arena.sizeY() + "x" + arena.sizeZ(),
                "blocks", Integer.toString(arena.savedBlocks().size()),
                "enabled", Boolean.toString(plugin.arenaManager().enabled(arena.name())),
                "interval", Long.toString(plugin.arenaManager().interval(arena.name()))
        ));
    }

    private boolean has(CommandSender sender, String permission) {
        if (sender.hasPermission("arenargen.admin") || sender.hasPermission(permission)) {
            return true;
        }
        plugin.messageManager().send(sender, "no-permission");
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(SUBCOMMANDS, args[0]);
        }
        if (args.length == 2 && List.of("regen", "delete", "info").contains(args[0].toLowerCase(Locale.ROOT))) {
            return filter(plugin.arenaManager().arenas().stream().map(Arena::name).toList(), args[1]);
        }
        return Collections.emptyList();
    }

    private List<String> filter(List<String> values, String prefix) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        for (String value : values) {
            if (value.toLowerCase(Locale.ROOT).startsWith(lower)) {
                result.add(value);
            }
        }
        return result;
    }
}
