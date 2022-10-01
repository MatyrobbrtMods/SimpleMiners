package com.matyrobbrt.simpleminers.data.base;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.matyrobbrt.simpleminers.miner.MinerType;
import com.matyrobbrt.simpleminers.miner.upgrade.MinerUpgradeType;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MinerTypeBuilder {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final JsonObject json = new JsonObject();
    public static MinerTypeBuilder builder() {
        return new MinerTypeBuilder();
    }

    public MinerTypeBuilder ticksPerOperation(int amount) {
        json.addProperty("ticksPerOperation", amount);
        return this;
    }

    public MinerTypeBuilder rollsPerOperation(int rolls) {
        json.addProperty("rollsPerOperation", rolls);
        return this;
    }

    public MinerTypeBuilder energy(MinerType.EnergyInfo energyInfo) {
        if (!energyInfo.enabled()) return this;
        final var energy = new JsonObject();
        energy.addProperty("usagePerTick", energyInfo.usagePerTick());
        energy.addProperty("capacity", energyInfo.capacity());
        energy.addProperty("ioRate", energyInfo.ioRate());
        json.add("energy", energy);
        return this;
    }

    public MinerTypeBuilder translation(String translation) {
        json.addProperty("translation", translation);
        return this;
    }

    public MinerTypeBuilder model(MinerType.ModelData modelData) {
        final var model = new JsonObject();
        model.addProperty("overlay", modelData.overlay().toString());
        json.add("modelData", model);
        return this;
    }

    public MinerTypeBuilder upgrades(Consumer<UpgradeBuilder> consumer) {
        final Map<String, JsonElement> map = new HashMap<>();
        consumer.accept(new UpgradeBuilder() {
            @Override
            public UpgradeBuilder add(MinerUpgradeType type, Consumer<MapBuilder> builderConsumer) {
                final Map<String, Object> data = new HashMap<>();
                builderConsumer.accept(new MapBuilder() {
                    @Override
                    public MapBuilder add(String key, Object value) {
                        data.put(key, value);
                        return this;
                    }
                });
                //noinspection ConstantConditions
                map.put(MinerUpgradeType.UPGRADE_TYPES.getKey(type).toString(), GSON.toJsonTree(data));
                return this;
            }
        });
        json.add("upgrades", GSON.toJsonTree(map));
        return this;
    }

    public void save(String name, DataGenerator gen, CachedOutput cache) throws IOException {
        DataProvider.saveStable(cache, json, gen.getOutputFolder().resolve("miners").resolve(name + ".json"));
    }

    public interface UpgradeBuilder {
        UpgradeBuilder add(MinerUpgradeType type, Consumer<MapBuilder> builderConsumer);

        interface MapBuilder {
            MapBuilder add(String key, Object value);
        }
    }
}
