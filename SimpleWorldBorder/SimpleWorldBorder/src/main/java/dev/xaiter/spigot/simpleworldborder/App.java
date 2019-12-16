package dev.xaiter.spigot.simpleworldborder;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Nameable;
import org.bukkit.World;
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
import org.bukkit.event.player.PlayerMoveEvent;
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

import net.md_5.bungee.api.chat.TextComponent;

public class App extends JavaPlugin implements Listener {

    private final int SPAWN_MIN_X = 28000000;
    private final int WORLD_LIMIT = 200000;
    private final int SNAP_BACK_RANGE = 250;
    private final int SNAP_BACK_DISTANCE = 10;
    private final int TELEPORT_BACK_PUSH_DISTANCE_MULTIPLIER = 50;
    private final int TELEPORT_SAFETY_BUFFER_DISTANCE = SNAP_BACK_DISTANCE * TELEPORT_BACK_PUSH_DISTANCE_MULTIPLIER;
    private final long CHECK_INTERVAL_MILLISECONDS = 1500;

    private long _nextCheckMilliseconds = 0;

    @Override
    public void onEnable() {
        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll((Listener) this);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {

        // Don't bother checking, it's not time.
        if(Instant.now().toEpochMilli() < _nextCheckMilliseconds)
            return;

        // Update the next check time
        _nextCheckMilliseconds = Instant.now().toEpochMilli() + CHECK_INTERVAL_MILLISECONDS;

        // If they're in spawn, nothing to check
        Player p = e.getPlayer();
        int x1 = (int)p.getLocation().getX();
        if(x1 > SPAWN_MIN_X) { 
            return;
        }

        // Get up the remaining variables we'll need for conditionals below into the local scope
        int z1 = (int)p.getLocation().getZ();
        int absX = Math.abs(x1);
        int absZ = Math.abs(z1);
        int fromX = (int)e.getFrom().getX();
        int fromZ = (int)e.getFrom().getZ();        

        // Tracking variables set by the condition of the variables declared above
        boolean teleportBack = false;
        int pushDistanceX = 0;
        int pushDistanceZ = 0;

        // If they're past the world limit on either axis, cancel the movement
        if(absX > WORLD_LIMIT) {
            teleportBack = true;

            pushDistanceX = absX - WORLD_LIMIT + SNAP_BACK_DISTANCE;
        }

        if(absZ > WORLD_LIMIT) {
            teleportBack = true;

            pushDistanceZ = absZ - WORLD_LIMIT + SNAP_BACK_DISTANCE;
        }

        // Do we need to move the player back?
        if(teleportBack) {

            int toX, toZ = 0;

            // Are we within snapping range?
            if(absX - WORLD_LIMIT < SNAP_BACK_RANGE && absZ - WORLD_LIMIT < SNAP_BACK_RANGE) {
                // Yep, just snap them back.  Little jarring, but better than an RTP.
                CancelMovement(e);

                // Flip the push distances if we're in the positives
                if(x1 > 0)
                    pushDistanceX = -pushDistanceX;
                if(z1 > 0)
                    pushDistanceZ = -pushDistanceZ;

                // Calculate the destination coordinates...
                toX = fromX + pushDistanceX;
                toZ = fromZ + pushDistanceZ;

                // Force the player back!
                e.getFrom().setX(toX);
                e.getFrom().setZ(toZ);

                // Tell the player they went too far!
                SendDireWarningMessage(p, "The world limit is " + WORLD_LIMIT + "!  TURN BACK!");
            } else {
                // Okay, they're too far to just "push" back.  They need to be TP'd back.  
                // We don't want to cancel the event here.

                // Start by magnifying the teleport distance for spreadplayers safety
                pushDistanceX += TELEPORT_SAFETY_BUFFER_DISTANCE;
                pushDistanceZ += TELEPORT_SAFETY_BUFFER_DISTANCE;

                // Flip the push distances if we're in the positives
                if(x1 > 0)
                    pushDistanceX = -pushDistanceX;
                if(z1 > 0)
                    pushDistanceZ = -pushDistanceZ;

                // Calculate the destination coordinates...
                toX = fromX + pushDistanceX;
                toZ = fromZ + pushDistanceZ;

                // Send 'em back with a warning
                String cmd = "spreadplayers " + toX + " " + toZ + " 0 " + TELEPORT_SAFETY_BUFFER_DISTANCE + " false " + p.getName();
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                SendDireWarningMessage(p, "The world limit is " + WORLD_LIMIT + "!");
            }
            
            //#region Temp Disabled Code?
            // // If their "from" coordinates are past the world limit too, damage them!
            // if(Math.abs(fromX) > WORLD_LIMIT || Math.abs(fromZ) > WORLD_LIMIT) {
            //     // Set health will ignore their armor
            //     if(p.getHealth() > 2) {
            //         p.setHealth(p.getHealth() - 2);
            //     } else {
            //         // rip
            //         p.setHealth(0);
            //     }

            //     // Damage will provoke a hurt effect
            //     p.damage(1);
            // }
            //#endregion
        }
    }

    private void CancelMovement(PlayerMoveEvent e) {
        e.setCancelled(true);
    }

    private void SendDireWarningMessage(Player p, String text) {
        TextComponent message = new TextComponent(text);
        message.setBold(true);
        message.setColor(net.md_5.bungee.api.ChatColor.RED);
        p.spigot().sendMessage(message);
    }
}