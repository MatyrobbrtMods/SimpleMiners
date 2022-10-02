package com.matyrobbrt.simpleminers.packsdatagen.compat;

import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.data.base.MinerResultProvider;
import com.matyrobbrt.simpleminers.data.base.result.ResultConsumer;
import com.matyrobbrt.simpleminers.data.base.result.ResultRecipeBuilder;
import com.matyrobbrt.simpleminers.packsdatagen.PackGenerator;
import com.matyrobbrt.simpleminers.packsdatagen.RegisterPack;
import com.matyrobbrt.simpleminers.results.modifier.BiomeWeightBonusModifier;
import com.matyrobbrt.simpleminers.results.modifier.ResultModifier;
import com.matyrobbrt.simpleminers.results.predicate.InDimensionPredicate;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.holdersets.AndHolderSet;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@RegisterPack("thermal")
@ParametersAreNonnullByDefault
public class ThermalGenerator implements PackGenerator {
    @Override
    public void gather(DataGenerator generator, ExistingFileHelper existingFileHelper, SideProvider sides) {
        generator.addProvider(sides.includeServer(), new MinerResultProvider(generator) {
            @Override
            protected void gather(ResultConsumer consumer) {
                final Registry<Biome> biomes = ops.registry(Registry.BIOME_REGISTRY).orElseThrow();
                ResultRecipeBuilder.builder("ore", "thermal")
                        .addCopying(null, new InDimensionPredicate(Level.END), builder -> builder
                                .add(5, ore("tin"))
                                .add(4, ore("sulfur"), ore("niter"))
                                .add(4, ore("lead"), ore("silver"), ore("nickel"))
                                .add(3, ore("cinnabar"))
                                .add(2, ResultModifier.biomeWeightBonus(
                                        new BiomeWeightBonusModifier.BonusEntry(new AndHolderSet<>(List.of(
                                                biomes.getOrCreateTag(BiomeTags.IS_RIVER),
                                                biomes.getOrCreateTag(BiomeTags.IS_OCEAN),
                                                biomes.getOrCreateTag(BiomeTags.IS_BEACH)
                                        )), 3)
                                ), ore("apatite")))
                        .save(consumer, new ResourceLocation(SimpleMiners.MOD_ID, "compat/thermal"));
            }
        });
    }

    private static Item ore(String name) {
        ((ForgeRegistry<Item>) ForgeRegistries.ITEMS).unfreeze();
        ForgeRegistries.ITEMS.register(new ResourceLocation("thermal", name + "_ore"), new Item(new Item.Properties()));
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation("thermal", name + "_ore"));
    }
}
