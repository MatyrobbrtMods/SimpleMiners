package com.matyrobbrt.simpleminers.packsdatagen.compat;

import com.google.gson.JsonElement;
import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.data.base.result.ResultConsumer;
import com.matyrobbrt.simpleminers.data.base.result.ResultRecipeBuilder;
import com.matyrobbrt.simpleminers.packsdatagen.DatagenCheating;
import com.matyrobbrt.simpleminers.packsdatagen.RegisterPack;
import com.matyrobbrt.simpleminers.packsdatagen.simple.SimplePackGenerator;
import com.matyrobbrt.simpleminers.packsdatagen.wood.WoodMinerResults;
import com.matyrobbrt.simpleminers.results.predicate.ResultPredicate;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@RegisterPack(IntegratedDynamicsGenerator.ID)
public class IntegratedDynamicsGenerator extends SimplePackGenerator {
    public static final String ID = "integrateddynamics";
    public IntegratedDynamicsGenerator() {
        super(GeneratorType.MINER_RESULTS);
    }

    @Override
    protected void addMinerResults(ResultConsumer consumer, RegistryOps<JsonElement> ops) {
        ResultRecipeBuilder.builder("wood", "")
                .addWithSamePredicate(ResultPredicate.inDimension(Level.OVERWORLD), builder -> new WoodMinerResults.Wood(
                        "menril", null, b("menril_log"), b("menril_leaves"), i("menril_sapling")
                ).add(builder))
                .save(consumer, new ResourceLocation(SimpleMiners.MOD_ID, "compat/integrateddynamics_wood"));
    }

    private static Block b(String name) {
        return DatagenCheating.block(new ResourceLocation(ID, name));
    }
    @SuppressWarnings("SameParameterValue")
    private static Item i(String name) {
        return DatagenCheating.item(new ResourceLocation(ID, name));
    }
}
