package com.matyrobbrt.simpleminers.packsdatagen.compat;

import com.google.gson.JsonElement;
import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.data.base.result.ResultConsumer;
import com.matyrobbrt.simpleminers.data.base.result.ResultRecipeBuilder;
import com.matyrobbrt.simpleminers.packsdatagen.RegisterPack;
import com.matyrobbrt.simpleminers.packsdatagen.wood.WoodMinerResults;
import com.matyrobbrt.simpleminers.results.predicate.ResultPredicate;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;

@RegisterPack
@ParametersAreNonnullByDefault
public class IntegratedDynamicsGenerator extends CompatPackGenerator {
    public IntegratedDynamicsGenerator() {
        super("integrateddynamics", GeneratorType.MINER_RESULTS);
    }

    @Override
    protected void addMinerResults(ResultConsumer consumer, RegistryOps<JsonElement> ops) {
        ResultRecipeBuilder.builder("wood", modId)
                .addWithSamePredicate(ResultPredicate.inDimension(Level.OVERWORLD), builder -> new WoodMinerResults.Wood(
                        "menril", null, block("menril_log"), block("menril_leaves"), item("menril_sapling")
                ).add(builder))
                .save(consumer, new ResourceLocation(SimpleMiners.MOD_ID, "compat/integrateddynamics_wood"));
    }


}
