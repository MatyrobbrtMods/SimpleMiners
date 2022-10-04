package com.matyrobbrt.simpleminers.packsdatagen.compat;

import com.matyrobbrt.simpleminers.Registration;
import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.data.base.CatalystBuilder;
import com.matyrobbrt.simpleminers.data.base.SimpleShapedRecipeBuilder;
import com.matyrobbrt.simpleminers.data.base.TagProviderBuilder;
import com.matyrobbrt.simpleminers.data.base.result.ResultConsumer;
import com.matyrobbrt.simpleminers.data.base.result.ResultRecipeBuilder;
import com.matyrobbrt.simpleminers.packsdatagen.RegisterPack;
import com.matyrobbrt.simpleminers.packsdatagen.SimplePackGenerator;
import com.matyrobbrt.simpleminers.results.modifier.CatalystWeightBonusModifier;
import com.matyrobbrt.simpleminers.results.modifier.ResultModifier;
import com.matyrobbrt.simpleminers.results.predicate.InDimensionPredicate;
import com.matyrobbrt.simpleminers.results.predicate.ResultPredicate;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.ore.OreType;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.ItemExistsCondition;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.function.Consumer;

import static com.matyrobbrt.simpleminers.packsdatagen.ore.OreMinerResults.mod;

@RegisterPack("mekanism")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MekanismGenerator extends SimplePackGenerator {
    private static final TagKey<Item> MEK_CATALYSTS = TagKey.create(Registry.ITEM_REGISTRY, mod("catalysts/mekanism"));

    @Override
    protected void addItemModels(ItemModelProvider provider) {
        provider.basicItem(new ResourceLocation(SimpleMiners.MOD_ID, "mekanism_catalyst"));
    }

    @Override
    protected void addRecipes(Consumer<FinishedRecipe> consumer) {
        final ResourceLocation mekanismCatalyst = new ResourceLocation(SimpleMiners.MOD_ID, "mekanism_catalyst");
        ConditionalRecipe.builder()
                .addCondition(new ItemExistsCondition(mekanismCatalyst))
                .addCondition(new ModLoadedCondition("mekanism"))
                .addRecipe(new SimpleShapedRecipeBuilder(mekanismCatalyst, 1)
                        .pattern("I")
                        .pattern("B")
                        .pattern("I")
                        .define('I', MekanismItems.INFUSED_ALLOY)
                        .define('B', Registration.CATALYST_BASE.get())
                        .finish(new ResourceLocation("hi")))
                .build(consumer, mod("mekanism_catalyst"));
    }

    @Override
    protected void addItemTags(TagProviderBuilder<Item> provider) {
        provider.tag(MEK_CATALYSTS, it -> it.addOptional(mod("mekanism_catalyst")));
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void addMinerResults(ResultConsumer consumer) {
        final ResultPredicate overworld = new InDimensionPredicate(Level.OVERWORLD);

        final ResultModifier mekCatalyst = ResultModifier.catalystWeightBonus(new CatalystWeightBonusModifier.Entry(
                Registry.ITEM.getOrCreateTag(MEK_CATALYSTS),
                1, false
        ));

        ResultRecipeBuilder.builder("ore", "mekanism")
                .addCopying(mekCatalyst, overworld, builder -> builder
                        .add(6, item(OreType.OSMIUM))
                        .add(5, item(OreType.TIN))
                        .add(4, item(OreType.FLUORITE), item(OreType.LEAD))
                        .add(3, item(OreType.URANIUM)))
                .save(consumer, mod("compat/mekanism_ores"));
    }

    @Override
    protected void addMiners(CachedOutput cachedOutput, DataGenerator generator) throws IOException {
        CatalystBuilder.builder()
                .add("mekanism_catalyst", builder -> builder
                        .rarity(Rarity.RARE)
                        .translation("Mekanism Catalyst"))
                .save(generator, cachedOutput);
    }

    private static Item item(OreType oreType) {
        return MekanismBlocks.ORES.get(oreType).stone().asItem();
    }
}
