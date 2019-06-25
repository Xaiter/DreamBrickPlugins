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
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
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
        Player player = event.getPlayer();
        String playerUUID = player.getUniqueId().toString();

        //file path
        File file = new File(this.plugin.getDataFolder().toString() + File.separator + "players" + File.separator + playerUUID + ".txt");
        
        //declare as null
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(file));

            //read file (name previously associated with player's uuid)
            String oldName = reader.readLine();

            //if old name is not equal to new name...
            if (!oldName.equals(player.getName())){

                Scoreboard scoreboard = s.getScoreboardManager().getMainScoreboard();
                
                //store old scoreboard into new
                for (Objective o : scoreboard.getObjectives()){
                    s.dispatchCommand(consoleSender, "execute store result score " + player.getName() + " " + o.getName() + " run scoreboard players get " + oldName + " " + o.getName());
                }
                //reset scores?
                //s.dispatchCommand(consoleSender, "scoreboard players reset " + oldName);

                //gets team for player's old name
                Team team = getTeamForPlayerName(scoreboard, oldName, playerUUID);
                
                //old player leaves team
                s.dispatchCommand(consoleSender, "team leave " + oldName);

                //if the old name was on a team, make the new name join the team
                if (team != null){
                    s.dispatchCommand(consoleSender, "team join " + team.getName() + " " + player.getName());
                }
                //if no team was found, kick the player out
                else {
                    s.dispatchCommand(consoleSender, "team leave " + player.getName());
                }

                //overrides old name with new name
                writeFile(file, player);
            }
        } catch (FileNotFoundException e) {
            //welcome message
            s.dispatchCommand(consoleSender, "tellraw @a [{\"text\":\"Welcome \",\"color\":\"gray\",\"bold\":\"false\"},{\"text\":\"" + player.getName() + "\",\"color\":\"gold\",\"bold\":\"true\"},{\"text\":\" to Dream's End!\",\"color\":\"gray\",\"bold\":\"false\"}]");
            
            //sets balance to zero (so /bal will always show a number)
            s.dispatchCommand(consoleSender, "scoreboard players set " + player.getName() + " balance 0");

            //anything else that should happen when a player first joins the server

            //creates a new file
            writeFile(file, player);
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            //always try closing
            tryCloseReader(reader);
        }
    }

    public static Team getTeamForPlayerName(Scoreboard scoreboard, String name, String uuid){

        Team team = null;

        //loop through every team
        for (Team t : scoreboard.getTeams()){
            //loop through every player
            for (OfflinePlayer p : t.getPlayers()){
                //return team if names match and uuids match (in case two players have had the same past names)
                if (name.equals(p.getName()) && uuid.equals(p.getUniqueId().toString())){
                    return t;
                }
            }
        }

        //team is null if no team is found
        return team;

    }

    public static void writeFile(File file, Player p){

        //declare as null
        BufferedWriter writer = null;

        try {
            //writes player name to file
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(p.getName());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //always try closing
            tryCloseWriter(writer);
        }
    }

    public static void tryCloseReader(BufferedReader reader){
        //does reader exist
        if (reader != null){
            try {
                //try closing no matter what
                reader.close();
            } catch (IOException e) {
                //dont care
                e.printStackTrace();
            }
        }
    }

    public static void tryCloseWriter(BufferedWriter writer){
        //does writer exist
        if (writer != null){
            try {
                //try closing no matter what
                writer.close();
            } catch (IOException e) {
                //dont care
                e.printStackTrace();
            }
        }
    }

}
