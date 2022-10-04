package com.matyrobbrt.simpleminers.packsdatagen.simple;

import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.data.base.MinerTypeBuilder;
import com.matyrobbrt.simpleminers.data.base.result.ResultConsumer;
import com.matyrobbrt.simpleminers.data.base.result.ResultRecipeBuilder;
import com.matyrobbrt.simpleminers.miner.MinerType;
import com.matyrobbrt.simpleminers.miner.upgrade.MinerUpgradeType;
import com.matyrobbrt.simpleminers.packsdatagen.RegisterPack;
import com.matyrobbrt.simpleminers.results.predicate.InDimensionPredicate;
import com.matyrobbrt.simpleminers.results.predicate.PositionPredicate;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.io.IOException;

@RegisterPack("stone")
public class StonePackGenerator extends SimplePackGenerator {

    @Override
    protected void addMinerResults(ResultConsumer consumer) {
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
                        .add(2, Blocks.CALCITE))
                .save(consumer, new ResourceLocation(SimpleMiners.MOD_ID, "overworld_stones"));

        ResultRecipeBuilder.builder("stone")
                .addCopying(null, new InDimensionPredicate(Level.NETHER), builder -> builder
                        .add(15, Blocks.NETHERRACK))
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
                .ticksPerOperation(400)
                .energy(new MinerType.EnergyInfo(20))
                .upgrades(upgradeBuilder -> upgradeBuilder
                        .add(MinerUpgradeType.SPEED, builder -> builder
                                .add("timeDecrease", 30)
                                .add("energyUsage", 30))
                        .add(MinerUpgradeType.ENERGY, builder -> builder
                                .add("usageDecrease", 8)))
                .model(new MinerType.ModelData(new ResourceLocation("textures/block/stone.png")))
                .save("stone", generator, cachedOutput);
    }
}
