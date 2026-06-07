package herr0w.arenaregen;

import herr0w.arenaregen.arena.ArenaManager;
import herr0w.arenaregen.command.CommandManager;
import herr0w.arenaregen.config.ConfigManager;
import herr0w.arenaregen.config.MessageManager;
import herr0w.arenaregen.selection.SelectionManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class ArenaRegenPlugin extends JavaPlugin {
    private ConfigManager configManager;
    private MessageManager messageManager;
    private SelectionManager selectionManager;
    private ArenaManager arenaManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("messages.yml", false);

        this.configManager = new ConfigManager(this);
        this.messageManager = new MessageManager(this);
        this.selectionManager = new SelectionManager(this);
        this.arenaManager = new ArenaManager(this);

        configManager.reload();
        messageManager.reload();
        arenaManager.loadArenas();
        arenaManager.startAutoRegens();

        getServer().getPluginManager().registerEvents(selectionManager, this);
        getServer().getPluginManager().registerEvents(arenaManager, this);
        CommandManager commandManager = new CommandManager(this);
        PluginCommand command = getCommand("arenargen");
        if (command != null) {
            command.setExecutor(commandManager);
            command.setTabCompleter(commandManager);
        }
    }

    @Override
    public void onDisable() {
        if (arenaManager != null) {
            arenaManager.shutdown();
        }
    }

    public ConfigManager configManager() {
        return configManager;
    }

    public MessageManager messageManager() {
        return messageManager;
    }

    public SelectionManager selectionManager() {
        return selectionManager;
    }

    public ArenaManager arenaManager() {
        return arenaManager;
    }
}
