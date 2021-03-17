package dev.xaiter.spigot.dreambrickspawncommand;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.Statistic;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class App extends JavaPlugin implements Listener {

    private final int TELEPORT_COST = 5;
    private final String MSG_COLLECTIVE_DREAM = "You experience a collective dream...";
    private final String MSG_COLLECTIVE_DREAM_TIME_WARNING = "[You will be teleported to spawn in 5 seconds]";
    private final String MSG_TELEPORT_FAILED_NOT_ASLEEP = "You must be asleep to teleport to spawn! (Enter a bed)";
    private final String MSG_TELEPORT_FAILED_CANNOT_AFFORD = "You need at least " + TELEPORT_COST + " vote flint to return to spawn! (Sorry!)";
    private final String MSG_TELEPORT_FAILED_BED_OBSTRUCTED = "Teleport to Spawn failed.  Your bed is obstructed, you will not respawn at it.";
    private final String BALANCE_SCOREBOARD_NAME = "balance";

    private final String PERMISSIONS_SPAWN = "dreambrickspawncommand.spawn";

    private final HashMap<String, LocalDateTime> LAST_MOVED_TIMETABLE = new HashMap<String, LocalDateTime>();

    private BalanceCmd _balanceCmd;
    private DiscordCmd _discordCmd;
    private RulesCmd _rulesCmd;
    private ClearChatCmd _clearChatCmd;
    private GetBedLocCmd _getBedLocCmd;
    private ReturnHomeCmd _returnHomeCmd;
    private OpCommandFilter _commandFilter;

    public App() {
    }



    // General Plug-In Events
    @Override
    public void onEnable() {
        final PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(this, this);

        // Make fresh copies of our commands and filter...
        this._balanceCmd = new BalanceCmd(this);
        this._discordCmd = new DiscordCmd(this);
        this._rulesCmd = new RulesCmd(this);
        this._clearChatCmd = new ClearChatCmd(this);
        this._getBedLocCmd = new GetBedLocCmd(this);
        this._returnHomeCmd = new ReturnHomeCmd(this);
        this._commandFilter = new OpCommandFilter(this);

        // Register Commands!
        this.getCommand("spawn").setExecutor(this);
        this.getCommand("bal").setExecutor(this._balanceCmd);
        this.getCommand("balance").setExecutor(this._balanceCmd);
        this.getCommand("getbedloc").setExecutor(this._getBedLocCmd);
        this.getCommand("returnhome").setExecutor(this._returnHomeCmd);
        this.getCommand("rules").setExecutor(this._rulesCmd);
        this.getCommand("discord").setExecutor(this._discordCmd);
        this.getCommand("clearchat").setExecutor(this._clearChatCmd);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll((Listener) this);
        HandlerList.unregisterAll((Listener) this._balanceCmd);
        HandlerList.unregisterAll((Listener) this._getBedLocCmd);
        HandlerList.unregisterAll((Listener) this._rulesCmd);
        HandlerList.unregisterAll((Listener) this._returnHomeCmd);
        HandlerList.unregisterAll((Listener) this._discordCmd);
        HandlerList.unregisterAll((Listener) this._clearChatCmd);
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent e)
    {
        final TextComponent welcomeLine0 = new TextComponent("=============================================");
        welcomeLine0.setObfuscated(true);

        final TextComponent welcomeLine1 = new TextComponent("Welcome to Dream's End");
        welcomeLine1.setBold(true);
        welcomeLine1.setColor(ChatColor.GOLD);

        final TextComponent welcomeLine2 = new TextComponent("On this server, the Spawn is not located in the Survival gameplay area.  You will need to enter a bed and type /spawn to return to spawn, and you will be returned to your bed upon exiting spawn.");
        welcomeLine2.setBold(false);
        welcomeLine2.setColor(ChatColor.DARK_AQUA);

        final TextComponent welcomeLine3 = new TextComponent("You may also teleport to Spawn by attempting to sleep through the night.  If all players in the Overworld are asleep and the night would be skipped, all sleeping players will be teleported to spawn for free and the night will be skipped.");
        welcomeLine3.setBold(false);
        welcomeLine3.setColor(ChatColor.DARK_AQUA);

        final TextComponent welcomeLine4 = new TextComponent("=============================================");
        welcomeLine4.setObfuscated(true);

        final Player p = e.getPlayer();
        p.spigot().sendMessage(welcomeLine0);
        p.spigot().sendMessage(welcomeLine1);
        p.spigot().sendMessage(welcomeLine2);
        p.spigot().sendMessage(new TextComponent(""));
        p.spigot().sendMessage(welcomeLine3);
        p.spigot().sendMessage(new TextComponent(""));
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + e.getPlayer().getName() + " [\"\",{\"color\":\"dark_aqua\",\"text\":\"Got any questions?  Join our \"},{\"text\":\"[\",\"color\":\"red\",\"bold\":true,\"underlined\":false},{\"text\":\"Discord\",\"color\":\"gold\",\"bold\":true,\"underlined\":true,\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://discord.gg/F4eE6aM\"}},{\"text\":\"]\",\"color\":\"red\",\"bold\":true,\"underlined\":false},{\"color\":\"dark_aqua\",\"text\":\" or type /help for information on commands.\"}]");
        p.spigot().sendMessage(welcomeLine4);
    }



    // Spawn Mechanic Methods
    @EventHandler
    public void onPlayerBedEnterEvent(final PlayerBedEnterEvent e) {
        // If they didn't successfully enter a bed, we can't possibly be in the correct state.
        if(e.getBedEnterResult() != BedEnterResult.OK)
            return;

        // If they managed to get into the bed, forcibly set their spawn location to ensure it sticks
        e.getPlayer().setBedSpawnLocation(e.getBed().getLocation());

        // If not everyone is asleep, there's nothing to do
        if(!AreAllPlayersAsleep(e.getPlayer()))
            return;

        // Time to send everyone to spawn - handy references we'll need.
        final Server s = Bukkit.getServer();
        final Collection<? extends Player> onlinePlayers = s.getOnlinePlayers();

        // For each player in the correct state, send them to spawn FIVE seconds later.
        for(final Player p : onlinePlayers) {
            if(IsPlayerValidSleepTarget(p)) {
                WakeUpAndTeleportPlayer(p, s);
                ResetTimeSinceRest(p);
                MessagePlayer(p, MSG_COLLECTIVE_DREAM, ChatColor.LIGHT_PURPLE);
                MessagePlayer(p, MSG_COLLECTIVE_DREAM_TIME_WARNING, ChatColor.LIGHT_PURPLE);
            }
        }

        // First, give them FOUR seconds to actually sleep and for the night to naturally skip...
        // But if it doesn't, force the night to skip.  This should wake them up "naturally".
        s.getScheduler().scheduleSyncDelayedTask(this, () -> {
            s.dispatchCommand(Bukkit.getConsoleSender(), "time set morning");
        }, 80);
    }

    @EventHandler
    public void onPlayerBedLeaveEvent(final PlayerBedLeaveEvent e) {
        // unused for now...
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerMove(final PlayerMoveEvent event) {
        this.LAST_MOVED_TIMETABLE.put(event.getPlayer().getName(), LocalDateTime.now());
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        // Permissions check!
        if(!sender.hasPermission(PERMISSIONS_SPAWN)) {
            return false;
        }

        final Player p = GetPlayer(sender.getName());

        // Wait, what?  Who sent this command?
        if(p == null) 
            return true;

        // Hey!  You need to be asleep to dream!
        if(!p.isSleeping()) {
            MessagePlayer(p, MSG_TELEPORT_FAILED_NOT_ASLEEP, ChatColor.RED);
            return true;
        }

        // Okay, check if the player can afford it, and charge them if they can!
        final Server s = Bukkit.getServer();
        if(!TryPayForTeleport(s, p))
            return true;
        
        // Kick them out of bed and TP time
        WakeUpAndTeleportPlayer(p, s);

        // Everything should have worked...
        return true;
    }

    private void ResetTimeSinceRest(final Player p) {
        p.setStatistic(Statistic.TIME_SINCE_REST, 0);
    }

    private void WakeUpAndTeleportPlayer(final Player p, final Server s) {
        // Grab their name, we don't want to rely on a reference that could be dead in 100 ticks
        final String playerName = p.getName();

        // Wait FIVE SECONDS for the night skip to naturally occur
        s.getScheduler().scheduleSyncDelayedTask(this, () -> {
            // Make sure they're still connected...
            final Player tempPlayer = s.getPlayer(playerName);
            if(tempPlayer == null)
                return;

            // If they're somehow STILL asleep (HOW?) wake them up!
            if(tempPlayer.isSleeping())
                tempPlayer.wakeup(true);

            // And move 'em.
            TeleportToSpawn(s, tempPlayer);
        }, 100);
    }

    private void TeleportToSpawn(final Server s, final Player p) {
        // Safety checks
        if(p == null || !p.isOnline()) {
            return;
        }

        // Make sure the player isn't an idiot
        if(IsBedObstructed(p)) {
            MessagePlayer(p, MSG_TELEPORT_FAILED_BED_OBSTRUCTED, ChatColor.RED);
            return;
        }

        // Okay, we're good, send 'em to spawn!
        s.dispatchCommand(s.getConsoleSender(), "warp overworld_spawn " +  p.getName());
    }

    private boolean TryPayForTeleport(final Server s, final Player p) {
        final Objective o = s.getScoreboardManager().getMainScoreboard().getObjective(BALANCE_SCOREBOARD_NAME);
        final Score score = o.getScore(p.getName());
        
        // Can't afford it, we're done
        final int balance = score.getScore();
        if(balance < TELEPORT_COST) {
            MessagePlayer(p, MSG_TELEPORT_FAILED_CANNOT_AFFORD, ChatColor.RED);
            return false;
        }
        
        // Charge the player!
        score.setScore(balance - TELEPORT_COST);
        return true;
    }

    private boolean AreAllPlayersAsleep(final Player eventOwner) {
        // Get all the players...
        final Server s = Bukkit.getServer();
        final Collection<? extends Player> onlinePlayers = s.getOnlinePlayers();

        // Is everyone asleep?
        for(final Player p : onlinePlayers) {
            // Skip 'em if they're not a valid sleep target
            if(!IsPlayerValidSleepTarget(p))
                continue;

            // Skip the player who triggered the event, they're definitely "asleep" enough.
            if(p.getName().equals(eventOwner.getName()))
                continue;

            // Finally, let's see if this person isn't asleep, and exit if they aren't.
            if(p.getSleepTicks() == 0)
                return false;
        }

        // Yep, everyone's asleep.
        return true;
    }

    private boolean IsPlayerValidSleepTarget(final Player p) {
        // If anyone is exempt from the sleeping rule, they shouldn't count here.
        if(p.isSleepingIgnored() || p.isOp() || IsPlayerAFK(p.getName()))
            return false;

        // Skip 'em if not in the overworld
        if(p.getWorld().getEnvironment() != World.Environment.NORMAL)
            return false;

        return true;
    }

    private boolean IsPlayerAFK(final String playerName) {
        if(!this.LAST_MOVED_TIMETABLE.containsKey(playerName)) {
            return false;
        }
        return ChronoUnit.SECONDS.between(this.LAST_MOVED_TIMETABLE.get(playerName), LocalDateTime.now()) > 299; // 5 minutes
    }

    private boolean IsBedObstructed(final Player p) {
        return p.getBedSpawnLocation() == null;
    }



    // General Junk
    private void MessagePlayer(final Player p, final String text, final ChatColor color) {
        final TextComponent message = new TextComponent(text);
        message.setBold(true);
        message.setColor(color);
        p.spigot().sendMessage(message);
    }

    private Player GetPlayer(final String name) {
        final Collection<? extends Player> players = GetOnlinePlayers();
        for(final Player p : players) {
            if(p.getName().equalsIgnoreCase(name))
                return p;
        }
        return null;
    }

    private Collection<? extends Player> GetOnlinePlayers() {
        final Server s = Bukkit.getServer();
        return s.getOnlinePlayers();
    }



    // Command Filter Event Passthrough
    @EventHandler
    public void onPlayerTab(final PlayerCommandSendEvent e) {
        // We're just a passthrough.
        this._commandFilter.onPlayerTab(e);
	}
}