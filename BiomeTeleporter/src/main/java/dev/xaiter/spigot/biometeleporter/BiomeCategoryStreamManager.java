package dev.xaiter.spigot.biometeleporter;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.block.Biome;

public class BiomeCategoryStreamManager extends HashMap<Biome, BufferedOutputStream> {
        
    // Member Variables & Accessors
    private final String _outputDirRoot;
    public String getOutputDirRoot() {
        return this._outputDirRoot;
    }
    
    // Constructors
    public BiomeCategoryStreamManager(String outputDirRoot, Biome[] biomeCategories) {
        this._outputDirRoot = outputDirRoot;
    }

    // Stream Management
    public RandomAccessFile GetRandomAccessFileStream(String biomeName) throws FileNotFoundException {
        Path path = this.GetBiomeCategoryFilePath(biomeName);
        return new RandomAccessFile(path.toString(), "r");
    }
    public void OpenAllBiomeWriteStreams(Biome[] biomes) throws IOException 
    {
        java.util.logging.Logger l = Bukkit.getLogger();
        for (Biome b : biomes) {
            Path filePath = GetBiomeCategoryFilePath(b.name());
            l.info("Opening " + filePath.toString());
            this.put(b, new BufferedOutputStream(new FileOutputStream(filePath.toString())));
        }
    }
    public void CloseAllBiomeWriteStreams() throws IOException
    {
        for (Biome b : this.keySet()) {
            BufferedOutputStream s = this.get(b);
            s.flush();
            s.close();
        }
    }
    
    // File Name Resolution
    public Path GetBiomeCategoryFilePath(String biomeName) {
        return Paths.get(this._outputDirRoot, biomeName + ".dat");
    }
}
