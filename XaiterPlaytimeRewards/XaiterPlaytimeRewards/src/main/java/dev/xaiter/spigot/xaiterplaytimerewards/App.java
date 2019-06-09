package dev.xaiter.spigot.xaiterplaytimerewards;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Nameable;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.DirectionalContainer;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class App extends JavaPlugin {

    private final int SLEEP_INTERVAL = 60000;
    private final int POINTS_PER_TOKEN = 60;

    protected Thread _backgroundThread = null;

    @Override
    public void onEnable() {
        Runnable r = new Runnable() {
            public void run() {
                RewardsLoop();
            }
        };

        _backgroundThread = new Thread(r);
        _backgroundThread.start();
    }

    @Override
    public void onDisable() {
        _backgroundThread.interrupt();
    }

    private void RewardsLoop() {
        while (true) {
            try {
                Thread.sleep(SLEEP_INTERVAL);
            } catch (InterruptedException e) {
                // politely exit if we're interrupted
                return;
            }
            
            AwardConnectedPlayers();
        }
    }

    private void AwardConnectedPlayers() {

        Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective fractionalBalanceObjective = CreateScoreboardObjectiveIfNotExists(mainScoreboard, "fracBal", "fracBal");
        Objective balanceObjective = CreateScoreboardObjectiveIfNotExists(mainScoreboard, "balance", "balance");

        java.util.Collection<? extends org.bukkit.entity.Player> players = Bukkit.getOnlinePlayers();
        for(Player p : players) {
            String playerName = p.getName();
            Score pointScore = fractionalBalanceObjective.getScore(playerName);
            
            // If they have enough points, it's time to payout.
            int newScoreValue = pointScore.getScore() + 1;
            if(newScoreValue >= POINTS_PER_TOKEN) {
                newScoreValue -= POINTS_PER_TOKEN;
                Score tokenScore = balanceObjective.getScore(playerName);
                tokenScore.setScore(tokenScore.getScore() + 1);
            }

            // And update their fractional score
            pointScore.setScore(newScoreValue);
        }
    }

    private Objective CreateScoreboardObjectiveIfNotExists(Scoreboard mainScoreboard, String name, String displayName) {
        Objective obj = mainScoreboard.getObjective(name);
        if(obj == null) {
            obj = mainScoreboard.registerNewObjective(name, name, displayName);
        }
        return obj;
    }
}