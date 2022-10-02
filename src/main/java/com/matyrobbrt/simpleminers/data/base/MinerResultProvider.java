package com.matyrobbrt.simpleminers.data.base;

import com.google.gson.JsonElement;
import com.matyrobbrt.simpleminers.data.base.result.ResultConsumer;
import com.matyrobbrt.simpleminers.results.ResultSet;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import cpw.mods.modlauncher.api.LamdbaExceptionUtils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.file.Path;
import java.util.function.BiConsumer;

@ParametersAreNonnullByDefault
public abstract class MinerResultProvider implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();

    protected final DataGenerator dataGenerator;
    protected final RegistryOps<JsonElement> ops;

    public MinerResultProvider(DataGenerator dataGenerator, RegistryOps<JsonElement> ops) {
        this.dataGenerator = dataGenerator;
        this.ops = ops;
    }
    public MinerResultProvider(DataGenerator generator) {
        this(generator, RegistryOps.create(JsonOps.INSTANCE, RegistryAccess.builtinCopy()));
    }

    @Override
    public void run(final CachedOutput cache) {
        final Path outputFolder = this.dataGenerator.getOutputFolder();
        final String dataFolder = PackType.SERVER_DATA.getDirectory();;
        final ResourceLocation registryId = ResultSet.RESULTS_REGISTRY.location();
        final BiConsumer<ResourceLocation, ResultSet> consumer = LamdbaExceptionUtils.rethrowBiConsumer((id, resultSet) -> {
            final Path path = outputFolder.resolve(String.join("/", dataFolder, id.getNamespace(), registryId.getNamespace(), registryId.getPath(), id.getPath() + ".json"));
            final JsonElement json = ResultSet.REQUIRED_MOD_AWARE_CODEC.encodeStart(ops, resultSet)
                    .getOrThrow(false, msg -> LOGGER.error("Encountered exception serializing: {}", msg));
            DataProvider.saveStable(cache, json, path);
        });
        gather(consumer::accept);
    }

    protected abstract void gather(ResultConsumer consumer);

    @Override
    public @NotNull String getName() {
        return "Miner Results";
    }

}
