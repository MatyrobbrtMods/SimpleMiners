package com.matyrobbrt.simpleminers.util.pack;

import net.minecraftforge.resource.PathPackResources;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class ZipPack extends PathPackResources {
    private static final ThreadLocal<FileSystem> SYSTEM = new ThreadLocal<>();

    private final FileSystem fs;

    public ZipPack(Path zip) throws IOException {
        super(zip.toString(), getRoot(zip));
        this.fs = SYSTEM.get();
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

    private static Path getRoot(Path zip) throws IOException {
        final var fs = FileSystems.newFileSystem(zip);
        SYSTEM.set(fs);
        return fs.getPath("/");
    }
}
