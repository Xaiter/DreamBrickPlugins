package dev.xaiter.spigot.dreambrickspawncommand;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

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