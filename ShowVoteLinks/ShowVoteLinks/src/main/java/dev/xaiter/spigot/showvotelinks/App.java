package dev.xaiter.spigot.showvotelinks;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;

import net.md_5.bungee.api.chat.TextComponent;

public class App extends JavaPlugin implements Listener, CommandExecutor {
    private String voteMessage;
    private String voteLinksTemplate;
    private String commandName = "vote";

    @Override
    public void onEnable() {
        readJsonTemplate();
        buildVoteLinksMessage();
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        this.getCommand(commandName).setExecutor(this);
    }

    private void readJsonTemplate() {
        String jsonTemplateFilePath = this.GetPluginFolder() + "/votelinks.json";
        FileInputStream inputStream = new FileInputStream(jsonTemplateFilePath);
        try {
            this.voteLinksTemplate = IOUtils.toString(inputStream);

        } catch (Exception exception) {

        } finally {
            inputStream.close();
        }
    }

    private void buildVoteLinksMessage() {
        String voteLinksFilePath = this.GetPluginFolder() + "/votelinks.txt";
        FileInputStream inputStream = new FileInputStream(voteLinksFilePath);
        try {
            String[] inputRows = IOUtils.toString(inputStream).split("\n");
            ArrayList<String> voteLinks = new ArrayList<String>();

            for (String voteLink: voteLinks) {
                if (!voteLink.isEmpty())
                    voteLinks.add(
                        String.format(
                            this.voteLinksTemplate,
                            voteLink
                        )
                    );
                }
            }

            this.voteMessage = "Vote: " + String.join(" ", voteLinks);
        } catch (Exception exception) {
            // @todo: Exceptionhandling.
        } finally {
            inputStream.close();
        }
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Bukkit.broadcastMessage(this.voteMessage);
        return true;
    }
}