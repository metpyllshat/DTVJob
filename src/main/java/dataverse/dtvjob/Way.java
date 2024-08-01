package dataverse.dtvjob;

import java.util.Iterator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

public final class Way implements Iterable<Block> {
    private final String worldName;

    private final Vector start;

    private final Vector end;

    private final Vector direction;

    private final int distance;

    private final double offsetY;

    public Way(Location start, Location end, double offsetY) {
        if (!start.getWorld().getName().equals(end.getWorld().getName()))
            throw new IllegalArgumentException("Cannot create a block line between two different worlds");
        this.worldName = start.getWorld().getName();
        this.start = start.toVector();
        this.end = end.toVector();
        this.direction = this.end.subtract(this.start);
        this.distance = (int)start.distance(end);
        this.offsetY = offsetY;
    }

    public Way(Location start, Location end) {
        this(start, end, 0.0D);
    }

    public Way(LivingEntity start, Location end) {
        this(start.getLocation(), end, start.getEyeHeight());
    }

    public String getWorldName() {
        return this.worldName;
    }

    public World getWorld() throws IllegalStateException {
        World w = Bukkit.getWorld(this.worldName);
        if (w == null)
            throw new IllegalStateException("World '" + this.worldName + "' is not loaded");
        return w;
    }

    public Vector getStart() {
        return this.start;
    }

    public Vector getEnd() {
        return this.end;
    }

    public Vector getDirection() {
        return this.direction;
    }

    public int getDistance() {
        return this.distance;
    }

    public double getOffsetY() {
        return this.offsetY;
    }

    public Iterator<Block> iterator() {
        return (Iterator<Block>)new BlockIterator(getWorld(), this.start, this.direction, this.offsetY, this.distance);
    }
}
