package com.matyrobbrt.simpleminers.packsdatagen;

import com.google.gson.JsonObject;
import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.data.base.MinerResultProvider;
import com.matyrobbrt.simpleminers.data.base.TagProviderBuilder;
import com.matyrobbrt.simpleminers.data.base.result.ResultConsumer;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class SimplePackGenerator implements PackGenerator {
    @Override
    public void gather(DataGenerator generator, ExistingFileHelper existingFileHelper, SideProvider sides) {
        generator.addProvider(sides.includeServer(), new RecipeProvider(generator) {
            @Override
            protected void saveAdvancement(CachedOutput pOutput, JsonObject pAdvancementJson, Path pPath) {
            }

            @Override
            protected void buildCraftingRecipes(Consumer<FinishedRecipe> pFinishedRecipeConsumer) {
                addRecipes(pFinishedRecipeConsumer);
            }
        });
        generator.addProvider(sides.includeServer(), new MinerResultProvider(generator) {
            @Override
            protected void gather(ResultConsumer consumer) {
                addMinerResults(consumer);
            }
        });

        final var tags = TagProviderBuilder.builder(generator, Registry.ITEM_REGISTRY, SimpleMiners.MOD_ID, existingFileHelper);
        addItemTags(tags);
        generator.addProvider(sides.includeServer(), tags);

        generator.addProvider(sides.includeClient(), new ItemModelProvider(generator, SimpleMiners.MOD_ID, existingFileHelper) {
            @Override
            protected void registerModels() {
                addItemModels(this);
            }
        });

        generator.addProvider(true, new DataProvider() {
            @Override
            public void run(CachedOutput pOutput) throws IOException {
                addMiners(pOutput, generator);
            }

            @Override
            public String getName() {
                return "Miners Generator";
            }
        });
    }

    protected void addMiners(CachedOutput cachedOutput, DataGenerator generator) throws IOException {}

    // region Server
    protected void addRecipes(Consumer<FinishedRecipe> consumer) {}
    protected void addItemTags(TagProviderBuilder<Item> provider) {}
    protected void addMinerResults(ResultConsumer consumer) {}
    // endregion

    // region Client
    protected void addItemModels(ItemModelProvider provider) {}
    // endregion
}
