package com.matyrobbrt.simpleminers.packsdatagen.compat;

import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.data.base.MinerResultProvider;
import com.matyrobbrt.simpleminers.data.base.result.ResultConsumer;
import com.matyrobbrt.simpleminers.data.base.result.ResultRecipeBuilder;
import com.matyrobbrt.simpleminers.packsdatagen.PackGenerator;
import com.matyrobbrt.simpleminers.packsdatagen.RegisterPack;
import com.matyrobbrt.simpleminers.results.predicate.InDimensionPredicate;
import com.matyrobbrt.simpleminers.results.predicate.ResultPredicate;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.data.ExistingFileHelper;
import potionstudios.byg.common.block.BYGBlocks;
import potionstudios.byg.common.world.biome.BYGBiomes;

import javax.annotation.ParametersAreNonnullByDefault;

@RegisterPack("byg")
@ParametersAreNonnullByDefault
public class BYGGenerator implements PackGenerator {
    @Override
    public void gather(DataGenerator generator, ExistingFileHelper existingFileHelper, SideProvider sides) {
        generator.addProvider(sides.includeServer(), new MinerResultProvider(generator) {
            @Override
            protected void gather(ResultConsumer consumer) {
                final Registry<Biome> biomes = registry(Registry.BIOME_REGISTRY);

                ResultRecipeBuilder.builder("ore", "byg")
                        .addCopying(null, new InDimensionPredicate(Level.NETHER), builder -> builder
                                .add(6, BYGBlocks.EMERALDITE_ORE, BYGBlocks.ANTHRACITE_ORE)
                                .add(3, BYGBlocks.EMERALDITE_ORE)
                                .add(2, BYGBlocks.PENDORITE_ORE))
                        .addCopying(null, new InDimensionPredicate(Level.END), builder -> builder
                                .add(5, BYGBlocks.AMETRINE_ORE, BYGBlocks.AMETRINE_ORE)
                                .add(7, BYGBlocks.CRYPTIC_REDSTONE_ORE))
                        .save(consumer, new ResourceLocation(SimpleMiners.MOD_ID, "compat/byg_ores"));

                final ResultPredicate etherealIslands = ResultPredicate.inBiome(HolderSet.direct(biomes.getHolderOrThrow(BYGBiomes.ETHEREAL_ISLANDS)));
                ResultRecipeBuilder.builder("soil", "byg")
                        .addCopying(null, new InDimensionPredicate(Level.END), builder -> builder
                                .add(7, etherealIslands, BYGBlocks.ETHER_SOIL.get()))
                                .add(2, etherealIslands, BYGBlocks.ETHER_GRASS.get())
                        .save(consumer, new ResourceLocation(SimpleMiners.MOD_ID, "compat/byg_soil"));
            }
        });
    }
}
