package dev.xaiter.spigot.dreambrickspawncommand;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Team;

public class NameFilter implements Listener {

    private App plugin;

    public NameFilter(App plugin) {
        this.plugin = plugin;
    }

    public void onPlayerJoin(PlayerJoinEvent event) {

        Server s = Bukkit.getServer();
        ConsoleCommandSender consoleSender = Bukkit.getConsoleSender();

        //get player
        Player p = event.getPlayer();
        String playerUUID = p.getUniqueId().toString();

        //file path
        File file = new File(this.plugin.getDataFolder().toString() + File.separator + playerUUID + ".txt");
        
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));

            //read file (name previously associated with player's uuid)
            String name = reader.readLine();

            //if old name is not equal to new name...
            if (!name.equals(p.getName())){
                //store old scoreboard into new
                s.dispatchCommand(consoleSender, "execute store result score " + p.getName() + " balance run scoreboard players get " + name + " balance");
                s.dispatchCommand(consoleSender, "scoreboard players reset " + name);

                //overrides old name with new name
                createNewFile(file, p);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            //welcome message
            s.dispatchCommand(consoleSender, "tellraw @a [{\"text\":\"Welcome \",\"color\":\"gray\",\"bold\":\"false\"},{\"text\":\"" + p.getName() + "\",\"color\":\"gold\",\"bold\":\"true\"},{\"text\":\" to Dream's End!\",\"color\":\"gray\",\"bold\":\"false\"}]");
            
            //creates a new file
            createNewFile(file, p);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void createNewFile(File file, Player p){
        try {
            //writes player name to file
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(p.getName());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}