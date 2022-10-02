package com.matyrobbrt.simpleminers.util.pack;

import com.matyrobbrt.simpleminers.SimpleMiners;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.FolderPackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SimpleMinersRepositorySource implements RepositorySource {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleMinersRepositorySource.class);

    public static final SimpleMinersRepositorySource INSTANCE = new SimpleMinersRepositorySource(SimpleMiners.BASE_PATH.resolve("packs"));

    public final File directory;

    private SimpleMinersRepositorySource(Path directory) {
        directory = directory.toAbsolutePath();
        this.directory = directory.toFile();

        if (Files.notExists(directory)) {
            LOG.info("Generating new pack folder at {}.", directory);
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                LOG.error("Encountered exception creating resource pack folder: ", e);
                throw new RuntimeException(e);
            }
        }
    }

    public List<PackEntry> findCandidates() {
        final List<PackEntry> packs = new ArrayList<>();
        for (File packCandidate : Objects.requireNonNull(directory.listFiles())) {
            final boolean isArchivePack = isArchivePack(packCandidate, false);
            final boolean isFolderPack = !isArchivePack && isFolderPack(packCandidate, false);

            if (isArchivePack || isFolderPack) {
                packs.add(new PackEntry(packCandidate, isArchivePack));
            }
        }
        return packs;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void loadPacks(Consumer<Pack> consumer, Pack.PackConstructor packConstructor) {
        LOG.info("Preparing pack injection...");
        int newPackCount = 0;
        int failedPacks = 0;

        final List<File> candidates = new ArrayList<>(List.of(Objects.requireNonNull(directory.listFiles())));
        candidates.remove(directory);

        for (File packCandidate : candidates) {
            final boolean isArchivePack = isArchivePack(packCandidate, false);
            if (packCandidate.isFile() && !isArchivePack) continue;

            final boolean isFolderPack = !isArchivePack && isFolderPack(packCandidate, false);
            final String typeName = isArchivePack ? "archive" : isFolderPack ? "folder" : "invalid";

            if (isArchivePack || isFolderPack) {
                final String packName = packCandidate.getName();
                final Pack pack = Pack.create(packName, true, createPackSupplier(packCandidate), packConstructor, Pack.Position.TOP, PackSource.BUILT_IN);

                if (pack != null) {
                    consumer.accept(pack);
                    newPackCount++;
                    LOG.info("Loaded {} from {}.", typeName, packCandidate.getAbsolutePath());
                }
            } else {
                LOG.error("Skipping {}. It is not a valid pack!", packCandidate.getAbsolutePath());
                isArchivePack(packCandidate, true);
                isFolderPack(packCandidate, true);
                failedPacks++;
            }
        }

        LOG.info("Successfully injected {}/{} packs from {}.", newPackCount, newPackCount + failedPacks, directory.getAbsolutePath());
    }

    private Supplier<PackResources> createPackSupplier(File packFile) {
        return () -> packFile.isDirectory() ? new FolderPackResources(packFile) : new FilePackResources(packFile);
    }

    public static boolean isArchivePack(File candidate, boolean logIssues) {
        if (candidate.isFile()) {
            final String fileName = candidate.getName();
            boolean isZipCompatibleArchive = endsWithIgnoreCase(fileName, ".zip") || endsWithIgnoreCase(fileName, ".jar");
            if (!isZipCompatibleArchive && logIssues) {
                LOG.warn("Can not load {} as an archive. It must be a .zip or .jar file!", candidate.getAbsolutePath());
            }
            return isZipCompatibleArchive;
        } else if (logIssues) {
            LOG.warn("Can not load {} as an archive. It is not a file.", candidate.getAbsolutePath());
        }

        return false;
    }

    public static boolean isFolderPack(File candidate, boolean logIssues) {
        if (candidate.isDirectory()) {
            if (new File(candidate, "pack.mcmeta").isFile()) {
                return true;
            } else if (logIssues) {
                LOG.warn("Can not load {} as a folder pack. It is missing a pack.mcmeta file!", candidate.getAbsolutePath());
            }
        } else if (logIssues) {
            LOG.warn("Can not load {} as folder. It is not a directory.", candidate.getAbsolutePath());
        }
        return false;
    }

    public boolean isPack(String name) {
        return isFolderPack(new File(directory, name), false) || isArchivePack(new File(directory, name + ".zip"), false);
    }

    private static boolean endsWithIgnoreCase(String str, String suffix) {
        final int suffixLength = suffix.length();
        return str.regionMatches(true, str.length() - suffixLength, suffix, 0, suffixLength);
    }

    public record PackEntry(PathGetter pathGetter) {
        public PackEntry(File file, boolean isArchive) {
            this(new PathGetter() {
                FileSystem fs;
                @Override
                public Path get(String name) throws IOException, URISyntaxException {
                    fs = !isArchive ? file.toPath().getFileSystem() : FileSystems.newFileSystem(new URI("jar:file", file.toURI().getPath(), null), new HashMap<>());
                    return fs.getPath(name);
                }

                @Override
                public void close() throws IOException {
                    if (isArchive) fs.close();
                }
            });
        }
    }

    public interface PathGetter {
        Path get(String name) throws IOException, URISyntaxException;

        void close() throws IOException;
    }
}
