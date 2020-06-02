package dev.xaiter.spigot.dreambrickspawncommand;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;

public class OpCommandFilter implements Listener {

    private App _plugin;

    private List<String> _defaultConfig;
    private List<String> _helperConfig;
    private List<String> _modConfig;

    public OpCommandFilter(App plugin) {
        this._plugin = plugin;
        LoadConfigs();
    }

    private void LoadConfigs() {
        // Prepare the file paths...
        File defaultConfigFile = new File(this._plugin.getDataFolder().toString() + File.separator + "config.txt");
        File helperConfigFile = new File(this._plugin.getDataFolder().toString() + File.separator + "helper-config.txt");
        File modConfigFile = new File(this._plugin.getDataFolder().toString() + File.separator + "mod-config.txt");

        // Get all the configs...
        _defaultConfig = LoadConfigFile(defaultConfigFile);
        _helperConfig = LoadConfigFile(helperConfigFile);
        _modConfig = LoadConfigFile(modConfigFile);

        // Inherit in a chain...
        _helperConfig.addAll(_defaultConfig);
        _modConfig.addAll(_helperConfig);
    }

    private List<String> LoadConfigFile(File file) {
        try {
            // Make sure the file exists if it doesn't... and return nothing.
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
                return new ArrayList<String>();
            }

            // Otherwise, return all the lines!
            return Files.readAllLines(file.toPath());
        } catch (IOException e) {
            return new ArrayList<String>();
        }
    }

    @EventHandler
    public void onPlayerTab(PlayerCommandSendEvent e) {
        // You're an OP? Don't need to inspect, firehose of commands
        Player p = e.getPlayer();
        if (p.isOp()) {
            return;
        }

        // Get all their tags to pick the right scoreboard...
        Set<String> scoreboardTags = p.getScoreboardTags();
        List<String> selectedConfig = null;

        if(scoreboardTags.contains("moderator")) {
            selectedConfig = this._modConfig;
        } else if(scoreboardTags.contains("helper")) {
            selectedConfig = this._helperConfig;
        } else {
            selectedConfig = this._defaultConfig;
        }

        // Remove anything that's not explicitly allowed for non-ops so they aren't
        // cluttered with garbage
        e.getCommands().retainAll(selectedConfig);
	}

}