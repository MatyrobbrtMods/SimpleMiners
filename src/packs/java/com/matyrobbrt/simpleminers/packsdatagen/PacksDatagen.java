package com.matyrobbrt.simpleminers.packsdatagen;

import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.data.base.PackMCMetaProvider;
import com.mojang.logging.LogUtils;
import net.minecraft.DetectedVersion;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mod.EventBusSubscriber(modid = SimpleMiners.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PacksDatagen {
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    static void gatherData(final GatherDataEvent event) throws IOException {
        record GenData(DataGenerator generator, String packName, Path path) {}
        final var sides = new PackGenerator.SideProvider() {
            @Override
            public boolean includeClient() {
                return event.includeClient();
            }

            @Override
            public boolean includeServer() {
                return event.includeServer();
            }
        };

        final List<GenData> generators = new ArrayList<>();
        final ExistingFileHelper helper = event.getExistingFileHelper();

        final Path baseOut = Path.of(System.getProperty("simpleminers.baseOut"));

        collectGenerators().forEach((name, packGenerator) -> {
            final var generator = new DataGenerator(
                    baseOut.resolve(name),
                    List.of(),
                    DetectedVersion.tryDetectVersion(),
                    true
            );
            packGenerator.gather(
                    generator, helper, sides
            );

            generator.addProvider(true, new PackMCMetaProvider(generator, "SimpleMiners %s pack".formatted(name)));

            generators.add(new GenData(generator, name, generator.getOutputFolder()));
        });

        for (final var gen : generators) {
            LOGGER.info("Started generator for pack {}", gen.packName());
            gen.generator().run();
        }

        final List<Path> generatedPaths = generators.stream().map(it -> it.path().toAbsolutePath()).collect(Collectors.toCollection(ArrayList::new));
        generatedPaths.add(event.getGenerator().getOutputFolder());
        try (Stream<Path> stream = Files.walk(event.getGenerator().getOutputFolder(), 1)
                .filter(it -> !generatedPaths.contains(it.toAbsolutePath()))) {
            final var it = stream.iterator();
            while (it.hasNext()) {
                FileUtils.deleteDirectory(it.next().toFile());
            }
        }
    }

    private static Map<String, PackGenerator> collectGenerators() {
        final Map<String, PackGenerator> packs = new HashMap<>();
        ModList.get().getAllScanData().stream()
                .flatMap(it -> it.getAnnotations().stream())
                .filter(it -> it.annotationType().equals(RegisterPack.TYPE))
                .forEach(it -> {
                    try {
                        final PackGenerator generator = (PackGenerator) Class.forName(it.clazz().getClassName())
                                .getConstructor()
                                .newInstance();

                        packs.put(it.annotationData().get("value").toString(), generator);
                    } catch (InstantiationException | ClassNotFoundException | IllegalAccessException |
                             InvocationTargetException | NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                });
        return packs;
    }
}
