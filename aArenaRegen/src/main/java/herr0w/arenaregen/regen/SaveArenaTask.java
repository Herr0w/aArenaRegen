package herr0w.arenaregen.regen;

import herr0w.arenaregen.ArenaRegenPlugin;
import herr0w.arenaregen.arena.Arena;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public final class SaveArenaTask extends BukkitRunnable {
    private final ArenaRegenPlugin plugin;
    private final World world;
    private final String name;
    private final int minX;
    private final int minY;
    private final int minZ;
    private final int maxX;
    private final int maxY;
    private final int maxZ;
    private final boolean saveAir;
    private final Consumer<Arena> onComplete;
    private final Player player;
    private final BossBar bossBar;
    private final long totalBlocks;
    private final Map<Long, String> blocks = new HashMap<>();
    private int x;
    private int y;
    private int z;
    private long processedBlocks;
    private boolean bossBarRemoved;

    public SaveArenaTask(ArenaRegenPlugin plugin, World world, String name, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean saveAir, Player player, Consumer<Arena> onComplete) {
        this.plugin = plugin;
        this.world = world;
        this.name = name;
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        this.saveAir = saveAir;
        this.player = player;
        this.onComplete = onComplete;
        this.totalBlocks = (long) (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
        this.x = minX;
        this.y = minY;
        this.z = minZ;
        this.bossBar = Bukkit.createBossBar("", BarColor.GREEN, BarStyle.SOLID);
        this.bossBar.addPlayer(player);
        updateBossBar();
    }

    @Override
    public void run() {
        if (!player.isOnline()) {
            removeBossBar();
        }

        try {
            int budget = plugin.configManager().blocksPerTick();
            while (budget-- > 0) {
                Block block = world.getBlockAt(x, y, z);
                Material type = block.getType();
                if (saveAir || !type.isAir()) {
                    blocks.put(Arena.key(x - minX, y - minY, z - minZ), block.getBlockData().getAsString());
                }
                processedBlocks++;
                if (!advance()) {
                    updateBossBar();
                    cancel();
                    onComplete.accept(new Arena(name, world.getName(), minX, minY, minZ, maxX, maxY, maxZ, blocks));
                    return;
                }
            }
            updateBossBar();
        } catch (RuntimeException exception) {
            removeBossBar();
            throw exception;
        }
    }

    public void removeBossBar() {
        if (bossBarRemoved) {
            return;
        }
        bossBar.removeAll();
        bossBarRemoved = true;
    }

    private void updateBossBar() {
        if (bossBarRemoved) {
            return;
        }
        double progress = totalBlocks <= 0 ? 1.0D : Math.min(1.0D, Math.max(0.0D, (double) processedBlocks / (double) totalBlocks));
        int percent = (int) Math.floor(progress * 100.0D);
        bossBar.setProgress(progress);
        bossBar.setTitle(plugin.messageManager().formatRaw("save-bossbar-title", Map.of(
                "arena", name,
                "percent", Integer.toString(percent),
                "current", Long.toString(processedBlocks),
                "total", Long.toString(totalBlocks)
        )));
    }

    private boolean advance() {
        z++;
        if (z <= maxZ) {
            return true;
        }
        z = minZ;
        y++;
        if (y <= maxY) {
            return true;
        }
        y = minY;
        x++;
        return x <= maxX;
    }
}
