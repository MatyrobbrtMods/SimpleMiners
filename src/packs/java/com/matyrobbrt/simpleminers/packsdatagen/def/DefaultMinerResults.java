package com.matyrobbrt.simpleminers.packsdatagen.def;

import com.google.gson.JsonElement;
import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.data.base.MinerResultProvider;
import com.matyrobbrt.simpleminers.data.base.ResultConsumer;
import com.matyrobbrt.simpleminers.data.base.ResultRecipeBuilder;
import com.matyrobbrt.simpleminers.results.modifier.BiomeWeightBonusModifier;
import com.matyrobbrt.simpleminers.results.modifier.CatalystWeightBonusModifier;
import com.matyrobbrt.simpleminers.results.modifier.ResultModifier;
import com.matyrobbrt.simpleminers.results.predicate.InDimensionPredicate;
import com.matyrobbrt.simpleminers.results.predicate.ResultPredicate;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.resource.ore.OreType;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;

public class DefaultMinerResults extends MinerResultProvider {
    public DefaultMinerResults(DataGenerator pGenerator, RegistryOps<JsonElement> ops) {
        super(pGenerator, ops);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void gather(@NotNull ResultConsumer consumer) {
        ResultRecipeBuilder.builder("ore")
                .addCopying(null, new InDimensionPredicate(Level.NETHER), builder -> builder
                        .add(6, Items.NETHER_QUARTZ_ORE)
                        .add(4, Items.NETHER_GOLD_ORE)
                        .add(1, Items.ANCIENT_DEBRIS))
                .save(consumer, mod("nether_ores"));

        final Registry<Biome> biomes = ops.registry(Registry.BIOME_REGISTRY).orElseThrow();

        final ResultModifier gemsCatalyst = ResultModifier.catalystWeightBonus(new CatalystWeightBonusModifier.Entry(
                Registry.ITEM.getOrCreateTag(DefaultMinerTags.GEM_CATALYSTS),
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

        // region Mekanism

        final ResultModifier mekCatalyst = ResultModifier.catalystWeightBonus(new CatalystWeightBonusModifier.Entry(
                Registry.ITEM.getOrCreateTag(DefaultMinerTags.MEK_CATALYSTS),
                1, false
        ));

        ResultRecipeBuilder.builder("ore", "mekanism")
                .addCopying(mekCatalyst, overworld, builder -> builder
                        .add(6, item(OreType.OSMIUM))
                        .add(5, item(OreType.TIN))
                        .add(4, item(OreType.FLUORITE), item(OreType.LEAD))
                        .add(3, item(OreType.URANIUM)))
                .save(consumer, mod("compat/mekanism_ore"));

        // endregion
    }

    private static Item item(OreType oreType) {
        return MekanismBlocks.ORES.get(oreType).stone().asItem();
    }

    private static ResultModifier biomeBonus(int amount, HolderSet<Biome> biomes) {
        return ResultModifier.biomeWeightBonus(new BiomeWeightBonusModifier.BonusEntry(biomes, amount));
    }

    public static ResourceLocation mod(String path) {
        return new ResourceLocation(SimpleMiners.MOD_ID, path);
    }
}
