package dev.xaiter.spigot.biometeleporter;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;


public class BiomeGraphGenerator {

    private final String _outputDirRoot;
    public final String getOutputDirRoot() { return this._outputDirRoot; }

    private final BiomeCategoryStreamManager _streamManager;
    public final BiomeCategoryStreamManager getStreamManager() { return this._streamManager; }

    public BiomeGraphGenerator(String outputDirRoot) {
        this._outputDirRoot = outputDirRoot;
        this._streamManager = new BiomeCategoryStreamManager(outputDirRoot, this.GetBiomeCategories());
    }

    public void GenerateBiomeData(String outputDirectoryRoot, World w, int minX, int maxX, int minZ, int maxZ, int threadCount) throws IOException, InterruptedException 
    {
        // Top Left Bound
        int topLeftChunkCenterX = minX - minX % 16 + 8;
        int topLeftChunkCenterZ = minZ - minZ % 16 + 8;

        // Bottom Right Bound
        int bottomRightChunkX = maxX - maxX % 16 + 8;
        int bottomRightChunkZ = maxZ - maxZ % 16 + 8;

        // Handy variables for easier math in the next steps...
        int chunksPerRow = (bottomRightChunkX - topLeftChunkCenterX) / 16;
        int rowCount = (bottomRightChunkZ - topLeftChunkCenterZ) / 16;
        int totalChunkCount = chunksPerRow * rowCount;

        // Get some the last of the variables ready for the closure...
        Logger logger = Bukkit.getLogger();
        AtomicInteger workIdCounter = new AtomicInteger();

        // Toss the closure into a collection, once per desired thread...
        ArrayList<Callable<Boolean>> stuff = new ArrayList<Callable<Boolean>>();
        for(int i = 0; i < threadCount; i++) {
            stuff.add(() -> {
                try {
                    GenerateChunkData(logger, workIdCounter, totalChunkCount, w, this::GetBiomeCategory, _streamManager, topLeftChunkCenterX, topLeftChunkCenterZ, chunksPerRow);
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            });
        }

        // Whip up a thread pool, open the file streams...
        //ExecutorService threadPool = Executors.newFixedThreadPool(threadCount);
        _streamManager.OpenAllBiomeWriteStreams(GetBiomeCategories());

        try 
        {
            // And hit it!
            logger.info("Generating data...");
            this.GenerateChunkData(logger, workIdCounter, totalChunkCount, w, this::GetBiomeCategory, _streamManager, topLeftChunkCenterX, topLeftChunkCenterZ, chunksPerRow);
        }
        finally {
            //threadPool.shutdown();
            _streamManager.CloseAllBiomeWriteStreams();
            logger.info("Done generating data!");
        }
    }

    protected static final void GenerateChunkData(Logger logger, AtomicInteger workIdCounter, int maxWorkId, World w, Function<Biome, Biome> biomeCategoryLookup, BiomeCategoryStreamManager streamManager, int topLeftChunkCenterX, int topLeftChunkCenterZ, int chunksPerRow) throws IOException
    {
        long threadId = Thread.currentThread().getId();

        // Loop until we're out of work
        int workUnitId = 0;
        while((workUnitId = workIdCounter.getAndIncrement()) < maxWorkId)
        {
            // Find the chunk's relative position based on the work ID
            int chunkCountOffsetMultiplierX = workUnitId / chunksPerRow;
            int chunkCountOffsetMultiplierZ = workUnitId % chunksPerRow;

            // Calculate the center of the chunk to test
            int chunkX = chunkCountOffsetMultiplierX * 16 + topLeftChunkCenterX;
            int chunkZ = chunkCountOffsetMultiplierZ * 16 + topLeftChunkCenterZ;

            // Get the biome, FINALLY
            Biome b = w.getBiome(chunkX, 64, chunkZ);
            
            // Map the biome to a category and if it's Void, skip it.
            Biome biomeCategory = biomeCategoryLookup.apply(b);
            if(biomeCategory == Biome.THE_VOID) {
                continue;
            }

            // Logging!
            logger.info("[Thread #" + threadId + "] [Work Unit #" + workUnitId + "] [Biome: " + b.name() + "] [X: " + chunkX + "] [Z: " + chunkZ + "]");

            // Otherwise, grab the correct file stream for it...
            BufferedOutputStream stream = streamManager.get(biomeCategory);

            // Get the bytes for the X/Z and write them to the buffer
            if(stream != null)
                stream.write(getBytes(chunkX, chunkZ));
        }
    }
    protected Biome[] GetBiomeCategories() {
        return new Biome[] { Biome.PLAINS, Biome.FOREST, Biome.DESERT, Biome.MOUNTAINS, Biome.SWAMP, Biome.BEACH };
    }
    protected Biome GetBiomeCategory(Biome b) {
        switch (b) {
            // Plains
            case PLAINS:
            case SUNFLOWER_PLAINS:
            case SAVANNA:
            case SAVANNA_PLATEAU:
            case SNOWY_TUNDRA: {
                return Biome.PLAINS;
            }

            // Forest
            case FOREST:
            case BIRCH_FOREST:
            case WOODED_HILLS:
            case TAIGA:
            case TAIGA_HILLS:
            case TAIGA_MOUNTAINS:
            case BIRCH_FOREST_HILLS:
            case DARK_FOREST:
            case DARK_FOREST_HILLS:
            case GIANT_SPRUCE_TAIGA:
            case GIANT_SPRUCE_TAIGA_HILLS:
            case GIANT_TREE_TAIGA:
            case GIANT_TREE_TAIGA_HILLS:
            case SNOWY_TAIGA:
            case SNOWY_TAIGA_HILLS:
            case WOODED_MOUNTAINS:
            case FLOWER_FOREST:
            case TALL_BIRCH_FOREST:
            case TALL_BIRCH_HILLS:
            case SNOWY_TAIGA_MOUNTAINS: {
                return Biome.FOREST;
            }

            // Desert
            case DESERT:
            case DESERT_HILLS:
            case DESERT_LAKES: {
                return Biome.DESERT;
            }

            // Mountains
            case MOUNTAINS:
            case GRAVELLY_MOUNTAINS:
            case MODIFIED_GRAVELLY_MOUNTAINS:
            case SNOWY_MOUNTAINS:
            case MOUNTAIN_EDGE:
            case STONE_SHORE: {
                return Biome.MOUNTAINS;
            }

            // Swamp
            case SWAMP:
            case SWAMP_HILLS: {
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
                (byte)(x >>> 24),
                (byte)(x >>> 16),
                (byte)(x >>> 8),
                (byte)x,  
                (byte)(z >>> 24),
                (byte)(z >>> 16),
                (byte)(z >>> 8),
                (byte)z};
    }
}