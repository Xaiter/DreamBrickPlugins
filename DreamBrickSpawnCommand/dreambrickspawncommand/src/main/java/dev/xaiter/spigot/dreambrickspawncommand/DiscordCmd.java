package dev.xaiter.spigot.dreambrickspawncommand;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class DiscordCmd implements Listener, CommandExecutor {

    private final String PERMISSIONS_DISCORD = "dreambrickspawncommand.discord";
    private final JavaPlugin _plugin;

    public DiscordCmd(JavaPlugin plugin) {
        this._plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Permissions check!
        if(!sender.hasPermission(PERMISSIONS_DISCORD)) {
            return false;
        }

        Server s = Bukkit.getServer();
        s.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + sender.getName() + " [\"\",{\"text\":\"Join our \"},{\"text\":\"[\",\"color\":\"red\",\"bold\":true,\"underlined\":false},{\"text\":\"Discord\",\"color\":\"gold\",\"bold\":true,\"underlined\":true,\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://discord.gg/F4eE6aM\"}},{\"text\":\"]\",\"color\":\"red\",\"bold\":true,\"underlined\":false},{\"text\":\"!\"}]");
        return true;
    }
}