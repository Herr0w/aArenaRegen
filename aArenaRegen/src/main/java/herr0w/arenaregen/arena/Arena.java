package herr0w.arenaregen.arena;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class Arena {
    private final String name;
    private final String worldName;
    private final int minX;
    private final int minY;
    private final int minZ;
    private final int maxX;
    private final int maxY;
    private final int maxZ;
    private final Map<Long, String> savedBlocks;

    public Arena(String name, String worldName, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Map<Long, String> savedBlocks) {
        this.name = name;
        this.worldName = worldName;
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        this.savedBlocks = Collections.unmodifiableMap(new HashMap<>(savedBlocks));
    }

    public String name() {
        return name;
    }

    public String worldName() {
        return worldName;
    }

    public int minX() {
        return minX;
    }

    public int minY() {
        return minY;
    }

    public int minZ() {
        return minZ;
    }

    public int maxX() {
        return maxX;
    }

    public int maxY() {
        return maxY;
    }

    public int maxZ() {
        return maxZ;
    }

    public Map<Long, String> savedBlocks() {
        return savedBlocks;
    }

    public int sizeX() {
        return maxX - minX + 1;
    }

    public int sizeY() {
        return maxY - minY + 1;
    }

    public int sizeZ() {
        return maxZ - minZ + 1;
    }

    public long volume() {
        return (long) sizeX() * sizeY() * sizeZ();
    }

    public static long key(int relX, int relY, int relZ) {
        return (((long) relX) & 0x1FFFFFL) << 43 | (((long) relY) & 0x1FFFFFL) << 22 | (((long) relZ) & 0x3FFFFFL);
    }
}
