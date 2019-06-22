package dev.hx.spigot.resetranks;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class App extends JavaPlugin implements Listener, CommandExecutor {

    @Override
    public void onEnable() {
        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(this, this);
        this.getCommand("resetranks").setExecutor(this);
    }
    @Override
    public void onDisable() {
        HandlerList.unregisterAll((Listener) this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        Server s = Bukkit.getServer();
        ConsoleCommandSender consoleSender = Bukkit.getConsoleSender();

        //needs 1 arg and one arg only
        if (args.length != 1){
            return false;
        }

        //kicks players from teams, then adds to correct team in increasing importance
        s.dispatchCommand(consoleSender, "execute as " + args[0] + " run team leave @s");
        s.dispatchCommand(consoleSender, "execute as " + args[0] + " if entity @s[tag=donator1] run team join donator1 @s");
        s.dispatchCommand(consoleSender, "execute as " + args[0] + " if entity @s[tag=donator2] run team join donator2 @s");
        s.dispatchCommand(consoleSender, "execute as " + args[0] + " if entity @s[tag=donator3] run team join donator3 @s");
        s.dispatchCommand(consoleSender, "execute as " + args[0] + " if entity @s[tag=helper] run team join helper @s");
        s.dispatchCommand(consoleSender, "execute as " + args[0] + " if entity @s[tag=moderator] run team join moderator @s");
        s.dispatchCommand(consoleSender, "execute as " + args[0] + " if entity @s[tag=admin] run team join admin @s");
        
        return true;
    }
}