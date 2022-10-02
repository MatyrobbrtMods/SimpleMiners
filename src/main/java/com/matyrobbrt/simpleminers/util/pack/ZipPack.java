package com.matyrobbrt.simpleminers.util.pack;

import net.minecraftforge.resource.PathPackResources;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class ZipPack extends PathPackResources {
    private final FileSystem fs;

    public ZipPack(Path zip) throws IOException {
        this(FileSystems.newFileSystem(zip), zip);
    }

    private ZipPack(FileSystem fs, Path zip) {
        super(zip.toString(), fs.getPath("/"));
        this.fs = fs;
    }

    @Override
    public void close() {
        super.close();
        try {
            fs.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
