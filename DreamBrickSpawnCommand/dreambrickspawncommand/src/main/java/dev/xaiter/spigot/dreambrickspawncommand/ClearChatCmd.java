package dev.xaiter.spigot.dreambrickspawncommand;

import java.util.Collection;

import org.bukkit.Bukkit;
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

public class ClearChatCmd implements Listener, CommandExecutor {

    private final String PERMISSIONS_CLEARCHAT = "dreambrickspawncommand.clearchat";
    private final JavaPlugin _plugin;

    public ClearChatCmd(JavaPlugin plugin) {
        this._plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Permissions check!
        if(!sender.hasPermission(PERMISSIONS_CLEARCHAT)) {
            return false;
        }

        // FLUSH
        Server s = Bukkit.getServer();
        for(Integer i = 0; i < 100; i++) {
            s.broadcastMessage("");
        }

        // Credit the clearer
        s.broadcastMessage("Chat cleared by " + sender.getName());

        return true;
    }
}