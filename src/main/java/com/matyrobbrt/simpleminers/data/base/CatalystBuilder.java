package com.matyrobbrt.simpleminers.data.base;

import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.world.item.Rarity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class CatalystBuilder {
    private final JsonObject json = new JsonObject();

    public static Group builder() {
        return new Group();
    }

    public CatalystBuilder maxStackSize(int maxStackSize) {
        json.addProperty("maxStackSize", maxStackSize);
        return this;
    }

    public CatalystBuilder fireResistant() {
        json.addProperty("fireResistant", true);
        return this;
    }

    public CatalystBuilder rarity(Rarity rarity) {
        json.addProperty("rarity", switch (rarity) {
            case COMMON -> "common";
            case EPIC -> "epic";
            case RARE -> "rare";
            case UNCOMMON -> "uncommon";
        });
        return this;
    }

    public CatalystBuilder translation(String translation) {
        json.addProperty("translation", translation);
        return this;
    }

    public static final class Group {
        private final Map<String, JsonObject> catalysts = new HashMap<>();

        public Group add(String name, Consumer<CatalystBuilder> consumer) {
            final var builder = new CatalystBuilder();
            consumer.accept(builder);
            catalysts.put(name, builder.json);
            return this;
        }

        public void save(DataGenerator gen, CachedOutput cache) throws IOException {
            DataProvider.saveStable(cache, MinerTypeBuilder.GSON.toJsonTree(catalysts), gen.getOutputFolder().resolve("catalysts.json"));
        }
    }
}
