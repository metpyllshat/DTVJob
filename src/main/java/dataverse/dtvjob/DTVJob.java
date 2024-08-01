package dataverse.dtvjob;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.entity.Squid;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class DTVJob extends JavaPlugin {
    private static DTVJob instance;

    public Lang lang;

    public static DTVJob getInstance() {
        return instance;
    }

    public static Economy economy = null;

    public static int mxBoxCount;

    public static int range;

    public static String itemlore;

    public static String itemName;

    public static String itemTitle;

    public static long serverTime;

    public static boolean slow;

    public static HashMap<String, Truck> trucks = new HashMap<>();

    public static HashMap<String, Long> event1 = new HashMap<>();

    public static HashMap<String, Long> event2 = new HashMap<>();

    public void onEnable() {
        loadPlugin();
    }

    public void onDisable() {
        backAllBoxes();
        disablePlugin();
    }

    public void disablePlugin() {
        getLogger().info("DTVJob Disabled!");
        getPluginLoader().disablePlugin((Plugin)this);
    }

    void loadPlugin() {
        instance = this;
        getServer().getPluginManager().registerEvents(new EventListener(this), (Plugin)this);
        getConfig().options().copyDefaults(true);
        saveConfig();
        Ways.load();
        this.lang = new Lang(this);
        this.lang.initPhrases();
        if (!(new File(getDataFolder(), "/lang/ru.yml")).exists()) {
            this.lang.saveNewLang();
        } else {
            this.lang.loadLang();
        }
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            event1.put(player.getName(), Long.valueOf(-1L));
            event2.put(player.getName(), Long.valueOf(-1L));
        }
        mxBoxCount = getConfig().getInt("max_box_count");
        itemlore = getConfig().getString("item-lore");
        itemName = getConfig().getString("item-name");
        itemTitle = getConfig().getString("title");
        slow = getConfig().getBoolean("slow");
        range = getConfig().getInt("range");
        setupEconomy();
        startParticles();
        startAnimation();
        startServerTimer();
        startOutOfWayCheck();
        getLogger().info("§aDTVJob Enabled!");
        getLogger().info("§eSpecial for EbanataLandya");
        getLogger().info("§eBy DataVerse Team <3");
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("DTVJob")) {
            if (!(sender instanceof Player))
                return true;
            Player p = (Player)sender;
            if (args.length == 0 || !p.isOp()) {
                viewInfo(p);
                return true;
            }
            if (args[0].equalsIgnoreCase("disable") && p.isOp()) {
                disablePlugin();
                return true;
            }
            if (args[0].equalsIgnoreCase("createway")) {
                if (args.length != 3) {
                    p.sendMessage("§ecreateway <имя пути> <заработок>");
                    return true;
                }
                Ways.createWay(args[1], Integer.parseInt(args[2]));
                p.sendMessage("§aПуть создан");
            }
            if (args[0].equalsIgnoreCase("setpoint1")) {
                if (args.length != 2) {
                    p.sendMessage("§esetpoint1 <имя пути>");
                    return true;
                }
                if (!Ways.isWay(args[1])) {
                    p.sendMessage("§cПуть не найден");
                    return true;
                }
                Block b = p.getTargetBlock(null, 200);
                if (!b.getType().equals(Material.OAK_SIGN) && !b.getType().equals(Material.OAK_WALL_SIGN)) {
                    p.sendMessage("§cВы должны смотреть на табличку");
                    return true;
                }
                changeWay(args[1], 1, b.getLocation());
                p.sendMessage("§aТочка 1 установлена!");
            }
            if (args[0].equalsIgnoreCase("setpoint2")) {
                if (args.length != 2) {
                    p.sendMessage("§esetpoint2 <имя пути>");
                    return true;
                }
                if (!Ways.isWay(args[1])) {
                    p.sendMessage("§cПуть не найден");
                    return true;
                }
                Block b = p.getTargetBlock(null, 200);
                if (!b.getType().equals(Material.OAK_SIGN) && !b.getType().equals(Material.OAK_WALL_SIGN)) {
                    p.sendMessage("§cВы должны смотреть на табличку");
                    return true;
                }
                changeWay(args[1], 2, b.getLocation());
                p.sendMessage("§aТочка 2 установлена");
            }
            if (args[0].equalsIgnoreCase("setwaypoint")) {
                if (args.length != 3) {
                    p.sendMessage("§esetwaypoint <имя пути> <a/b>");
                    return true;
                }
                if (!Ways.isWay(args[1])) {
                    p.sendMessage("§cПуть не найден");
                    return true;
                }
                Location loc = p.getLocation();
                Ways.getWayConfig().set(String.valueOf(args[1]) + ".waypoints." + args[2] + ".x", Integer.valueOf(loc.getBlockX()));
                Ways.getWayConfig().set(String.valueOf(args[1]) + ".waypoints." + args[2] + ".y", Integer.valueOf(loc.getBlockY()));
                Ways.getWayConfig().set(String.valueOf(args[1]) + ".waypoints." + args[2] + ".z", Integer.valueOf(loc.getBlockZ() + 1));
                Ways.saveWays();
                p.sendMessage("§aТочка добавлена");
                return true;
            }
        }
        return true;
    }

    public static void changeWay(String w, int pos, Location loc) {
        Ways.renew(w, pos, loc);
    }

    public void viewInfo(Player p) {
        p.sendMessage("§e/dtvjob createway <название пути> <плата за блок>");
        p.sendMessage("§e/dtvjob setpoint1 <название пути> (смотря на табличку)");
        p.sendMessage("§e/dtvjob setpoint2 <название пути> (смотря на табличку)");
        p.sendMessage("§e/dtvjob setwaypoint <название пути> <точка a/b>");
        p.sendMessage("§eSpecial for EbanataLandya");
        p.sendMessage("§eby DataVerse Team <3");
    }

    public void startParticles() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask((Plugin)this, new Runnable() {
            public void run() {
                for (String name : DTVJob.trucks.keySet()) {
                    String way = ((Truck)DTVJob.trucks.get(name)).getWay();
                    Player pl = ((Truck)DTVJob.trucks.get(name)).getPlayer();
                    World world = Bukkit.getWorld(Ways.getWayConfig().getString(String.valueOf(way) + ".A.world"));
                    ConfigurationSection cs = Ways.getWayConfig().getConfigurationSection(String.valueOf(way) + ".waypoints");
                    if (cs.getKeys(false).size() < 2)
                        continue;
                    List<Location> locations = new LinkedList<>();
                    for (String ky : cs.getKeys(false))
                        locations.add(new Location(world, cs.getInt(String.valueOf(ky) + ".x"), cs.getInt(String.valueOf(ky) + ".y"), cs.getInt(String.valueOf(ky) + ".z")));
                    for (int ind = 1; ind < locations.size(); ind++) {
                        Location a = locations.get(ind - 1);
                        Location b = locations.get(ind);
                        Way w = new Way(a, b);
                        Iterator<Block> it = w.iterator();
                        while (it.hasNext()) {
                            Block block = it.next();
                            pl.playEffect(block.getLocation(), Effect.ENDER_EYE_PLACED, 5);
                        }
                    }
                }
            }
        },0L, 5L);
    }

    public void startServerTimer() {
        serverTime = 0L;
        Bukkit.getScheduler().scheduleSyncRepeatingTask((Plugin)this, new Runnable() {
            public void run() {
                DTVJob.serverTime++;
            }
        },  0L, 1L);
    }

    public void startAnimation() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask((Plugin)this, new Runnable() {
            public void run() {
                for (String name : DTVJob.trucks.keySet()) {
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
                    block.setCustomName(DTVJob.itemTitle.replace("<number>", Integer.toString(((Truck)DTVJob.trucks.get(name)).getBox_count())));
                    block.setCustomNameVisible(true);
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
            }
        },0L, 300L);
    }

    public void startOutOfWayCheck() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask((Plugin)this, new Runnable() {
            public void run() {
                for (String name : DTVJob.trucks.keySet()) {
                    String way = ((Truck)DTVJob.trucks.get(name)).getWay();
                    Player pl = ((Truck)DTVJob.trucks.get(name)).getPlayer();
                    pl.updateInventory();
                    int longestSegment = ((Truck)DTVJob.trucks.get(name)).getLongestSegment();
                    int longestWay = ((Truck)DTVJob.trucks.get(name)).getNearestWay(pl.getLocation());
                    if (longestWay > longestSegment * 2) {
                        pl.sendMessage(Lang.get("soFar"));
                        DTVJob.abortTruck(name);
                    }
                }
            }
        },  0L, 20L);
    }

    public static boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null)
            return false;
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null)
            return false;
        economy = (Economy)rsp.getProvider();
        return (economy != null);
    }

    public static void backAllBoxes() {
        for (String name : trucks.keySet())
            abortTruck(name);
    }

    public static void abortTruck(String name) {
        String way = ((Truck)trucks.get(name)).getWay();
        Player player = Bukkit.getPlayer(name);
        player.removePotionEffect(PotionEffectType.SLOW);
        String id = "BRICKS";
        Material mat = Material.getMaterial(id);
        FallingBlock block = ((Truck)trucks.get(name)).getPrevBlock();
        if (block != null)
            block.remove();
        Squid prevMob = ((Truck)trucks.get(name)).getPrevMob();
        if (prevMob != null)
            prevMob.remove();
        int slot = 0;
        for (Location loc : ((Truck)trucks.get(name)).getBoxes()) {
            loc.getBlock().setType(mat);
            player.getInventory().clear(slot);
            slot++;
        }
        trucks.remove(player.getName());
    }
}