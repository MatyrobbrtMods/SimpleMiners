package com.matyrobbrt.simpleminers.packsdatagen.ore;

import com.google.gson.JsonElement;
import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.data.base.MinerResultProvider;
import com.matyrobbrt.simpleminers.data.base.result.ResultConsumer;
import com.matyrobbrt.simpleminers.data.base.result.ResultRecipeBuilder;
import com.matyrobbrt.simpleminers.results.modifier.BiomeWeightBonusModifier;
import com.matyrobbrt.simpleminers.results.modifier.CatalystWeightBonusModifier;
import com.matyrobbrt.simpleminers.results.modifier.ResultModifier;
import com.matyrobbrt.simpleminers.results.predicate.InDimensionPredicate;
import com.matyrobbrt.simpleminers.results.predicate.ResultPredicate;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;

public class OreMinerResults extends MinerResultProvider {
    public OreMinerResults(DataGenerator pGenerator, RegistryOps<JsonElement> ops) {
        super(pGenerator, ops);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void gather(@NotNull ResultConsumer consumer) {
        ResultRecipeBuilder.builder("ore")
                .addCopying(null, new InDimensionPredicate(Level.NETHER), builder -> builder
                        .add(15, Items.NETHER_QUARTZ_ORE)
                        .add(8, Items.NETHER_GOLD_ORE)
                        .add(1, Items.ANCIENT_DEBRIS))
                .save(consumer, mod("nether_ores"));

        final Registry<Biome> biomes = ops.registry(Registry.BIOME_REGISTRY).orElseThrow();

        final ResultModifier gemsCatalyst = ResultModifier.catalystWeightBonus(new CatalystWeightBonusModifier.Entry(
                Registry.ITEM.getOrCreateTag(OrePackGenerator.GEM_CATALYSTS),
                2, false
        ));

        final ResultPredicate overworld = new InDimensionPredicate(Level.OVERWORLD);

        ResultRecipeBuilder.builder("ore")
                .addCopying(null, overworld, builder -> builder
                        .add(10, Items.COAL_ORE)
                        .add(8, Items.COPPER_ORE)
                        .add(7, Items.IRON_ORE, Items.REDSTONE_ORE, Items.LAPIS_ORE)
                        .add(3, gemsCatalyst, Items.DIAMOND_ORE)
                        .add(5, biomeBonus(2, biomes.getOrCreateTag(BiomeTags.IS_BADLANDS)), Items.GOLD_ORE)
                        .add(1, biomeBonus(1, biomes.getOrCreateTag(Tags.Biomes.IS_MOUNTAIN)).and(gemsCatalyst), Items.EMERALD_ORE))
                .save(consumer, mod("overworld_ores"));
    }

    private static ResultModifier biomeBonus(int amount, HolderSet<Biome> biomes) {
        return ResultModifier.biomeWeightBonus(new BiomeWeightBonusModifier.BonusEntry(biomes, amount));
    }

    public static ResourceLocation mod(String path) {
        return new ResourceLocation(SimpleMiners.MOD_ID, path);
    }
}
