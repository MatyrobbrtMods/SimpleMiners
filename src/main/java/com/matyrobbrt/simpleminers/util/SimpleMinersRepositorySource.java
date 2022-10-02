package com.matyrobbrt.simpleminers.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.matyrobbrt.simpleminers.SimpleMiners;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.FolderPackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class SimpleMinersRepositorySource implements RepositorySource {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOG = LoggerFactory.getLogger(SimpleMinersRepositorySource.class);

    public static final SimpleMinersRepositorySource INSTANCE = new SimpleMinersRepositorySource(SimpleMiners.BASE_PATH.resolve("packs"));

    private final File directory;
    private final File builtInDir;
    private final Predicate<String> builtinTester;

    private SimpleMinersRepositorySource(Path directory) {
        directory = directory.toAbsolutePath();
        this.directory = directory.toFile();
        this.builtInDir = new File(this.directory, "builtins");

        if (Files.notExists(directory)) {
            LOG.info("Generating new pack folder at {}.", directory);
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                LOG.error("Encountered exception creating resource pack folder: ", e);
                throw new RuntimeException(e);
            }
        }

        final Path configPath = directory.resolve("builtins.json");
        try {
            if (Files.exists(configPath)) {
                try (final var reader = Files.newBufferedReader(configPath)) {
                    final var json = GSON.fromJson(reader, JsonObject.class);
                    final boolean isBlacklist = json.get("mode").getAsString().equalsIgnoreCase("blacklist");
                    final List<String> packs = StreamSupport.stream(
                            json.getAsJsonArray("packs").spliterator(), false
                    ).map(JsonElement::getAsString).toList();
                    this.builtinTester = name -> isBlacklist != packs.contains(name);
                }
            } else {
                final var json = new JsonObject();
                json.addProperty("mode", "blacklist");
                json.add("packs", new JsonArray());
                Files.writeString(configPath, GSON.toJson(json));
                this.builtinTester = e -> true;
            }
        } catch (IOException exception) {
            LOG.error("Encountered exception setting up builtin pack config: ", exception);
            throw new RuntimeException(exception);
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
        packs.addAll(Stream.of(Objects.requireNonNull(builtInDir.listFiles()))
                .filter(it -> builtinTester.test(FilenameUtils.removeExtension(it.getName())))
                .map(it -> new PackEntry(it, isArchivePack(it, false)))
                .toList());
        return packs;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void loadPacks(Consumer<Pack> consumer, Pack.PackConstructor packConstructor) {
        LOG.info("Preparing pack injection...");
        int newPackCount = 0;
        int failedPacks = 0;

        final List<File> candidates = new ArrayList<>();
        candidates.addAll(List.of(Objects.requireNonNull(directory.listFiles())));
        candidates.addAll(Stream.of(Objects.requireNonNull(builtInDir.listFiles()))
                .filter(it -> builtinTester.test(FilenameUtils.removeExtension(it.getName())))
                .toList());

        candidates.remove(directory);
        candidates.remove(builtInDir);

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

    private Supplier<PackResources> createPackSupplier (File packFile) {
        return () -> packFile.isDirectory() ? new FolderPackResources(packFile) : new FilePackResources(packFile);
    }

    private boolean isArchivePack(File candidate, boolean logIssues) {
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

    private static boolean isFolderPack(File candidate, boolean logIssues) {
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

    private static boolean endsWithIgnoreCase(String str, String suffix) {
        final int suffixLength = suffix.length();
        return str.regionMatches(true, str.length() - suffixLength, suffix, 0, suffixLength);
    }

    public void copyDefaults(Path source) {
        final Path target = builtInDir.toPath();

        try (final Stream<Path> files = (Files.exists(target) ? Files.walk(target) : Stream.<Path>empty())
                .filter(it -> it.getFileName().toString().endsWith(".zip"))) {
            final Iterator<Path> it = files.sorted(Comparator.reverseOrder()).iterator();
            while (it.hasNext()) {
                Files.delete(it.next());
            }

            Files.createDirectories(target);
        } catch (IOException exception) {
            LOG.error("Encountered exception deleting builtIn miner packs: ", exception);
        }

        try (final Stream<Path> packs = Files.walk(source, 1)) {
            final Iterator<Path> it = packs.iterator();
            while (it.hasNext()) {
                final Path next = it.next();
                if (!Files.isDirectory(next)) {
                    Files.copy(next, target.resolve(next.getFileName().toString()));
                }
            }
        } catch (IOException exception) {
            LOG.error("Encountered exception copying builtIn packs: ", exception);
        }
    }

    public record PackEntry(File path, boolean isArchive) {}
}
