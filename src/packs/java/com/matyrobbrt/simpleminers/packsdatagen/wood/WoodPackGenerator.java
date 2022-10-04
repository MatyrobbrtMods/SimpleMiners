package com.matyrobbrt.simpleminers.packsdatagen.wood;

import com.google.gson.JsonElement;
import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.data.base.TagProviderBuilder;
import com.matyrobbrt.simpleminers.data.base.result.ResultConsumer;
import com.matyrobbrt.simpleminers.packsdatagen.RegisterPack;
import com.matyrobbrt.simpleminers.packsdatagen.simple.SimplePackGenerator;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.function.Consumer;

@RegisterPack("wood")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WoodPackGenerator extends SimplePackGenerator {
    static final TagKey<Item> LEAF_CATALYSTS = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(SimpleMiners.MOD_ID, "catalysts/leaf"));

    private DataGenerator generator;
    @Override
    public void gather(DataGenerator generator, ExistingFileHelper existingFileHelper, SideProvider sides) {
        this.generator = generator;
        super.gather(generator, existingFileHelper, sides);
    }

    @Override
    protected void addMiners(CachedOutput cachedOutput, DataGenerator generator) throws IOException {
        new WoodMinerProvider(generator).run(cachedOutput);
    }

    @Override
    protected void addMinerResults(ResultConsumer consumer, RegistryOps<JsonElement> ops) {
        new WoodMinerResults(generator, ops).gather(consumer);
    }

    @Override
    protected void addRecipes(Consumer<FinishedRecipe> consumer) {
        new WoodMinerRecipes(generator).buildCraftingRecipes(consumer);
    }

    @Override
    protected void addItemTags(TagProviderBuilder<Item> provider) {
        provider.tag(LEAF_CATALYSTS, it -> it.addOptional(new ResourceLocation(SimpleMiners.MOD_ID, "leaf_catalyst")));
    }

    @Override
    protected void addItemModels(ItemModelProvider provider) {
        provider.basicItem(new ResourceLocation(SimpleMiners.MOD_ID, "leaf_catalyst"));
    }
}
