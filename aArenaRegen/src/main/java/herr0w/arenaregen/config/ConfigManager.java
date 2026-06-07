package herr0w.arenaregen.config;

import herr0w.arenaregen.ArenaRegenPlugin;

public final class ConfigManager {
    private final ArenaRegenPlugin plugin;
    private int blocksPerTick;
    private int ticksBetweenBatches;
    private int maxRegensAtOnce;
    private boolean saveAirBlocks;
    private boolean usePhysics;
    private boolean debug;

    public ConfigManager(ArenaRegenPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.reloadConfig();
        blocksPerTick = Math.max(1, plugin.getConfig().getInt("settings.blocks-per-tick", 1000));
        ticksBetweenBatches = Math.max(1, plugin.getConfig().getInt("settings.ticks-between-batches", 1));
        maxRegensAtOnce = Math.max(1, plugin.getConfig().getInt("settings.max-regens-at-once", 1));
        saveAirBlocks = plugin.getConfig().getBoolean("settings.save-air-blocks", false);
        usePhysics = plugin.getConfig().getBoolean("settings.use-physics", false);
        debug = plugin.getConfig().getBoolean("settings.debug", false);
    }

    public int blocksPerTick() {
        return blocksPerTick;
    }

    public int ticksBetweenBatches() {
        return ticksBetweenBatches;
    }

    public int maxRegensAtOnce() {
        return maxRegensAtOnce;
    }

    public boolean saveAirBlocks() {
        return saveAirBlocks;
    }

    public boolean usePhysics() {
        return usePhysics;
    }

    public boolean debug() {
        return debug;
    }
}
