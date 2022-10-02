package com.matyrobbrt.simpleminers.packsdatagen.compat;

import com.matyrobbrt.simpleminers.Registration;
import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.data.base.CatalystBuilder;
import com.matyrobbrt.simpleminers.data.base.MinerResultProvider;
import com.matyrobbrt.simpleminers.data.base.SimpleShapedRecipeBuilder;
import com.matyrobbrt.simpleminers.data.base.TagProviderBuilder;
import com.matyrobbrt.simpleminers.data.base.result.ResultConsumer;
import com.matyrobbrt.simpleminers.data.base.result.ResultRecipeBuilder;
import com.matyrobbrt.simpleminers.packsdatagen.PackGenerator;
import com.matyrobbrt.simpleminers.packsdatagen.RegisterPack;
import com.matyrobbrt.simpleminers.results.modifier.CatalystWeightBonusModifier;
import com.matyrobbrt.simpleminers.results.modifier.ResultModifier;
import com.matyrobbrt.simpleminers.results.predicate.InDimensionPredicate;
import com.matyrobbrt.simpleminers.results.predicate.ResultPredicate;
import com.mojang.serialization.JsonOps;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.ore.OreType;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.ItemExistsCondition;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.function.Consumer;

import static com.matyrobbrt.simpleminers.packsdatagen.def.DefaultMinerResults.mod;

@RegisterPack("mekanism")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MekanismGenerator implements PackGenerator {
    private static final TagKey<Item> MEK_CATALYSTS = TagKey.create(Registry.ITEM_REGISTRY, mod("catalysts/mekanism"));

    @Override
    @SuppressWarnings("deprecation")
    public void gather(DataGenerator generator, ExistingFileHelper existingFileHelper, SideProvider sides) {
        generator.addProvider(sides.includeClient(), new ItemModelProvider(generator, SimpleMiners.MOD_ID, existingFileHelper) {
            @Override
            protected void registerModels() {
                basicItem(new ResourceLocation(SimpleMiners.MOD_ID, "mekanism_catalyst"));
            }
        });

        generator.addProvider(sides.includeServer(), TagProviderBuilder.builder(generator, Registry.ITEM_REGISTRY, existingFileHelper)
                .tag(MEK_CATALYSTS, it -> it.addOptional(mod("mekanism_catalyst"))));
        generator.addProvider(sides.includeServer(), new RecipeProvider(generator) {
            @Override
            protected void buildCraftingRecipes(Consumer<FinishedRecipe> pFinishedRecipeConsumer) {
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
                        .build(pFinishedRecipeConsumer, mod("mekanism_catalyst"));
            }
        });
        generator.addProvider(sides.includeServer(), new MinerResultProvider(generator, RegistryOps.create(
                JsonOps.INSTANCE, RegistryAccess.builtinCopy()
        )) {
            @Override
            protected void gather(ResultConsumer consumer) {
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
        });

        generator.addProvider(true, new DataProvider() {
            @Override
            public void run(CachedOutput pOutput) throws IOException {
                CatalystBuilder.builder()
                        .add("mekanism_catalyst", builder -> builder
                            .rarity(Rarity.RARE)
                            .translation("Mekanism Catalyst"))
                        .save(generator, pOutput);
            }

            @Override
            public String getName() {
                return "Mekanism Catalysts";
            }
        });
    }

    private static Item item(OreType oreType) {
        return MekanismBlocks.ORES.get(oreType).stone().asItem();
    }
}
