package com.matyrobbrt.simpleminers.packsdatagen.simple;

import com.google.gson.JsonElement;
import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.data.base.MinerTypeBuilder;
import com.matyrobbrt.simpleminers.data.base.SimpleShapedRecipeBuilder;
import com.matyrobbrt.simpleminers.data.base.result.ResultConsumer;
import com.matyrobbrt.simpleminers.data.base.result.ResultRecipeBuilder;
import com.matyrobbrt.simpleminers.miner.MinerType;
import com.matyrobbrt.simpleminers.miner.upgrade.MinerUpgradeType;
import com.matyrobbrt.simpleminers.packsdatagen.RegisterPack;
import com.matyrobbrt.simpleminers.results.modifier.ResultModifier;
import com.matyrobbrt.simpleminers.results.predicate.InDimensionPredicate;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.ItemExistsCondition;

import java.io.IOException;
import java.util.function.Consumer;

@RegisterPack("soil")
public final class SoilPackGenerator extends SimplePackGenerator {
    public SoilPackGenerator() {
        super(GeneratorType.MINERS, GeneratorType.RECIPES, GeneratorType.MINER_RESULTS);
    }

    @Override
    protected void addMinerResults(ResultConsumer consumer, RegistryOps<JsonElement> ops) {
        final Registry<Biome> biomes = ops.registry(Registry.BIOME_REGISTRY).orElseThrow();

        ResultRecipeBuilder.builder("soil")
                .addCopying(null, new InDimensionPredicate(Level.OVERWORLD), builder -> builder
                        .add(15, Items.DIRT)
                        .add(8, ResultModifier.biomeWeightBonus(
                                biomes.getOrCreateTag(Tags.Biomes.IS_DESERT), 15
                        ))
                        .add(7, Items.COARSE_DIRT)
                        .add(6, ResultModifier.biomeWeightBonus(
                                biomes.getOrCreateTag(BiomeTags.IS_TAIGA), 4
                        ), Items.PODZOL)
                        .add(5, ResultModifier.biomeWeightBonus(
                                biomes.getOrCreateTag(Tags.Biomes.IS_LUSH), 4
                        ), Items.ROOTED_DIRT))
                .save(consumer, new ResourceLocation(SimpleMiners.MOD_ID, "overworld_soil"));

        ResultRecipeBuilder.builder("soil")
                .addCopying(null, new InDimensionPredicate(Level.NETHER), builder -> builder
                        .addWithSameModifier(ResultModifier.biomeWeightBonus(
                                HolderSet.direct(biomes.getHolderOrThrow(Biomes.SOUL_SAND_VALLEY)), 7
                        ), b -> b.add(6, Items.SOUL_SAND)
                                .add(2, Items.SOUL_SOIL)))
                .save(consumer, new ResourceLocation(SimpleMiners.MOD_ID, "nether_soil"));
    }

    @Override
    protected void addMiners(CachedOutput cachedOutput, DataGenerator generator) throws IOException {
        MinerTypeBuilder.builder()
                .translation("Soil Miner")
                .rollsPerOperation(1)
                .ticksPerOperation(300)
                .energy(new MinerType.EnergyInfo(30))
                .upgrades(upgradeBuilder -> upgradeBuilder
                        .add(MinerUpgradeType.SPEED, builder -> builder
                                .add("timeDecrease", 25)
                                .add("energyUsage", 30))
                        .add(MinerUpgradeType.ENERGY, builder -> builder
                                .add("usageDecrease", 8)))
                .model(new MinerType.ModelData(new ResourceLocation("textures/block/dirt.png")))
                .save("soil", generator, cachedOutput);
    }

    @Override
    protected void addRecipes(Consumer<FinishedRecipe> consumer) {
        final ResourceLocation minerId = new ResourceLocation(SimpleMiners.MOD_ID, "soil_miner");

        ConditionalRecipe.builder()
                .addCondition(new ItemExistsCondition(minerId))
                .addRecipe(new SimpleShapedRecipeBuilder(minerId, 1)
                        .pattern("DSD")
                        .pattern("aCa")
                        .pattern("sDs")
                        .define('D', ItemTags.DIRT)
                        .define('S', Items.IRON_SHOVEL)
                        .define('a', ItemTags.SAND)
                        .define('s', Items.SOUL_SOIL)
                        .define('C', Tags.Items.CHESTS)
                        .finish(new ResourceLocation("hi")))
                .build(consumer, new ResourceLocation(SimpleMiners.MOD_ID, "soil_miner"));
    }
}
