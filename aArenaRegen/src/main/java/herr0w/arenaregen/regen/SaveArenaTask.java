package herr0w.arenaregen.regen;

import herr0w.arenaregen.ArenaRegenPlugin;
import herr0w.arenaregen.arena.Arena;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
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
    private final Map<Long, String> blocks = new HashMap<>();
    private int x;
    private int y;
    private int z;

    public SaveArenaTask(ArenaRegenPlugin plugin, World world, String name, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean saveAir, Consumer<Arena> onComplete) {
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
        this.onComplete = onComplete;
        this.x = minX;
        this.y = minY;
        this.z = minZ;
    }

    @Override
    public void run() {
        int budget = plugin.configManager().blocksPerTick();
        while (budget-- > 0) {
            Block block = world.getBlockAt(x, y, z);
            Material type = block.getType();
            if (saveAir || !type.isAir()) {
                blocks.put(Arena.key(x - minX, y - minY, z - minZ), block.getBlockData().getAsString());
            }
            if (!advance()) {
                cancel();
                onComplete.accept(new Arena(name, world.getName(), minX, minY, minZ, maxX, maxY, maxZ, blocks));
                return;
            }
        }
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
