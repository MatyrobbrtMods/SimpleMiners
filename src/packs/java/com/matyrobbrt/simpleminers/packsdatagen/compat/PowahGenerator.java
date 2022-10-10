package com.matyrobbrt.simpleminers.packsdatagen.compat;

import com.google.gson.JsonElement;
import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.data.base.result.ResultConsumer;
import com.matyrobbrt.simpleminers.data.base.result.ResultRecipeBuilder;
import com.matyrobbrt.simpleminers.packsdatagen.RegisterPack;
import com.matyrobbrt.simpleminers.results.predicate.InDimensionPredicate;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;

@RegisterPack
@ParametersAreNonnullByDefault
public class PowahGenerator extends CompatPackGenerator {
    public PowahGenerator() {
        super("powah", GeneratorType.MINER_RESULTS);
    }

    @Override
    protected void addMinerResults(ResultConsumer consumer, RegistryOps<JsonElement> ops) {
        ResultRecipeBuilder.builder("ore", modId)
                .addCopying(null, new InDimensionPredicate(Level.OVERWORLD), builder -> builder
                        .add(4, item("uraninite_ore_poor"))
                        .add(3, item("uraninite_ore"))
                        .add(2, item("uraninite_ore_dense")))
                .save(consumer, new ResourceLocation(SimpleMiners.MOD_ID, "compat/powah_ores"));
    }
}
