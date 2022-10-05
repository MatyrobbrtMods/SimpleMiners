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
import com.matyrobbrt.simpleminers.results.predicate.PositionPredicate;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.ItemExistsCondition;

import java.io.IOException;
import java.util.function.Consumer;

@RegisterPack("stone")
public class StonePackGenerator extends SimplePackGenerator {
    public StonePackGenerator() {
        super(GeneratorType.MINERS, GeneratorType.MINER_RESULTS, GeneratorType.RECIPES);
    }

    @Override
    protected void addMinerResults(ResultConsumer consumer, RegistryOps<JsonElement> ops) {
        final Registry<Biome> biomes = ops.registry(Registry.BIOME_REGISTRY).orElseThrow();

        ResultRecipeBuilder.builder("stone")
                .addCopying(null, new InDimensionPredicate(Level.OVERWORLD), builder -> builder
                        .add(15, Items.STONE.getDefaultInstance(), new PositionPredicate(
                                PositionPredicate.Comparison.GREATER_OR_EQUAL, PositionPredicate.Axis.Y, 0
                        ))
                        .add(15, Items.DEEPSLATE.getDefaultInstance(), new PositionPredicate(
                                PositionPredicate.Comparison.SMALLER, PositionPredicate.Axis.Y, 0
                        ))
                        .add(7, Blocks.GRANITE, Blocks.DIORITE)
                        .add(6, Blocks.ANDESITE)
                        .add(5, Blocks.TUFF)
                        .add(2, Blocks.CALCITE, Blocks.SMOOTH_BASALT))
                .save(consumer, new ResourceLocation(SimpleMiners.MOD_ID, "overworld_stones"));

        final var basaltDeltaModifier = ResultModifier.biomeWeightBonus(
                HolderSet.direct(biomes.getHolderOrThrow(Biomes.BASALT_DELTAS)), 5
        );
        ResultRecipeBuilder.builder("stone")
                .addCopying(null, new InDimensionPredicate(Level.NETHER), builder -> builder
                        .add(15, Blocks.NETHERRACK)
                        .add(6, basaltDeltaModifier, Blocks.BASALT)
                        .add(5, basaltDeltaModifier, Blocks.BLACKSTONE))
                .save(consumer, new ResourceLocation(SimpleMiners.MOD_ID, "nether_stones"));

        ResultRecipeBuilder.builder("stone")
                .addCopying(null, new InDimensionPredicate(Level.END), builder -> builder
                        .add(15, Blocks.END_STONE))
                .save(consumer, new ResourceLocation(SimpleMiners.MOD_ID, "end_stones"));
    }

    @Override
    protected void addMiners(CachedOutput cachedOutput, DataGenerator generator) throws IOException {
        MinerTypeBuilder.builder()
                .translation("Stone Miner")
                .rollsPerOperation(1)
                .ticksPerOperation(300)
                .energy(new MinerType.EnergyInfo(25))
                .upgrades(upgradeBuilder -> upgradeBuilder
                        .add(MinerUpgradeType.SPEED, builder -> builder
                                .add("timeDecrease", 30)
                                .add("energyUsage", 30))
                        .add(MinerUpgradeType.ENERGY, builder -> builder
                                .add("usageDecrease", 8)))
                .model(new MinerType.ModelData(new ResourceLocation("textures/block/stone.png")))
                .save("stone", generator, cachedOutput);
    }

    @Override
    protected void addRecipes(Consumer<FinishedRecipe> consumer) {
        final ResourceLocation minerId = new ResourceLocation(SimpleMiners.MOD_ID, "stone_miner");

        ConditionalRecipe.builder()
                .addCondition(new ItemExistsCondition(minerId))
                .addRecipe(new SimpleShapedRecipeBuilder(minerId, 1)
                        .pattern("GPD")
                        .pattern("SCS")
                        .pattern("GSD")
                        .define('G', Items.GRANITE)
                        .define('P', Items.IRON_PICKAXE)
                        .define('D', Items.DIORITE)
                        .define('S', Tags.Items.STONE)
                        .define('C', Tags.Items.CHESTS)
                        .finish(new ResourceLocation("hi")))
                .build(consumer, new ResourceLocation(SimpleMiners.MOD_ID, "stone_miner"));
    }
}
