package dev.xaiter.spigot.dreambrickspawncommand;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class GetBedLocCmd implements Listener, CommandExecutor {

    private final String PERMISSIONS_GETBEDLOC = "dreambrickspawncommand.getbedloc";
    private final JavaPlugin _plugin;

    public GetBedLocCmd(JavaPlugin plugin) {
        this._plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Permissions check!
        if(!sender.hasPermission(PERMISSIONS_GETBEDLOC)) {
            return false;
        }

        if(args.length != 1) {
            return false;
        }

        Location bedLoc = null;
        Server s = Bukkit.getServer();
        Player onlinePlayer = s.getPlayer(args[0]);
        if(onlinePlayer != null)
            bedLoc = onlinePlayer.getBedSpawnLocation();
        else
        {
            OfflinePlayer p = Bukkit.getServer().getOfflinePlayer(args[0]);
            if(p != null)
                bedLoc = p.getBedSpawnLocation();
            else
            {
                sender.sendMessage("Player could not be located.  Try again when they are online.");
                return true;
            }
        }

        if(bedLoc != null)
            sender.sendMessage("X: " + bedLoc.getBlockX() + " / Y: " + bedLoc.getBlockY() + " / Z: " + bedLoc.getBlockZ());
        else
            sender.sendMessage("No bed located.");

        return true;
    }
}