package dataverse.dtvjob;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class EventListener implements Listener {
    private DTVJob plugin;

    public EventListener(DTVJob plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClickSign(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        if (!e.getClickedBlock().getType().equals(Material.OAK_SIGN) && !e.getClickedBlock().getType().equals(Material.OAK_WALL_SIGN))
            return;
        Block b = e.getClickedBlock();
        Player p = e.getPlayer();
        if (Math.abs(((Long)DTVJob.event2.get(p.getName())).longValue() - DTVJob.serverTime) < 2L)
            return;
        DTVJob.event2.put(p.getName(), Long.valueOf(DTVJob.serverTime));
        int stat = Ways.isWayExist(b.getLocation());
        if (stat == 0)
            return;
        if (stat == 1) {
            p.sendMessage("Путь не завершен!");
            return;
        }
        int type = Ways.getSignType(b.getLocation());
        if (type == 1) {
            List<String> lst = DTVJob.getInstance().getConfig().getStringList("sign-info");
            for (String text : lst)
                p.sendMessage(text);
            return;
        }
        if (type == 2) {
            if (!DTVJob.trucks.keySet().contains(p.getName())) {
                p.sendMessage(Lang.get("noBoxes"));
                return;
            }
            String way = Ways.getWay(b.getLocation());
            if (!way.equals(((Truck)DTVJob.trucks.get(p.getName())).getWay())) {
                p.sendMessage(Lang.get("anotherWay"));
                return;
            }
            ((Truck)DTVJob.trucks.get(p.getName())).deliver();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClickSign2(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        if (e.getRightClicked().getType() != EntityType.SQUID)
            return;
        Block block = p.getTargetBlock(null, 200);
        if (!block.getType().equals(Material.OAK_SIGN) && !block.getType().equals(Material.OAK_WALL_SIGN))
            return;
        if (Math.abs(((Long)DTVJob.event2.get(p.getName())).longValue() - DTVJob.serverTime) < 2L)
            return;
        DTVJob.event2.put(p.getName(), Long.valueOf(DTVJob.serverTime));
        Block b = block;
        int stat = Ways.isWayExist(b.getLocation());
        if (stat == 0)
            return;
        if (stat == 1) {
            p.sendMessage("Путь не завершен!");
            return;
        }
        int type = Ways.getSignType(b.getLocation());
        if (type == 1) {
            List<String> lst = DTVJob.getInstance().getConfig().getStringList("sign-info");
            for (String text : lst)
                p.sendMessage(text);
            return;
        }
        if (type == 2) {
            if (!DTVJob.trucks.keySet().contains(p.getName())) {
                p.sendMessage(Lang.get("noBoxes"));
                return;
            }
            String way = Ways.getWay(b.getLocation());
            if (!way.equals(((Truck)DTVJob.trucks.get(p.getName())).getWay())) {
                p.sendMessage(Lang.get("anotherWay"));
                return;
            }
            ((Truck)DTVJob.trucks.get(p.getName())).deliver();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClickBlock(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        if (e.getRightClicked().getType() != EntityType.SQUID)
            return;
        if (Math.abs(((Long)DTVJob.event1.get(p.getName())).longValue() - DTVJob.serverTime) < 2L)
            return;
        DTVJob.event1.put(p.getName(), Long.valueOf(DTVJob.serverTime));
        for (String csName : Ways.getWayConfig().getKeys(false)) {
            ConfigurationSection cs = Ways.getWayConfig().getConfigurationSection(csName);
            Block block = p.getTargetBlock(null, 200);
            int id = cs.getInt("block");
            String blockType = cs.getString("block");
            if (!blockType.equals(block.getType().toString())) {
                continue;
            }
            int x = cs.getInt("A.x");
            int y = cs.getInt("A.y");
            int z = cs.getInt("A.z");
            int bx = block.getX();
            int by = block.getY();
            int bz = block.getZ();
            int length = (int)Math.sqrt(((x - bx) * (x - bx) + (y - by) * (y - by) + (z - bz) * (z - bz)));
            if (length > DTVJob.range)
                continue;
            if (DTVJob.trucks.keySet().contains(p.getName())) {
                if (((Truck)DTVJob.trucks.get(p.getName())).getBox_count() == DTVJob.mxBoxCount) {
                    p.sendMessage(Lang.get("boxLimit"));
                    return;
                }
                if (!csName.equals(((Truck)DTVJob.trucks.get(p.getName())).getWay())) {
                    p.sendMessage(Lang.get("anotherBox"));
                    return;
                }
                Truck.giveBox(p, csName, ((Truck)DTVJob.trucks.get(p.getName())).getBox_count(), block.getLocation());
                block.getLocation().getBlock().setType(Material.AIR);
                e.setCancelled(true);
                continue;
            }
            p.sendMessage(Lang.get("startTruck"));
            Truck.startTruck(p, csName, block.getLocation());
            block.getLocation().getBlock().setType(Material.AIR);
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClickBlock2(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        if (Math.abs(((Long)DTVJob.event1.get(p.getName())).longValue() - DTVJob.serverTime) < 2L)
            return;
        DTVJob.event1.put(p.getName(), Long.valueOf(DTVJob.serverTime));
        for (String csName : Ways.getWayConfig().getKeys(false)) {
            ConfigurationSection cs = Ways.getWayConfig().getConfigurationSection(csName);
            Block block = e.getClickedBlock();
            String blockType = cs.getString("block");
            if (!blockType.equals(block.getType().toString())) {
                continue;
            }
            int x = cs.getInt("A.x");
            int y = cs.getInt("A.y");
            int z = cs.getInt("A.z");
            int bx = block.getX();
            int by = block.getY();
            int bz = block.getZ();
            int length = (int)Math.sqrt(((x - bx) * (x - bx) + (y - by) * (y - by) + (z - bz) * (z - bz)));
            if (length > DTVJob.range)
                continue;
            if (DTVJob.trucks.keySet().contains(p.getName())) {
                if (((Truck)DTVJob.trucks.get(p.getName())).getBox_count() == DTVJob.mxBoxCount) {
                    p.sendMessage(Lang.get("boxLimit"));
                    return;
                }
                if (!csName.equals(((Truck)DTVJob.trucks.get(p.getName())).getWay())) {
                    p.sendMessage(Lang.get("anotherBox"));
                    return;
                }
                Truck.giveBox(p, csName, ((Truck)DTVJob.trucks.get(p.getName())).getBox_count(), block.getLocation());
                block.getLocation().getBlock().setType(Material.AIR);
                e.setCancelled(true);
                continue;
            }
            p.sendMessage(Lang.get("startTruck"));
            Truck.startTruck(p, csName, block.getLocation());
            block.getLocation().getBlock().setType(Material.AIR);
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (!DTVJob.trucks.containsKey(p.getName()))
            return;
        DTVJob.event1.remove(p.getName());
        DTVJob.event2.remove(p.getName());
        DTVJob.abortTruck(p.getName());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        DTVJob.event1.put(p.getName(), Long.valueOf(-1L));
        DTVJob.event2.put(p.getName(), Long.valueOf(-1L));
    }

    @EventHandler
    public void onInventoryClick1(InventoryInteractEvent e) {
        Player p = (Player)e.getWhoClicked();
        if (!DTVJob.trucks.containsKey(p.getName()))
            return;
        e.setCancelled(true);
        p.updateInventory();
    }

    @EventHandler
    public void onInventoryClick2(InventoryDragEvent e) {
        Player p = (Player)e.getWhoClicked();
        if (!DTVJob.trucks.containsKey(p.getName()))
            return;
        e.setCancelled(true);
        p.updateInventory();
    }

    @EventHandler
    public void onInventoryClick3(InventoryClickEvent e) {
        Player p = (Player)e.getWhoClicked();
        if (!DTVJob.trucks.containsKey(p.getName()))
            return;
        e.setCancelled(true);
        p.sendMessage(Lang.get("clickEnventory"));
        p.updateInventory();
    }

    public boolean equalsMeta(ItemStack is) {
        if (!is.hasItemMeta())
            return false;
        return (is.getItemMeta().getDisplayName().equals(DTVJob.itemName) && ((String)is.getItemMeta().getLore().get(0)).equals(DTVJob.itemlore));
    }

    @EventHandler
    public void onMilk(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!DTVJob.trucks.containsKey(p.getName()))
            return;
        if (p.getItemInHand().getType().equals(Material.MILK_BUCKET))
            p.getInventory().setItem(p.getInventory().getHeldItemSlot(), new ItemStack(Material.AIR));
    }

    @EventHandler
    public void onDie(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if (!DTVJob.trucks.containsKey(p.getName()))
            return;
        Player player = e.getEntity();
        Material mat = Material.getMaterial("BRICKS");
        for (int ind = 0; ind < e.getDrops().size(); ind++) {
            ItemStack is = e.getDrops().get(ind);
            if (is.getType().equals(mat) && equalsMeta(is)) {
                e.getDrops().remove(ind);
                ind--;
            }
        }
        DTVJob.abortTruck(player.getName());
        p.sendMessage(Lang.get("death"));
    }

    @EventHandler
    public void onInventoryClick2(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        if (!DTVJob.trucks.containsKey(p.getName()))
            return;
        Material mat = Material.getMaterial(Ways.getWayConfig().getString(String.valueOf(((Truck)DTVJob.trucks.get(p.getName())).getWay()) + ".block"));
        if (e.getItemDrop().getItemStack().getType().equals(mat) && equalsMeta(e.getItemDrop().getItemStack()))
            e.setCancelled(true);
    }
}
