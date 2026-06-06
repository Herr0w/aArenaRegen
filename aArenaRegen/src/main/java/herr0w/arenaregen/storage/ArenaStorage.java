package herr0w.arenaregen.storage;

import herr0w.arenaregen.ArenaRegenPlugin;
import herr0w.arenaregen.arena.Arena;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class ArenaStorage {
    private static final int MAGIC = 0x4152474E;
    private static final int VERSION = 1;

    private final File arenasFolder;

    public ArenaStorage(ArenaRegenPlugin plugin) {
        this.arenasFolder = new File(plugin.getDataFolder(), "arenas");
        if (!arenasFolder.exists()) {
            arenasFolder.mkdirs();
        }
    }

    public File file(String arenaName) {
        return new File(arenasFolder, arenaName + ".dat");
    }

    public List<String> arenaNames() {
        File[] files = arenasFolder.listFiles((dir, name) -> name.endsWith(".dat"));
        List<String> names = new ArrayList<>();
        if (files == null) {
            return names;
        }
        for (File file : files) {
            names.add(file.getName().substring(0, file.getName().length() - 4));
        }
        return names;
    }

    public boolean delete(String arenaName) {
        File file = file(arenaName);
        return !file.exists() || file.delete();
    }

    public void save(Arena arena) throws IOException {
        Map<String, Integer> paletteIndexes = new HashMap<>();
        List<String> palette = new ArrayList<>();
        for (String data : arena.savedBlocks().values()) {
            if (!paletteIndexes.containsKey(data)) {
                paletteIndexes.put(data, palette.size());
                palette.add(data);
            }
        }

        try (DataOutputStream out = new DataOutputStream(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(file(arena.name())))))) {
            out.writeInt(MAGIC);
            out.writeInt(VERSION);
            out.writeUTF(arena.name());
            out.writeUTF(arena.worldName());
            out.writeInt(arena.minX());
            out.writeInt(arena.minY());
            out.writeInt(arena.minZ());
            out.writeInt(arena.maxX());
            out.writeInt(arena.maxY());
            out.writeInt(arena.maxZ());
            out.writeInt(palette.size());
            for (String value : palette) {
                out.writeUTF(value);
            }
            out.writeInt(arena.savedBlocks().size());
            for (Map.Entry<Long, String> entry : arena.savedBlocks().entrySet()) {
                long key = entry.getKey();
                int relX = (int) ((key >>> 43) & 0x1FFFFF);
                int relY = (int) ((key >>> 22) & 0x1FFFFF);
                int relZ = (int) (key & 0x3FFFFF);
                out.writeInt(relX);
                out.writeInt(relY);
                out.writeInt(relZ);
                out.writeInt(paletteIndexes.get(entry.getValue()));
            }
        }
    }

    public Arena load(File file) throws IOException {
        try (DataInputStream in = new DataInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(file))))) {
            int magic = in.readInt();
            int version = in.readInt();
            if (magic != MAGIC || version != VERSION) {
                throw new IOException("Unsupported arena file format: " + file.getName());
            }

            String name = in.readUTF();
            String world = in.readUTF();
            int minX = in.readInt();
            int minY = in.readInt();
            int minZ = in.readInt();
            int maxX = in.readInt();
            int maxY = in.readInt();
            int maxZ = in.readInt();

            int paletteSize = in.readInt();
            List<String> palette = new ArrayList<>(paletteSize);
            for (int i = 0; i < paletteSize; i++) {
                palette.add(in.readUTF());
            }

            int blockCount = in.readInt();
            Map<Long, String> blocks = new HashMap<>(Math.max(16, blockCount));
            for (int i = 0; i < blockCount; i++) {
                int relX = in.readInt();
                int relY = in.readInt();
                int relZ = in.readInt();
                int paletteIndex = in.readInt();
                blocks.put(Arena.key(relX, relY, relZ), palette.get(paletteIndex));
            }
            return new Arena(name, world, minX, minY, minZ, maxX, maxY, maxZ, blocks);
        } catch (EOFException exception) {
            throw new IOException("Truncated arena file: " + file.getName(), exception);
        }
    }

    public List<Arena> loadAll() throws IOException {
        List<Arena> arenas = new ArrayList<>();
        File[] files = arenasFolder.listFiles((dir, name) -> name.endsWith(".dat"));
        if (files == null) {
            return arenas;
        }
        for (File file : files) {
            arenas.add(load(file));
        }
        return arenas;
    }
}
