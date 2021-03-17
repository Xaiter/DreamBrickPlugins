package dev.xaiter.spigot.dreambrickspawncommand;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class GetBedLocCmd implements Listener, CommandExecutor {

    private final String PERMISSIONS_GETBEDLOC = "dreambrickspawncommand.getbedloc";

    public GetBedLocCmd(JavaPlugin plugin) {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Permissions check!
        if (!sender.hasPermission(PERMISSIONS_GETBEDLOC)) {
            return false;
        }

        if (args.length != 1) {
            return false;
        }

        Location bedLoc = null;
        Server s = Bukkit.getServer();
        Player onlinePlayer = s.getPlayer(args[0]);
        if (onlinePlayer != null)
            bedLoc = onlinePlayer.getBedSpawnLocation();
        else {
            sender.sendMessage("Player could not be located.  Try again when they are online.");
            return true;
        }

        if (bedLoc != null)
            sender.sendMessage("X: " + bedLoc.getBlockX() + " / Y: " + bedLoc.getBlockY() + " / Z: " + bedLoc.getBlockZ());
        else
            sender.sendMessage("No bed located.");

        return true;
    }
}