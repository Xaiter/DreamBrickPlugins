package dev.xaiter.spigot.dreambrickspawncommand;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;


public class BalanceCmd implements Listener, CommandExecutor {

    private final String PERMISSIONS_BALANCE = "dreambrickspawncommand.balance";

    public BalanceCmd(JavaPlugin plugin) {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Permissions check!
        if(!sender.hasPermission(PERMISSIONS_BALANCE)) {
            return false;
        }

        Server s = Bukkit.getServer();
        s.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + sender.getName() + " [\"\",{\"text\":\"You have \",\"color\":\"gray\"},{\"score\":{\"name\":\"" + sender.getName() + "\",\"objective\":\"balance\"},\"color\":\"gold\",\"bold\":true},{\"text\":\" flint.\",\"color\":\"gray\",\"bold\":false}]");
        return true;
    }
}