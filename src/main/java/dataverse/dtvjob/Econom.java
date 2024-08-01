package dataverse.dtvjob;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class Econom {
    static void withdraw(Player p, int e) {
        DTVJob.economy.withdrawPlayer((OfflinePlayer)p, e);
    }

    static void deposit(Player p, int e) {
        DTVJob.economy.depositPlayer(p, e);
    }

    static double getBalance(Player p) {
        return DTVJob.economy.getBalance((OfflinePlayer)p);
    }
}
