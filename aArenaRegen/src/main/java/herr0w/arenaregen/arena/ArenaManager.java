package herr0w.arenaregen.arena;

import herr0w.arenaregen.ArenaRegenPlugin;
import herr0w.arenaregen.regen.RegenTask;
import herr0w.arenaregen.regen.SaveArenaTask;
import herr0w.arenaregen.selection.Selection;
import herr0w.arenaregen.storage.ArenaStorage;
import herr0w.arenaregen.util.ArenaName;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public final class ArenaManager {
    private final ArenaRegenPlugin plugin;
    private final ArenaStorage storage;
    private final Map<String, Arena> arenas = new HashMap<>();
    private final Set<String> runningRegens = new HashSet<>();
    private final Queue<RegenRequest> regenQueue = new ArrayDeque<>();
    private final Map<String, BukkitTask> autoTasks = new HashMap<>();
    private int activeRegens;

    public ArenaManager(ArenaRegenPlugin plugin) {
        this.plugin = plugin;
        this.storage = new ArenaStorage(plugin);
    }

    public void loadArenas() {
        arenas.clear();
        try {
            for (Arena arena : storage.loadAll()) {
                arenas.put(ArenaName.normalize(arena.name()), arena);
            }
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load arenas", exception);
        }
    }

    public Collection<Arena> arenas() {
        return new ArrayList<>(arenas.values());
    }

    public Arena arena(String name) {
        return arenas.get(ArenaName.normalize(name));
    }

    public void saveArena(Player player, String name, Selection selection) {
        if (!selection.complete()) {
            plugin.messageManager().send(player, "selection-incomplete");
            return;
        }
        if (!ArenaName.valid(name)) {
            plugin.messageManager().send(player, "invalid-name");
            return;
        }

        Location pos1 = selection.pos1();
        Location pos2 = selection.pos2();
        World world = pos1.getWorld();
        if (world == null) {
            return;
        }

        String normalized = ArenaName.normalize(name);
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        plugin.messageManager().send(player, "save-started", Map.of("arena", normalized));
        new SaveArenaTask(plugin, world, normalized, minX, minY, minZ, maxX, maxY, maxZ, plugin.configManager().saveAirBlocks(), arena -> {
            CompletableFuture.runAsync(() -> {
                try {
                    storage.save(arena);
                } catch (IOException exception) {
                    throw new IllegalStateException(exception);
                }
            }).whenComplete((ignored, throwable) -> plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (throwable != null) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to save arena " + normalized, throwable);
                    plugin.messageManager().send(player, "save-failed", Map.of("arena", normalized));
                    return;
                }
                arenas.put(normalized, arena);
                ensureArenaConfigDefaults(normalized);
                plugin.saveConfig();
                restartAutoRegen(normalized);
                plugin.messageManager().send(player, "arena-saved", Map.of("arena", normalized));
            }));
        }).runTaskTimer(plugin, 0L, plugin.configManager().ticksBetweenBatches());
    }

    public void requestRegen(String arenaName, CommandSender requester) {
        String normalized = ArenaName.normalize(arenaName);
        Arena arena = arenas.get(normalized);
        if (arena == null) {
            plugin.messageManager().send(requester, "arena-not-found");
            return;
        }
        if (runningRegens.contains(normalized)) {
            plugin.messageManager().send(requester, "regen-already-running", Map.of("arena", arena.name()));
            return;
        }

        RegenRequest request = new RegenRequest(arena, requester);
        if (activeRegens >= plugin.configManager().maxRegensAtOnce()) {
            regenQueue.add(request);
            plugin.messageManager().send(requester, "regen-queued", Map.of("arena", arena.name()));
            return;
        }
        startRegen(request);
    }

    private void startRegen(RegenRequest request) {
        Arena arena = request.arena();
        World world = plugin.getServer().getWorld(arena.worldName());
        if (world == null) {
            plugin.messageManager().send(request.requester(), "world-missing", Map.of("world", arena.worldName()));
            return;
        }

        activeRegens++;
        runningRegens.add(ArenaName.normalize(arena.name()));
        plugin.messageManager().send(request.requester(), "regen-started", Map.of("arena", arena.name()));
        new RegenTask(plugin, arena, world, () -> {
            activeRegens--;
            runningRegens.remove(ArenaName.normalize(arena.name()));
            plugin.messageManager().send(request.requester(), "regen-finished", Map.of("arena", arena.name()));
            drainQueue();
        }).runTaskTimer(plugin, 0L, plugin.configManager().ticksBetweenBatches());
    }

    private void drainQueue() {
        while (activeRegens < plugin.configManager().maxRegensAtOnce() && !regenQueue.isEmpty()) {
            RegenRequest next = regenQueue.poll();
            if (!runningRegens.contains(ArenaName.normalize(next.arena().name()))) {
                startRegen(next);
            }
        }
    }

    public boolean deleteArena(String name) {
        String normalized = ArenaName.normalize(name);
        BukkitTask task = autoTasks.remove(normalized);
        if (task != null) {
            task.cancel();
        }
        arenas.remove(normalized);
        plugin.getConfig().set("arenas." + normalized, null);
        plugin.saveConfig();
        return storage.delete(normalized);
    }

    public void reload() {
        plugin.configManager().reload();
        plugin.messageManager().reload();
        loadArenas();
        startAutoRegens();
    }

    public void startAutoRegens() {
        for (BukkitTask task : autoTasks.values()) {
            task.cancel();
        }
        autoTasks.clear();

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("arenas");
        if (section == null) {
            return;
        }
        for (String name : section.getKeys(false)) {
            restartAutoRegen(name);
        }
    }

    private void restartAutoRegen(String arenaName) {
        String normalized = ArenaName.normalize(arenaName);
        BukkitTask oldTask = autoTasks.remove(normalized);
        if (oldTask != null) {
            oldTask.cancel();
        }

        if (!plugin.getConfig().getBoolean("arenas." + normalized + ".enabled", true)) {
            return;
        }
        long intervalSeconds = Math.max(1L, plugin.getConfig().getLong("arenas." + normalized + ".regen-interval-seconds", 300L));
        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            Arena arena = arenas.get(normalized);
            if (arena != null) {
                requestRegen(arena.name(), plugin.getServer().getConsoleSender());
            }
        }, intervalSeconds * 20L, intervalSeconds * 20L);
        autoTasks.put(normalized, task);
    }

    private void ensureArenaConfigDefaults(String name) {
        if (!plugin.getConfig().isConfigurationSection("arenas." + name)) {
            plugin.getConfig().set("arenas." + name + ".enabled", true);
            plugin.getConfig().set("arenas." + name + ".regen-interval-seconds", 300);
        }
    }

    public boolean enabled(String name) {
        return plugin.getConfig().getBoolean("arenas." + ArenaName.normalize(name) + ".enabled", true);
    }

    public long interval(String name) {
        return plugin.getConfig().getLong("arenas." + ArenaName.normalize(name) + ".regen-interval-seconds", 300L);
    }

    public void shutdown() {
        for (BukkitTask task : autoTasks.values()) {
            task.cancel();
        }
        autoTasks.clear();
        regenQueue.clear();
        plugin.getServer().getScheduler().cancelTasks(plugin);
    }

    private record RegenRequest(Arena arena, CommandSender requester) {
    }
}
