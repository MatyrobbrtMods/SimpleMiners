package com.matyrobbrt.simpleminers.packsdatagen.simple;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.data.base.MinerResultProvider;
import com.matyrobbrt.simpleminers.data.base.TagProviderBuilder;
import com.matyrobbrt.simpleminers.data.base.result.ResultConsumer;
import com.matyrobbrt.simpleminers.packsdatagen.PackGenerator;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class SimplePackGenerator implements PackGenerator {
    protected final EnumSet<GeneratorType> types;
    public SimplePackGenerator(EnumSet<GeneratorType> types) {
        this.types = types;
    }
    public SimplePackGenerator(GeneratorType type1, GeneratorType... types) {
        this(EnumSet.of(type1, types));
    }

    public SimplePackGenerator() {
        this(EnumSet.allOf(GeneratorType.class));
    }

    protected DataGenerator generator;
    @Override
    public void gather(DataGenerator generator, ExistingFileHelper existingFileHelper, SideProvider sides) {
        this.generator = generator;

        ifEnabled(GeneratorType.RECIPES, () -> generator.addProvider(sides.includeServer(), new RecipeProvider(generator) {
            @Override
            protected void saveAdvancement(CachedOutput pOutput, JsonObject pAdvancementJson, Path pPath) {
            }

            @Override
            protected void buildCraftingRecipes(Consumer<FinishedRecipe> pFinishedRecipeConsumer) {
                addRecipes(pFinishedRecipeConsumer);
            }
        }));
        ifEnabled(GeneratorType.MINER_RESULTS, () -> generator.addProvider(sides.includeServer(), new MinerResultProvider(generator) {
            @Override
            protected void gather(ResultConsumer consumer) {
                addMinerResults(consumer, ops);
            }
        }));

        ifEnabled(GeneratorType.ITEM_TAGS, () -> {
            final var tags = TagProviderBuilder.builder(generator, Registry.ITEM_REGISTRY, SimpleMiners.MOD_ID, existingFileHelper);
            addItemTags(tags);
            generator.addProvider(sides.includeServer(), tags);
        });

        ifEnabled(GeneratorType.ITEM_MODELS, () -> generator.addProvider(sides.includeClient(), new ItemModelProvider(generator, SimpleMiners.MOD_ID, existingFileHelper) {
            @Override
            protected void registerModels() {
                addItemModels(this);
            }
        }));

        ifEnabled(GeneratorType.MINERS, () -> generator.addProvider(true, new DataProvider() {
            @Override
            public void run(CachedOutput pOutput) throws IOException {
                addMiners(pOutput, generator);
            }

            @Override
            public String getName() {
                return "Miners Generator";
            }
        }));
    }

    protected final void ifEnabled(GeneratorType type, Runnable ifEnabled) {
        if (types.contains(type)) ifEnabled.run();
    }

    protected void addMiners(CachedOutput cachedOutput, DataGenerator generator) throws IOException {}

    // region Server
    protected void addRecipes(Consumer<FinishedRecipe> consumer) {}
    protected void addItemTags(TagProviderBuilder<Item> provider) {}
    protected void addMinerResults(ResultConsumer consumer, RegistryOps<JsonElement> ops) {}
    // endregion

    // region Client
    protected void addItemModels(ItemModelProvider provider) {}
    // endregion

    protected enum GeneratorType {
        ITEM_MODELS, MINERS, RECIPES, ITEM_TAGS, MINER_RESULTS
    }
}
