package herr0w.arenaregen.selection;

import herr0w.arenaregen.ArenaRegenPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public final class SelectionManager implements Listener {
    private final ArenaRegenPlugin plugin;
    private final NamespacedKey wandKey;
    private final Map<UUID, Selection> selections = new HashMap<>();

    public SelectionManager(ArenaRegenPlugin plugin) {
        this.plugin = plugin;
        this.wandKey = new NamespacedKey(plugin, "selection_wand");
    }

    public Selection selection(Player player) {
        return selections.computeIfAbsent(player.getUniqueId(), ignored -> new Selection());
    }

    public ItemStack createWand() {
        ItemStack item = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§aArenaRegen Wand");
        meta.getPersistentDataContainer().set(wandKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public boolean isWand(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(wandKey, PersistentDataType.BYTE);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || !isWand(event.getItem()) || event.getClickedBlock() == null) {
            return;
        }

        Action action = event.getAction();
        if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        event.setCancelled(true);
        Player player = event.getPlayer();
        Location location = event.getClickedBlock().getLocation();
        Selection selection = selection(player);
        Map<String, String> placeholders = Map.of(
                "x", Integer.toString(location.getBlockX()),
                "y", Integer.toString(location.getBlockY()),
                "z", Integer.toString(location.getBlockZ())
        );

        if (action == Action.LEFT_CLICK_BLOCK) {
            selection.pos1(location);
            plugin.messageManager().send(player, "pos1-set", placeholders);
        } else {
            selection.pos2(location);
            plugin.messageManager().send(player, "pos2-set", placeholders);
        }
    }
}
