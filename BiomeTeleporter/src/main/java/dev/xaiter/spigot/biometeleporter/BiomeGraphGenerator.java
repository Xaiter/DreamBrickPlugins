package dev.xaiter.spigot.biometeleporter;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Biome;

public class BiomeGraphGenerator {
    private final App _app;

    private final String _outputDirRoot;

    public final String getOutputDirRoot() {
        return this._outputDirRoot;
    }

    private final BiomeCategoryStreamManager _streamManager;

    public final BiomeCategoryStreamManager getStreamManager() {
        return this._streamManager;
    }

    public BiomeGraphGenerator(String outputDirRoot, App plugin) {
        this._outputDirRoot = outputDirRoot;
        this._streamManager = new BiomeCategoryStreamManager(outputDirRoot, this.GetBiomeCategories());
        this._app = plugin;
    }

    public void GenerateBiomeData(String outputDirectoryRoot, World w, int minX, int maxX, int minZ, int maxZ,
            int threadCount) throws IOException, InterruptedException {
        // Handy variables for easier math in the next steps...
        int chunksPerRow = maxX - minX;
        int rowCount = maxZ - minZ;
        int totalChunkCount = chunksPerRow * rowCount;

        // Get some the last of the variables ready for the closure...
        Logger logger = Bukkit.getLogger();
        AtomicInteger workIdCounter = new AtomicInteger();

        _streamManager.OpenAllBiomeWriteStreams(GetBiomeCategories());

        logger.info("Generating data...");
        this.GenerateChunkData(logger, workIdCounter, totalChunkCount, w, this::GetBiomeCategory, _streamManager,
                minX, minZ, chunksPerRow);

    }

    protected final void GenerateChunkData(Logger logger, AtomicInteger workIdCounter, int maxWorkId, World w,
            Function<Biome, Biome> biomeCategoryLookup, BiomeCategoryStreamManager streamManager, int chunkMinX,
            int chunkMinZ, int chunksPerRow) {
        long threadId = Thread.currentThread().getId();
        final Server s = Bukkit.getServer();

        // Attempt 2 per tick
        int workUnitId = workIdCounter.getAndIncrement();
        int stopWorkId = workUnitId + 1;
        while (workUnitId < stopWorkId) {
            ProcessWorkUnit(logger, w, biomeCategoryLookup, streamManager, chunkMinX, chunkMinZ, chunksPerRow, threadId,
                    workUnitId);
            workUnitId = workIdCounter.getAndIncrement();
        }

        // Have to process the last item we requested from the counter...
        ProcessWorkUnit(logger, w, biomeCategoryLookup, streamManager, chunkMinX, chunkMinZ, chunksPerRow, threadId,
                workUnitId);

        if (workUnitId >= maxWorkId) {
            // If we're on the last unit of work, close everything out
            try {
                _streamManager.CloseAllBiomeWriteStreams();
            } catch (IOException ex) {
                logger.warning("Error closing streams!");
                logger.warning(ex.toString());
            }
            logger.info("Done generating data!");
        } else {
            // Otherwise, schedule us to run again in 1 tick
            s.getScheduler().scheduleSyncDelayedTask(this._app, () -> {
                GenerateChunkData(logger, workIdCounter, maxWorkId, w, biomeCategoryLookup, streamManager, chunkMinX,
                        chunkMinZ, chunksPerRow);
            }, 1);
        }

    }

    private void ProcessWorkUnit(Logger logger, World w, Function<Biome, Biome> biomeCategoryLookup,
            BiomeCategoryStreamManager streamManager, int chunkMinX, int chunkMinZ, int chunksPerRow, long threadId,
            int workUnitId) {
        try {
            // Find the chunk's relative position based on the work ID
            int chunkCountOffsetMultiplierX = workUnitId / chunksPerRow;
            int chunkCountOffsetMultiplierZ = workUnitId % chunksPerRow;

            // Calculate the center of the chunk to test
            int chunkX = chunkCountOffsetMultiplierX + chunkMinX;
            int chunkZ = chunkCountOffsetMultiplierZ + chunkMinZ;

            // Get the biome....
            Biome b = GetChunkBiome(w, chunkX, chunkZ);

            // Map the biome to a category, record it if it's valid
            Biome biomeCategory = biomeCategoryLookup.apply(b);
            if (biomeCategory != Biome.THE_VOID) {
                logger.info("[Thread #" + threadId + "] [Work Unit #" + workUnitId + "] [Biome: " + b.name() + "] [X: "
                        + chunkX + "] [Z: " + chunkZ + "]");

                // Grab the correct file stream for it...
                BufferedOutputStream stream = streamManager.get(biomeCategory);

                // Get the bytes for the X/Z and write them to the buffer
                if (stream != null)
                    stream.write(getBytes(chunkX, chunkZ));
            }
        } catch (IOException ex) {
            logger.warning(ex.toString());
        }
    }

    protected Biome GetChunkBiome(World w, int chunkX, int chunkZ) {
        Biome b;
        Chunk chunk = w.getChunkAt(chunkX, chunkZ, false);
        if (chunk == null) {
            int offsetX = 8;
            int offsetZ = 8;
            if (chunkX < 0) {
                offsetX = -8;
            }
            if (chunkZ < 0) {
                offsetZ = -8;
            }
            b = w.getBiome(chunkX * 16 + offsetX, 300, chunkZ * 16 + offsetZ);
        } else {
            ChunkSnapshot snapshot = chunk.getChunkSnapshot(false, true, false);
            b = snapshot.getBiome(8, 300, 8);
            chunk.unload(false);
        }
        return b;
    }

    protected Biome[] GetBiomeCategories() {
        return new Biome[] { Biome.PLAINS, Biome.FOREST, Biome.DESERT, Biome.JAGGED_PEAKS, Biome.SWAMP, Biome.BEACH };
    }

    protected Biome GetBiomeCategory(Biome b) {

        switch (b) {
            // Plains
            case PLAINS:
            case SUNFLOWER_PLAINS:
            case SAVANNA:
            case SAVANNA_PLATEAU: {
                return Biome.PLAINS;
            }

            // Forest
            case FOREST:
            case BIRCH_FOREST:
            case WOODED_BADLANDS:
            case TAIGA:
            case DARK_FOREST:
            case SNOWY_TAIGA:
            case FLOWER_FOREST: {
                return Biome.FOREST;
            }

            // Desert
            case DESERT: {
                return Biome.DESERT;
            }

            // Mountains
            case WINDSWEPT_HILLS:
            case WINDSWEPT_FOREST:
            case WINDSWEPT_GRAVELLY_HILLS:
            case JAGGED_PEAKS: {
                return Biome.JAGGED_PEAKS;
            }

            // Swamp
            case SWAMP: {
                return Biome.SWAMP;
            }

            // Beach
            case BEACH:
            case SNOWY_BEACH: {
                return Biome.BEACH;
            }

            default: {
                return Biome.THE_VOID;
            }
        }
    }

    // oh my god what I'd give for direct memory access right now
    protected static final byte[] getBytes(int x, int z) {
        return new byte[] {
                (byte) (x >>> 24),
                (byte) (x >>> 16),
                (byte) (x >>> 8),
                (byte) x,
                (byte) (z >>> 24),
                (byte) (z >>> 16),
                (byte) (z >>> 8),
                (byte) z };
    }
}