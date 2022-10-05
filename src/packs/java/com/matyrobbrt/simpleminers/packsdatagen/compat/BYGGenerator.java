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
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.data.ExistingFileHelper;
import potionstudios.byg.common.block.BYGBlocks;
import potionstudios.byg.common.world.biome.BYGBiomes;
import potionstudios.byg.reg.RegistryObject;

import javax.annotation.ParametersAreNonnullByDefault;

@RegisterPack("byg")
@ParametersAreNonnullByDefault
public class BYGGenerator implements PackGenerator {
    @Override
    public void gather(DataGenerator generator, ExistingFileHelper existingFileHelper, SideProvider sides) {
        generator.addProvider(sides.includeServer(), new MinerResultProvider(generator) {
            private Registry<Biome> biomes;
            @Override
            protected void gather(ResultConsumer consumer) {
                biomes = registry(Registry.BIOME_REGISTRY);

                ResultRecipeBuilder.builder("ore", "byg")
                        .addCopying(null, new InDimensionPredicate(Level.NETHER), builder -> builder
                                .add(6, inBiome(BYGBiomes.WAILING_GARTH), BYGBlocks.EMERALDITE_ORE)
                                .add(6, inBiome(BYGBiomes.BRIMSTONE_CAVERNS), BYGBlocks.ANTHRACITE_ORE)
                                .add(2, inBiome(BYGBiomes.EMBUR_BOG), BYGBlocks.PENDORITE_ORE))

                        .addCopying(null, new InDimensionPredicate(Level.END), builder -> builder
                                .add(5, inBiome(BYGBiomes.VISCAL_ISLES), BYGBlocks.AMETRINE_ORE)
                                .add(7, inBiome(BYGBiomes.CRYPTIC_WASTES), BYGBlocks.CRYPTIC_REDSTONE_ORE))
                        .save(consumer, new ResourceLocation(SimpleMiners.MOD_ID, "compat/byg_ores"));

                final ResultPredicate etherealIslands = inBiome(BYGBiomes.ETHEREAL_ISLANDS);
                ResultRecipeBuilder.builder("soil", "byg")
                        .addCopying(null, new InDimensionPredicate(Level.END), builder -> builder
                                .add(7, etherealIslands, BYGBlocks.ETHER_SOIL.get()))
                                .add(2, etherealIslands, BYGBlocks.ETHER_GRASS.get())
                        .save(consumer, new ResourceLocation(SimpleMiners.MOD_ID, "compat/byg_soils"));

                ResultRecipeBuilder.builder("stone", "byg")
                        .addCopying(null, new InDimensionPredicate(Level.END), builder -> builder
                                .add(20, etherealIslands, BYGBlocks.ETHER_STONE.get())
                                .add(6, BYGBlocks.PURPUR_STONE.get()))

                        .addCopying(null, new InDimensionPredicate(Level.NETHER), builder -> builder
                                .add(20, inBiome(BYGBiomes.BRIMSTONE_CAVERNS), BYGBlocks.BRIMSTONE.get())
                                .add(20, inBiome(BYGBiomes.MAGMA_WASTES), BYGBlocks.MAGMATIC_STONE.get()))

                        .addCopying(null, new InDimensionPredicate(Level.OVERWORLD), builder -> builder
                                .add(6, BYGBlocks.SCORIA_STONE.get()))
                        .save(consumer, new ResourceLocation(SimpleMiners.MOD_ID, "compat/byg_stones"));
            }

            private ResultPredicate inBiome(ResourceKey<Biome> biomeResourceKey) {
                return ResultPredicate.inBiome(HolderSet.direct(biomes.getHolderOrThrow(biomeResourceKey)));
            }
        });
    }
}
