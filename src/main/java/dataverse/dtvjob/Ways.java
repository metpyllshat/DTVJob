package dataverse.dtvjob;

import java.io.File;
import java.io.IOException;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Ways {
    private static File wayConfigF;

    private static FileConfiguration wayConfig;

    public static void load() {
        createFile();
    }

    public static void renew(String w, int pos, Location loc) {
        String world = loc.getWorld().getName();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        if (pos == 1) {
            getWayConfig().set(String.valueOf(w) + ".A.world", world);
            getWayConfig().set(String.valueOf(w) + ".A.x", Integer.valueOf(x));
            getWayConfig().set(String.valueOf(w) + ".A.y", Integer.valueOf(y));
            getWayConfig().set(String.valueOf(w) + ".A.z", Integer.valueOf(z));
        }
        if (pos == 2) {
            getWayConfig().set(String.valueOf(w) + ".B.world", world);
            getWayConfig().set(String.valueOf(w) + ".B.x", Integer.valueOf(x));
            getWayConfig().set(String.valueOf(w) + ".B.y", Integer.valueOf(y));
            getWayConfig().set(String.valueOf(w) + ".B.z", Integer.valueOf(z));
        }
        saveWays();
    }

    public static int isWayExist(Location loc) {
        String world = loc.getWorld().getName().toLowerCase();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        for (String csName : getWayConfig().getKeys(false)) {
            ConfigurationSection cs = getWayConfig().getConfigurationSection(csName);
            if (!cs.getString("A.world").equalsIgnoreCase(world))
                continue;
            int state1 = 0, state2 = 0;
            if (cs.getInt("A.x") == x && cs.getInt("A.y") == y && cs.getInt("A.z") == z)
                state1++;
            if (state1 > 0 &&
                    cs.getInt("B.x") != -1 && cs.getInt("B.y") != -1 && cs.getInt("B.z") != -1)
                state1++;
            if (cs.getInt("B.x") == x && cs.getInt("B.y") == y && cs.getInt("B.z") == z)
                state2++;
            if (state2 > 0 &&
                    cs.getInt("A.x") != -1 && cs.getInt("A.y") != -1 && cs.getInt("A.z") != -1)
                state2++;
            int state = Math.max(state1, state2);
            if (state != 0)
                return state;
        }
        return 0;
    }

    public static String getWay(Location loc) {
        String world = loc.getWorld().getName().toLowerCase();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        for (String csName : getWayConfig().getKeys(false)) {
            ConfigurationSection cs = getWayConfig().getConfigurationSection(csName);
            if (!cs.getString("A.world").equalsIgnoreCase(world))
                continue;
            if (cs.getInt("A.x") == x && cs.getInt("A.y") == y && cs.getInt("A.z") == z)
                return csName;
            if (cs.getInt("B.x") == x && cs.getInt("B.y") == y && cs.getInt("B.z") == z)
                return csName;
        }
        return "none";
    }

    public static int getSignType(Location loc) {
        int state = 0;
        String world = loc.getWorld().getName().toLowerCase();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        for (String csName : getWayConfig().getKeys(false)) {
            ConfigurationSection cs = getWayConfig().getConfigurationSection(csName);
            if (!cs.getString("A.world").equalsIgnoreCase(world))
                continue;
            if (cs.getInt("A.x") == x && cs.getInt("A.y") == y && cs.getInt("A.z") == z)
                return 1;
            if (cs.getInt("B.x") == x && cs.getInt("B.y") == y && cs.getInt("B.z") == z)
                return 2;
            if (state != 0)
                return state;
        }
        return state;
    }

    public static void createWay(String name, int cost) {
        getWayConfig().set(String.valueOf(name) + ".cost", Integer.valueOf(cost));
        getWayConfig().set(String.valueOf(name) + ".block", "BRICKS");
        getWayConfig().set(String.valueOf(name) + ".A.x", Integer.valueOf(-1));
        getWayConfig().set(String.valueOf(name) + ".A.y", Integer.valueOf(-1));
        getWayConfig().set(String.valueOf(name) + ".A.z", Integer.valueOf(-1));
        getWayConfig().set(String.valueOf(name) + ".A.world", "World");
        getWayConfig().set(String.valueOf(name) + ".B.x", Integer.valueOf(-1));
        getWayConfig().set(String.valueOf(name) + ".B.y", Integer.valueOf(-1));
        getWayConfig().set(String.valueOf(name) + ".B.z", Integer.valueOf(-1));
        getWayConfig().createSection(String.valueOf(name) + ".waypoints");
        getWayConfig().set(String.valueOf(name) + ".B.world", "World");
        saveWays();
    }

    public static boolean isWay(String way) {
        return getWayConfig().getKeys(false).contains(way);
    }

    public static void saveWays() {
        try {
            wayConfig.save(wayConfigF);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static FileConfiguration getWayConfig() {
        return wayConfig;
    }

    private static void createFile() {
        wayConfigF = new File(DTVJob.getInstance().getDataFolder(), "ways.yml");
        if (!wayConfigF.exists()) {
            wayConfigF.getParentFile().mkdirs();
            DTVJob.getInstance().saveResource("ways.yml", false);
        }
        wayConfig = (FileConfiguration)new YamlConfiguration();
        try {
            try {
                wayConfig.load(wayConfigF);
            } catch (InvalidConfigurationException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
