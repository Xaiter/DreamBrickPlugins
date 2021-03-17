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
import org.bukkit.scoreboard.Team;


public class ReturnHomeCmd implements Listener, CommandExecutor {

    private final String PERMISSIONS_RETURNHOME = "dreambrickspawncommand.returnhome";

    public ReturnHomeCmd(JavaPlugin plugin) {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Permissions check!
        if(!sender.hasPermission(PERMISSIONS_RETURNHOME)) {
            return false;
        }

        if (args.length != 0) {
            return false;
        } 

        // Grab a server ref for sending commands...
        Server s = Bukkit.getServer();

        // Verify the player is actually online...
        String targetPlayerName = sender.getName();;
        Player onlinePlayer = s.getPlayer(targetPlayerName);
        if (onlinePlayer == null) {
            s.getLogger().warning(sender.getName() + " was not online when ReturnHome was executed.");
            return false;
        }
        
        // Verify they're actually in spawn...
        Team spawnTeam = s.getScoreboardManager().getMainScoreboard().getTeam("spawn");
        if(!spawnTeam.hasEntry(onlinePlayer.getName())) {
            s.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + targetPlayerName + " [\"\",{\"text\":\"You must be in spawn to return home.\",\"color\":\"gray\"}]");
            return true;
        }

        // Verify the player actually has a bed, print an error if they don't...
        Location bedLoc = onlinePlayer.getBedSpawnLocation();
        if(bedLoc == null) {
            s.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + targetPlayerName + " [\"\",{\"text\":\"You do not have a bed set.  Please RTP to exit spawn.\",\"color\":\"gray\"}]");
            return true;
        }

        // Otherwise, send them to their bed and set their game mode
        s.dispatchCommand(Bukkit.getConsoleSender(), "execute in minecraft:overworld run tp " + targetPlayerName + " " + bedLoc.getBlockX() + " " + bedLoc.getBlockY() + " " + bedLoc.getBlockZ());
        s.dispatchCommand(Bukkit.getConsoleSender(), "gms " + targetPlayerName);
        s.dispatchCommand(Bukkit.getConsoleSender(), "resetranks " + targetPlayerName);

        // Success!
        return true;
    }
}