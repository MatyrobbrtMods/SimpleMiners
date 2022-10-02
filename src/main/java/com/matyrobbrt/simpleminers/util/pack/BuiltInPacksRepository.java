package com.matyrobbrt.simpleminers.util.pack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import cpw.mods.modlauncher.api.LamdbaExceptionUtils;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@ParametersAreNonnullByDefault
public record BuiltInPacksRepository(Path directory) implements RepositorySource {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static BuiltInPacksRepository instance;

    @Override
    public void loadPacks(Consumer<Pack> pInfoConsumer, Pack.PackConstructor pInfoFactory) {
        try (final Stream<Path> stream = Files.walk(directory, 1)
                .filter(it -> !it.equals(directory) && it.getFileName().toString().endsWith(".zip"))) {
            final Predicate<String> builtinTester = builtInTester();

            final var it = stream.iterator();
            while (it.hasNext()) {
                final Path path = it.next();
                final String packName = FilenameUtils.removeExtension(path.getFileName().toString());

                if (SimpleMinersRepositorySource.INSTANCE.isPack(packName)) {
                    LOGGER.debug("Skipping builtIn pack '{}' as it is overriden by another pack.", packName);
                    continue;
                }

                if (!builtinTester.test(packName)) {
                    LOGGER.debug("Skipping builtIn pack '{}' as it is disabled.", packName);
                    continue;
                }

                final Pack pack = Pack.create(packName, true, LamdbaExceptionUtils.rethrowSupplier(() -> new ZipPack(path)),
                        pInfoFactory, Pack.Position.TOP, PackSource.BUILT_IN);

                if (pack != null) {
                    pInfoConsumer.accept(pack);
                    LOGGER.info("Loaded builtIn pack '{}'", packName);
                }
            }
        } catch (IOException exception) {
            LOGGER.error("Encountered exception setting up builtIn packs: ", exception);
            throw new RuntimeException(exception);
        }
    }

    public List<PackData> list() throws IOException {
        final List<PackData> packs = new ArrayList<>();
        final var tester = builtInTester();
        try (final Stream<Path> stream = Files.walk(directory, 1)
                .filter(it -> !it.equals(directory) && it.getFileName().toString().endsWith(".zip"))) {
            final var it = stream.iterator();
            while (it.hasNext()) {
                final Path path = it.next();
                final String packName = FilenameUtils.removeExtension(path.getFileName().toString());

                if (SimpleMinersRepositorySource.INSTANCE.isPack(packName)) {
                    packs.add(new PackData(packName, false, true));
                    continue;
                }

                if (!tester.test(packName)) {
                    packs.add(new PackData(packName, true, false));
                    continue;
                }
                packs.add(new PackData(packName, false, false));
            }
        }
        return packs;
    }

    public List<SimpleMinersRepositorySource.PackEntry> findPacks() throws IOException {
        final var tester = builtInTester().and(Predicate.not(SimpleMinersRepositorySource.INSTANCE::isPack));
        try (final var stream = Files.walk(directory, 1)) {
            return stream
                    .filter(it -> !it.equals(directory))
                    .filter(it -> tester.test(FilenameUtils.removeExtension(it.getFileName().toString())))
                    .map(path -> new SimpleMinersRepositorySource.PackEntry(new SimpleMinersRepositorySource.PathGetter() {
                        FileSystem fs;
                        @Override
                        public Path get(String name) throws IOException {
                            fs = FileSystems.newFileSystem(path);
                            return fs.getPath(name);
                        }

                        @Override
                        public void close() throws IOException {
                            fs.close();
                        }
                    }))
                    .toList();
        }
    }

    public Path getPack(String name) {
        return directory.resolve(name + ".zip");
    }

    public static Predicate<String> builtInTester() throws IOException {
        final Path configPath = SimpleMinersRepositorySource.INSTANCE.directory.toPath().resolve("builtins.json");
        try {
            if (Files.exists(configPath)) {
                try (final var reader = Files.newBufferedReader(configPath)) {
                    final var json = GSON.fromJson(reader, JsonObject.class);
                    final boolean isBlacklist = json.get("mode").getAsString().equalsIgnoreCase("blacklist");
                    final List<String> packs = StreamSupport.stream(
                            json.getAsJsonArray("packs").spliterator(), false
                    ).map(JsonElement::getAsString).toList();
                    final List<String> disabled = StreamSupport.stream(
                            GsonHelper.getAsJsonArray(json, "disabled", new JsonArray()).spliterator(), false
                    ).map(JsonElement::getAsString).toList();
                    return name -> isBlacklist != packs.contains(name) && !disabled.contains(name);
                }
            } else {
                final var json = new JsonObject();
                json.addProperty("mode", "blacklist");
                json.add("packs", new JsonArray());
                Files.writeString(configPath, GSON.toJson(json));
                return e -> true;
            }
        } catch (IOException exception) {
            LOGGER.error("Encountered exception setting up builtin pack config: ", exception);
            throw new RuntimeException(exception);
        }
    }

    public record PackData(String name, boolean isDisabled, boolean isOverridden) {}
}
