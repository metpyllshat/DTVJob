package dataverse.dtvjob;

import java.util.LinkedList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.entity.Squid;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Truck {
    private Player player;

    private String way;

    private int boxCount;

    private List<Location> boxes = new LinkedList<>();

    private FallingBlock prevBlock = null;

    private Squid prevMob;

    public Truck(Player player, String way, int boxCount) {
        setBox_count(boxCount);
        setWay(way);
        setPlayer(player);
    }

    public void deliver() {
        String id = Ways.getWayConfig().getString(String.valueOf(this.way) + ".block");
        int cost = Ways.getWayConfig().getInt(String.valueOf(this.way) + ".cost");
        Material mat = Material.getMaterial(id);
        int slot = 0;
        for (Location loc : this.boxes) {
            loc.getBlock().setType(mat);
            Econom.deposit(this.player, cost);
            this.player.sendMessage(Lang.get("deliverBox").replaceAll("%money%", Integer.toString(cost)));
            this.player.getInventory().clear(slot);
            slot++;
        }
        this.player.removePotionEffect(PotionEffectType.SLOW);
        if (this.prevBlock != null)
            this.prevBlock.remove();
        if (this.prevMob != null)
            this.prevMob.remove();
        DTVJob.trucks.remove(this.player.getName());
    }

    public int getBox_count() {
        return this.boxCount;
    }

    public void setBox_count(int box_count) {
        this.boxCount = box_count;
    }

    public String getWay() {
        return this.way;
    }

    public void setWay(String way) {
        this.way = way;
    }

    public Player getPlayer() {
        return this.player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public static void startTruck(Player p, String way, Location loc) {
        DTVJob.trucks.put(p.getName(), new Truck(p, way, 0));
        addAnimation(p.getName());
        giveBox(p, way, 0, loc);
    }

    public static void addAnimation(String name) {
        String way = ((Truck)DTVJob.trucks.get(name)).getWay();
        Player pl = ((Truck)DTVJob.trucks.get(name)).getPlayer();
        FallingBlock prevBlock = ((Truck)DTVJob.trucks.get(name)).getPrevBlock();
        if (prevBlock != null)
            prevBlock.remove();
        Squid prevMob = ((Truck)DTVJob.trucks.get(name)).getPrevMob();
        if (prevMob != null)
            prevMob.remove();
        Material mat = Material.getMaterial(Ways.getWayConfig().getString(String.valueOf(way) + ".block"));
        FallingBlock block = pl.getWorld().spawnFallingBlock(new Location(pl.getLocation().getWorld(), pl.getLocation().getX(), pl.getLocation().getY() + 2.0D, pl.getLocation().getZ()), mat, (byte)0);
        block.setVelocity(new Vector(0, 0, 0));
        block.setDropItem(false);
        block.setFallDistance(0.0F);
        block.setGravity(false);
        block.setHurtEntities(false);
        block.setInvulnerable(true);
        Squid mob = (Squid)pl.getWorld().spawnEntity(new Location(pl.getLocation().getWorld(), pl.getLocation().getX(), pl.getLocation().getY() + 2.0D, pl.getLocation().getZ()), EntityType.SQUID);
        mob.setVelocity(new Vector(0, 0, 0));
        mob.setAI(false);
        mob.setCanPickupItems(false);
        mob.setFallDistance(0.0F);
        mob.setGliding(false);
        mob.setGlowing(false);
        mob.setGravity(false);
        mob.setInvulnerable(true);
        mob.setMaxHealth(2048.0D);
        mob.setHealth(2048.0D);
        mob.setSilent(true);
        mob.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 10000000, 10, true));
        mob.setPassenger((Entity)block);
        pl.setPassenger((Entity)mob);
        ((Truck)DTVJob.trucks.get(name)).setPrevBlock(block);
        ((Truck)DTVJob.trucks.get(name)).setPrevMob(mob);
    }

    public int getLongestSegment() {
        int mx = 0;
        List<Location> locations = new LinkedList<>();
        ConfigurationSection cs = Ways.getWayConfig().getConfigurationSection(String.valueOf(this.way) + ".waypoints");
        for (String ky : cs.getKeys(false))
            locations.add(new Location(Bukkit.getWorld(Ways.getWayConfig().getString(String.valueOf(this.way) + ".A.world")), cs.getInt(String.valueOf(ky) + ".x"), cs.getInt(String.valueOf(ky) + ".y"), cs.getInt(String.valueOf(ky) + ".z")));
        for (int ind = 1; ind < locations.size(); ind++) {
            Location a = locations.get(ind - 1);
            Location b = locations.get(ind);
            if (mx < Math.sqrt(((a.getBlockX() - b.getBlockX()) * (a.getBlockX() - b.getBlockX()) + (a.getBlockY() - b.getBlockY()) * (a.getBlockY() - b.getBlockY()) + (a.getBlockZ() - b.getBlockZ()) * (a.getBlockZ() - b.getBlockZ()))))
                mx = (int)Math.sqrt(((a.getBlockX() - b.getBlockX()) * (a.getBlockX() - b.getBlockX()) + (a.getBlockY() - b.getBlockY()) * (a.getBlockY() - b.getBlockY()) + (a.getBlockZ() - b.getBlockZ()) * (a.getBlockZ() - b.getBlockZ())));
        }
        return mx;
    }

    public int getNearestWay(Location a) {
        int mx = 10000000;
        List<Location> locations = new LinkedList<>();
        ConfigurationSection cs = Ways.getWayConfig().getConfigurationSection(String.valueOf(this.way) + ".waypoints");
        for (String ky : cs.getKeys(false))
            locations.add(new Location(Bukkit.getWorld(Ways.getWayConfig().getString(String.valueOf(this.way) + ".A.world")), cs.getInt(String.valueOf(ky) + ".x"), cs.getInt(String.valueOf(ky) + ".y"), cs.getInt(String.valueOf(ky) + ".z")));
        for (int ind = 0; ind < locations.size(); ind++) {
            Location b = locations.get(ind);
            if (mx > Math.sqrt(((a.getBlockX() - b.getBlockX()) * (a.getBlockX() - b.getBlockX()) + (a.getBlockY() - b.getBlockY()) * (a.getBlockY() - b.getBlockY()) + (a.getBlockZ() - b.getBlockZ()) * (a.getBlockZ() - b.getBlockZ()))))
                mx = (int)Math.sqrt(((a.getBlockX() - b.getBlockX()) * (a.getBlockX() - b.getBlockX()) + (a.getBlockY() - b.getBlockY()) * (a.getBlockY() - b.getBlockY()) + (a.getBlockZ() - b.getBlockZ()) * (a.getBlockZ() - b.getBlockZ())));
        }
        return mx;
    }

    public static void giveBox(Player p, String way, int slot, Location loc) {
        ItemStack pr = p.getInventory().getItem(slot);
        ((Truck)DTVJob.trucks.get(p.getName())).setBox_count(((Truck)DTVJob.trucks.get(p.getName())).getBox_count() + 1);
        ((Truck)DTVJob.trucks.get(p.getName())).getBoxes().add(loc);
        ((Truck)DTVJob.trucks.get(p.getName())).getPrevBlock().setCustomName(DTVJob.itemTitle.replace("<number>", Integer.toString(((Truck)DTVJob.trucks.get(p.getName())).getBox_count())));
        ((Truck)DTVJob.trucks.get(p.getName())).getPrevBlock().setCustomNameVisible(true);
        if (DTVJob.slow && ((Truck)DTVJob.trucks.get(p.getName())).getBox_count() > 1) {
            p.removePotionEffect(PotionEffectType.SLOW);
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200000000, ((Truck)DTVJob.trucks.get(p.getName())).getBox_count() - 2));
        }
        String id = Ways.getWayConfig().getString(String.valueOf(way) + ".block");
        ItemStack is = new ItemStack(Material.getMaterial(id));
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(DTVJob.itemName);
        List<String> ls = new LinkedList<>();
        ls.add(DTVJob.itemlore);
        im.setLore(ls);
        is.setItemMeta(im);
        if (pr == null) {
            p.getInventory().setItem(slot, is);
        } else {
            p.getInventory().setItem(slot, is);
            for (int i = slot + 1; i <= 36; i++) {
                if (p.getInventory().getItem(i) == null) {
                    p.getInventory().setItem(i, pr);
                    break;
                }
                if (i == 36)
                    p.getLocation().getWorld().dropItem(p.getLocation(), pr);
            }
        }
    }

    public List<Location> getBoxes() {
        return this.boxes;
    }

    public void setBoxes(List<Location> boxes) {
        this.boxes = boxes;
    }

    public FallingBlock getPrevBlock() {
        return this.prevBlock;
    }

    public void setPrevBlock(FallingBlock prevBlock) {
        this.prevBlock = prevBlock;
    }

    public Squid getPrevMob() {
        return this.prevMob;
    }

    public void setPrevMob(Squid prevMob) {
        this.prevMob = prevMob;
    }
}