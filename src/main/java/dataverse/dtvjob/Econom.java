package dataverse.dtvjob;

import org.bukkit.entity.Player;

public class Econom {

    static void deposit(Player p, int e) {
        DTVJob.economy.depositPlayer(p, e);
    }

}
