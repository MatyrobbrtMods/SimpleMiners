package com.matyrobbrt.simpleminers.data.base;

import com.google.gson.JsonObject;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.SharedConstants;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.server.packs.PackType;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PackMCMetaProvider implements DataProvider {
    private final DataGenerator dataGenerator;
    private final String packDesc;

    public PackMCMetaProvider(DataGenerator dataGenerator, String packDesc) {
        this.dataGenerator = dataGenerator;
        this.packDesc = packDesc;
    }

    @Override
    public void run(CachedOutput pOutput) throws IOException {
        final var json = new JsonObject();
        {
            final var pack = new JsonObject();
            pack.addProperty("description", packDesc);
            pack.addProperty("pack_format", PackType.CLIENT_RESOURCES.getVersion(SharedConstants.getCurrentVersion()));
            pack.addProperty("forge:resource_pack_format", PackType.CLIENT_RESOURCES.getVersion(SharedConstants.getCurrentVersion()));
            pack.addProperty("forge:data_pack_format", PackType.SERVER_DATA.getVersion(SharedConstants.getCurrentVersion()));

            json.add("pack", pack);
        }
        DataProvider.saveStable(pOutput, json, dataGenerator.getOutputFolder().resolve("pack.mcmeta"));
    }

    @Override
    public String getName() {
        return "pack.mcmeta";
    }
}
