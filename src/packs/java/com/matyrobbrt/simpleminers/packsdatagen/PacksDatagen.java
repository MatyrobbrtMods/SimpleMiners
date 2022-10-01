package com.matyrobbrt.simpleminers.packsdatagen;

import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.data.base.PackMCMetaProvider;
import net.minecraft.DetectedVersion;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = SimpleMiners.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PacksDatagen {
    @SubscribeEvent
    static void gatherData(final GatherDataEvent event) throws IOException {
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

        final List<DataGenerator> generators = new ArrayList<>();
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

            generators.add(generator);
        });

        for (final var gen : generators) {
            gen.run();
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
