package dev.xaiter.spigot.biometeleporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class App extends JavaPlugin implements CommandExecutor {

    private BiomeGraphGenerator _graphGenerator;
    private Biome[] _biomeCategories;
    private Random _rng = new Random();

    public App() {
        
    }

    @Override
    public void onEnable() {
        File folder = this.getDataFolder();
        if (!folder.exists()) {
            folder.mkdirs();
        }

        this._graphGenerator = new BiomeGraphGenerator(folder.toString());
        this._biomeCategories = _graphGenerator.GetBiomeCategories();
        
        this.getCommand("biometp").setExecutor(this);
    }

    @Override
    public void onDisable() {

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp() || args.length != 7) {
            // lol, nope
            return false;
        }

        // Bad biome? Leave.
        String targetBiome = args[0];
        if (!IsValidBiomeCategory(targetBiome)) {
            return false;
        }

        // Parse out the coords...
        Integer teleportMinX = TryParse(args[1]);
        Integer teleportMinY = TryParse(args[2]);
        Integer teleportMinZ = TryParse(args[3]);
        Integer teleportMaxX = TryParse(args[4]);
        Integer teleportMaxY = TryParse(args[5]);
        Integer teleportMaxZ = TryParse(args[6]);

        // Bad data? Leave.
        if (teleportMinX == null || teleportMaxX == null || teleportMinY == null || teleportMaxY == null
                || teleportMinZ == null || teleportMaxZ == null) {
            return false;
        }

        // Get a file handle...
        RandomAccessFile rngFileAccess;
        try {
            rngFileAccess = this._graphGenerator.getStreamManager().GetRandomAccessFileStream(targetBiome);
        } catch (FileNotFoundException e) {
            getLogger().severe("Unable to locate data file for biome " + targetBiome);             
            return false;
        }

        // Measure the file length, get the number of coordinate pairs (int/int)
        long pairCount;
        try {
            pairCount = rngFileAccess.length() / 8;
        } catch (IOException e) {
            getLogger().severe("Unable to get data file length for biome " + targetBiome);
            try {
                rngFileAccess.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }            
            return false;
        }

        // Get a random pair index, calculate the offset
        long selectedPair = Math.abs(this._rng.nextLong()) % pairCount;
        long offset = selectedPair * 8;

        // Next, seek to the right location in the file and read 8 bytes
        int targetChunkX, targetChunkZ;
        try {
            rngFileAccess.seek(offset);
            targetChunkX = rngFileAccess.readInt();
            targetChunkZ = rngFileAccess.readInt();
        } catch (IOException e) {
            getLogger().severe("Unable to get seek to position " + offset + " in data file for biome " + targetBiome);
            return false;
        }
        finally {
            try {
                rngFileAccess.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Now find all of the players in the target selection range...
        Server s = Bukkit.getServer();
        java.util.Collection<? extends org.bukkit.entity.Player> players = s.getOnlinePlayers();
        ArrayList<Player> affectedPlayers = new ArrayList<Player>();
        for(Player p : players) {
            if(IsWithinBounds(p.getLocation(), teleportMinX, teleportMaxX, teleportMinY, teleportMaxY, teleportMinZ, teleportMaxZ))
                affectedPlayers.add(p);
        }

        Logger l = getLogger();
        l.info("Found " + affectedPlayers.size() + " players.");

        // Issue the commands necessary to move the players to the overworld and spread them over the desired range!
        ConsoleCommandSender consoleSender = Bukkit.getConsoleSender();

        // First generate a unique team name for these people...
        String teamName = UUID.randomUUID().toString().replaceAll("\u2014", "").substring(0, 16);
        s.dispatchCommand(consoleSender, "team add " +  teamName + " \"" + teamName + "\"");

        // Put them all on a team, get them into the overworld!
        for(Player p : affectedPlayers) {

            // Quick Sanity Check: Only players in The End can be teleported - if this isn't true, what the heck just happened?
            if(p.getWorld().getEnvironment() != Environment.THE_END) {
                continue;
            }

            // Join the team...
            s.dispatchCommand(consoleSender, "team join " + teamName + " " + p.getName());

            // Get them into the overworld!
            s.dispatchCommand(consoleSender, "warp biometp_overworld_parking " + p.getName());
        }

        // Try to spread them around the chunk...
        String cmd = "spreadplayers " + targetChunkX + " " + targetChunkZ + " 0 32 true @a[team=" + teamName + "]";
        l.info(cmd);
        boolean result = s.dispatchCommand(consoleSender, cmd);

        // Did the previous command just fail?
        if(!result) {
            // oh crap send them back
            for(Player p : affectedPlayers) {
                s.dispatchCommand(consoleSender, "warp biomertp " + p.getName());
                s.dispatchCommand(consoleSender, "team join spawn " + p.getName());
            }
        } else {
            // Set 'em to survival mode
            for(Player p : affectedPlayers) {
                s.dispatchCommand(consoleSender, "gamemode survival " + p.getName());

                s.dispatchCommand(consoleSender, "resetranks " + p.getName());
            }
        }

        // We didn't generate an exception, whether or not the TP succeeded, so this command didn't "fail"
        return true;
    }
    private static boolean IsWithinBounds(Location loc, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        return loc.getBlockX() > minX
                && loc.getBlockX() < maxX
                && loc.getBlockY() > minY
                && loc.getBlockY() < maxY
                && loc.getBlockZ() > minZ
                && loc.getBlockZ() < maxZ;
    }
    private boolean IsValidBiomeCategory(String biomeName) {
        for (Biome b : this._biomeCategories) {
            if(b.name().equalsIgnoreCase(biomeName))
                return true;
        }
        return false;
    }
    private static Integer TryParse(String s) {
        try {
            return Integer.parseInt(s);
        }
        catch (NumberFormatException e) {
            return null;
        }
    }
    private void RegenerateData() {
        File folder = this.getDataFolder();
        if(!folder.exists()) {
            folder.mkdirs();
        }

        World w = Bukkit.getServer().getWorld("world");
        BiomeGraphGenerator t = new BiomeGraphGenerator(folder.toString());
        try {
            t.GenerateBiomeData(folder.getAbsolutePath(), w, -10000, 10000, -10000, 10000, 32);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}