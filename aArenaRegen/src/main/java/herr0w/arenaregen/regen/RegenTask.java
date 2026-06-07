package herr0w.arenaregen.regen;

import herr0w.arenaregen.ArenaRegenPlugin;
import herr0w.arenaregen.arena.Arena;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.scheduler.BukkitRunnable;

public final class RegenTask extends BukkitRunnable {
    private final ArenaRegenPlugin plugin;
    private final Arena arena;
    private final World world;
    private final Runnable onComplete;
    private int x;
    private int y;
    private int z;

    public RegenTask(ArenaRegenPlugin plugin, Arena arena, World world, Runnable onComplete) {
        this.plugin = plugin;
        this.arena = arena;
        this.world = world;
        this.onComplete = onComplete;
        this.x = arena.minX();
        this.y = arena.minY();
        this.z = arena.minZ();
    }

    @Override
    public void run() {
        int budget = plugin.configManager().blocksPerTick();
        boolean physics = plugin.configManager().usePhysics();
        Map<Long, String> saved = arena.savedBlocks();

        while (budget-- > 0) {
            int relX = x - arena.minX();
            int relY = y - arena.minY();
            int relZ = z - arena.minZ();
            String expected = saved.get(Arena.key(relX, relY, relZ));
            Block block = world.getBlockAt(x, y, z);

            if (expected == null) {
                if (!block.getType().isAir()) {
                    block.setType(Material.AIR, physics);
                }
            } else if (!block.getBlockData().getAsString().equals(expected)) {
                BlockData data = Bukkit.createBlockData(expected);
                block.setBlockData(data, physics);
            }

            if (!advance()) {
                cancel();
                onComplete.run();
                return;
            }
        }
    }

    private boolean advance() {
        z++;
        if (z <= arena.maxZ()) {
            return true;
        }
        z = arena.minZ();
        y++;
        if (y <= arena.maxY()) {
            return true;
        }
        y = arena.minY();
        x++;
        return x <= arena.maxX();
    }
}
