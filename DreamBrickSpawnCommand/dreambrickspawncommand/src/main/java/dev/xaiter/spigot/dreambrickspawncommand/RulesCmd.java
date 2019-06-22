package dev.xaiter.spigot.dreambrickspawncommand;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class RulesCmd implements Listener, CommandExecutor {

    private final String PERMISSIONS_RULES = "dreambrickspawncommand.rules";
    private final JavaPlugin _plugin;

    public RulesCmd(JavaPlugin plugin) {
        this._plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Permissions check!
        if(!sender.hasPermission(PERMISSIONS_RULES)) {
            return false;
        }

        Server s = Bukkit.getServer();
        s.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + sender.getName() + " [\"\",{\"text\":\"[1] \",\"color\":\"gold\"},{\"text\":\"Don't be a dick.\",\"color\":\"gray\"}]");
        s.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + sender.getName() + " [\"\",{\"text\":\"[2] \",\"color\":\"gold\"},{\"text\":\"No hacking/dupe exploiting\",\"color\":\"gray\"}]");
        s.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + sender.getName() + " [\"\",{\"text\":\"[3] \",\"color\":\"gold\"},{\"text\":\"Don't make lag machines.\",\"color\":\"gray\"}]");
        s.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + sender.getName() + " [\"\",{\"text\":\"See the \"},{\"text\":\"[\",\"color\":\"red\",\"bold\":true,\"underlined\":false},{\"text\":\"Discord\",\"color\":\"gold\",\"bold\":true,\"underlined\":true,\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://discord.gg/F4eE6aM\"}},{\"text\":\"]\",\"color\":\"red\",\"bold\":true,\"underlined\":false},{\"text\":\" for more information\"}]");
        return true;
    }
}