package dev.xaiter.spigot.dreambrickspawncommand;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class App extends JavaPlugin implements Listener, CommandExecutor {

    private final int TELEPORT_COST = 5;
    private final String MSG_COLLECTIVE_DREAM = "You experience a collective dream...";
    private final String MSG_TELEPORT_FAILED_NOT_ASLEEP = "You must be asleep to teleport to spawn! (Enter a bed)";
    private final String MSG_TELEPORT_FAILED_CANNOT_AFFORD = "You need at least " + TELEPORT_COST + " vote flint to return to spawn! (Sorry!)";
    private final String BALANCE_SCOREBOARD_NAME = "balance";

    private final String PERMISSIONS_SPAWN = "dreambrickspawncommand.spawn";

    private final BalanceCmd _balanceCmd;
    private final DiscordCmd _discordCmd;
    private final RulesCmd _rulesCmd;

    public App() {
        this._balanceCmd = new BalanceCmd(this);
        this._discordCmd = new DiscordCmd(this);
        this._rulesCmd = new RulesCmd(this);
    }


    @Override
    public void onEnable() {
        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(this, this);

        // Register Commands!
        this.getCommand("spawn").setExecutor(this);
        this.getCommand("bal").setExecutor(this._balanceCmd);
        this.getCommand("balance").setExecutor(this._balanceCmd);
        this.getCommand("rules").setExecutor(this._rulesCmd);
        this.getCommand("discord").setExecutor(this._discordCmd);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll((Listener) this);     
    }

    @EventHandler
    public void onPlayerBedEnterEvent(PlayerBedEnterEvent e) {
        // If they didn't successfully enter a bed, we can't possibly be in the correct state.
        if(e.getBedEnterResult() != BedEnterResult.OK)
            return;

        // If they managed to get into the bed, forcibly set their spawn location to ensure it sticks
        e.getPlayer().setBedSpawnLocation(e.getBed().getLocation());

        if(AreAllPlayersAsleep(e.getPlayer())) {
            Server s = Bukkit.getServer();
            Collection<? extends Player> onlinePlayers = s.getOnlinePlayers();

            for(Player p : onlinePlayers) {
                if(!p.isOp()) {
                    TeleportToSpawn(s, p);
                    MessagePlayer(p, MSG_COLLECTIVE_DREAM, ChatColor.LIGHT_PURPLE);
                }
            }

            // If we don't cancel, the player who gets into bed last won't be TP'd.  :(
            e.setCancelled(true);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Permissions check!
        if(!sender.hasPermission(PERMISSIONS_SPAWN)) {
            return false;
        }

        Player p = GetPlayer(sender.getName());

        // Wait, what?  Who sent this command?
        if(p == null) 
            return true;

        // Hey!  You need to be asleep to dream!
        if(!p.isSleeping()) {
            MessagePlayer(p, MSG_TELEPORT_FAILED_NOT_ASLEEP, ChatColor.RED);
            return true;
        }

        // Okay, check if the player can afford it, and charge them if they can!
        Server s = Bukkit.getServer();
        if(!TryPayForTeleport(s, p))
            return true;
        
        // Finally, teleport the player and exit
        TeleportToSpawn(s, p);
        return true;
    }

    private Player GetPlayer(String name) {
        Collection<? extends Player> players = GetOnlinePlayers();
        for(Player p : players) {
            if(p.getName().equalsIgnoreCase(name))
                return p;
        }
        return null;
    }

    private void TeleportToSpawn(Server s, Player p) {
        s.dispatchCommand(s.getConsoleSender(), "warp overworld_spawn " +  p.getName());
    }

    private boolean TryPayForTeleport(Server s, Player p) {
        Objective o = s.getScoreboardManager().getMainScoreboard().getObjective(BALANCE_SCOREBOARD_NAME);
        Score score = o.getScore(p.getName());
        
        // Can't afford it, we're done
        int balance = score.getScore();
        if(balance < TELEPORT_COST) {
            MessagePlayer(p, MSG_TELEPORT_FAILED_CANNOT_AFFORD, ChatColor.RED);
            return false;
        }
        
        // Charge the player!
        score.setScore(balance - TELEPORT_COST);
        return true;
    }

    private void MessagePlayer(Player p, String text, ChatColor color) {
        TextComponent message = new TextComponent(text);
        message.setBold(true);
        message.setColor(color);
        p.spigot().sendMessage(message);
    }

    private boolean AreAllPlayersAsleep(Player eventOwner) {
        // Get all the players...
        Server s = Bukkit.getServer();
        Collection<? extends Player> onlinePlayers = s.getOnlinePlayers();

        // Is everyone asleep?
        for(Player p : onlinePlayers) {
            // Skip the player who triggered the event, they're definitely "asleep" enough.
            if(p.getName().equals(eventOwner.getName()))
                continue;

            // Ops don't count.
            if(p.getSleepTicks() == 0 && !p.isOp()) {
                return false;
            }
        }

        // Yep.
        return true;
    }

    private Collection<? extends Player> GetOnlinePlayers() {
        Server s = Bukkit.getServer();
        return s.getOnlinePlayers();
    }
}