package dev.xaiter.spigot.xaiterplaytimerewards;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

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
                MessagePlayer(p, "[The Dream Bank deposits 1 Flint into your account...]", ChatColor.AQUA);
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

    private void MessagePlayer(Player p, String text, ChatColor color) {
        TextComponent message = new TextComponent(text);
        message.setItalic(true);
        message.setColor(color);
        p.spigot().sendMessage(message);
    }
}