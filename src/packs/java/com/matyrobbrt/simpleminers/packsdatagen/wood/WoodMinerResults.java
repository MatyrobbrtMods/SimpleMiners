package com.matyrobbrt.simpleminers.packsdatagen.wood;

import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.data.base.MinerResultProvider;
import com.matyrobbrt.simpleminers.data.base.result.ResultConsumer;
import com.matyrobbrt.simpleminers.data.base.result.ResultRecipeBuilder;
import com.matyrobbrt.simpleminers.results.ItemResult;
import com.matyrobbrt.simpleminers.results.modifier.ResultModifier;
import com.matyrobbrt.simpleminers.results.predicate.ResultPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.holdersets.OrHolderSet;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.List;

@ParametersAreNonnullByDefault
public class WoodMinerResults extends MinerResultProvider {
    public WoodMinerResults(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void gather(ResultConsumer consumer) {
        final Registry<Biome> biomes = registry(Registry.BIOME_REGISTRY);
        final HolderSet<Biome> isForest = biomes.getOrCreateTag(BiomeTags.IS_FOREST);
        final HolderSet<Biome> isDarkForest = HolderSet.direct(biomes.getHolderOrThrow(Biomes.DARK_FOREST));

        record Wood(String name, HolderSet<Biome> biomes, Block log, Block leaves, Item saplings) {}

        final List<Wood> woods = List.of(
                new Wood("oak", isForest, Blocks.OAK_LOG, Blocks.OAK_LEAVES, Items.OAK_SAPLING),
                new Wood("dark_oak", isDarkForest, Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_LEAVES, Items.DARK_OAK_SAPLING),
                new Wood("birch", isDarkForest, Blocks.BIRCH_LOG, Blocks.BIRCH_LEAVES, Items.BIRCH_SAPLING),
                new Wood("acacia", or(biomes.getHolderOrThrow(Biomes.SAVANNA), biomes.getHolderOrThrow(Biomes.SAVANNA_PLATEAU)), Blocks.ACACIA_LOG, Blocks.ACACIA_LEAVES, Items.ACACIA_SAPLING),
                new Wood("spruce", biomes.getOrCreateTag(BiomeTags.IS_TAIGA), Blocks.SPRUCE_LOG, Blocks.SPRUCE_LEAVES, Items.SPRUCE_SAPLING),
                new Wood("mangrove", HolderSet.direct(biomes.getHolderOrThrow(Biomes.MANGROVE_SWAMP)), Blocks.MANGROVE_LOG, Blocks.MANGROVE_LEAVES, Items.MANGROVE_LEAVES)
        );

        woods.forEach(it -> ResultRecipeBuilder.builder("wood")
                .addCopying(ResultModifier.biomeWeightBonus(
                        it.biomes(), 4
                ), ResultPredicate.inDimension(Level.OVERWORLD), builder -> builder
                        .add(8, it.log)
                        .add(4, it.leaves)
                        .add(2, it.saplings))
                .save(consumer, new ResourceLocation(SimpleMiners.MOD_ID, "wood/overworld/" + it.name)));

        ResultRecipeBuilder.builder("wood")
                .add(new ItemResult(
                        Items.APPLE.getDefaultInstance(), 1, ResultPredicate.inDimension(Level.OVERWORLD),
                        ResultModifier.biomeWeightBonus(isForest, 1)
                ))
                .save(consumer, new ResourceLocation(SimpleMiners.MOD_ID, "wood/apple"));

        ResultRecipeBuilder.builder("wood")
                .addCopying(null, ResultPredicate.inDimension(Level.NETHER), builder -> builder
                        .addWithSameModifier(ResultModifier.biomeWeightBonus(
                                HolderSet.direct(biomes.getHolderOrThrow(Biomes.WARPED_FOREST)), 4
                        ), b -> b
                                .add(8, Blocks.WARPED_STEM)
                                .add(4, Blocks.WARPED_WART_BLOCK)
                                .add(2, Items.WARPED_FUNGUS)
                                .add(1, Blocks.SHROOMLIGHT))
                        .addWithSameModifier(ResultModifier.biomeWeightBonus(
                                HolderSet.direct(biomes.getHolderOrThrow(Biomes.CRIMSON_FOREST)), 4
                        ), b -> b
                                .add(8, Blocks.CRIMSON_STEM)
                                .add(4, Blocks.CRIMSON_NYLIUM)
                                .add(2, Items.CRIMSON_FUNGUS)
                                .add(1, Blocks.SHROOMLIGHT)))
                .save(consumer, new ResourceLocation(SimpleMiners.MOD_ID, "nether_wood"));
    }

    @SafeVarargs
    private static <T> HolderSet<T> or(Holder<T>... holders) {
        return new OrHolderSet<>(Arrays.stream(holders)
                .<HolderSet<T>>map(HolderSet::direct)
                .toList());
    }
}
