package dev.xaiter.spigot.dreambrickspawncommand;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;

public class OpCommandFilter implements Listener {

    private App _plugin;

    private List<String> _allowedCommands;

    public OpCommandFilter(App plugin) {
        this._plugin = plugin;
        this._plugin.getDataFolder();

        _allowedCommands = LoadConfig();
    }

    private List<String> LoadConfig() {
        File configFile = new File(this._plugin.getDataFolder().toString() + File.separator + "config.txt");

        try {
            // Make sure the file exists if it doesn't... and return nothing.
            if (!configFile.exists()) {
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();
                return new ArrayList<String>();
            }

            // Otherwise, return all the lines!
            return Files.readAllLines(configFile.toPath());
        } catch (IOException e) {
            return new ArrayList<String>();
        }
    }

    @EventHandler
    public void onPlayerTab(PlayerCommandSendEvent e) {
        // You're an OP?  Don't need to inspect, firehose of commands
        if(e.getPlayer().isOp()) {
            return;
        }

        // Remove anything that's not explicitly allowed for non-ops so they aren't cluttered with garbage
        e.getCommands().retainAll(this._allowedCommands);
	}

}