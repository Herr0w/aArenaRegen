package herr0w.arenaregen.selection;

import org.bukkit.Location;

public final class Selection {
    private Location pos1;
    private Location pos2;

    public Location pos1() {
        return pos1;
    }

    public Location pos2() {
        return pos2;
    }

    public void pos1(Location pos1) {
        this.pos1 = pos1;
    }

    public void pos2(Location pos2) {
        this.pos2 = pos2;
    }

    public boolean complete() {
        return pos1 != null && pos2 != null && pos1.getWorld() != null && pos1.getWorld().equals(pos2.getWorld());
    }
}
