package com.matyrobbrt.simpleminers.packsdatagen.ore;

import com.google.gson.JsonElement;
import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.data.base.TagProviderBuilder;
import com.matyrobbrt.simpleminers.data.base.result.ResultConsumer;
import com.matyrobbrt.simpleminers.packsdatagen.RegisterPack;
import com.matyrobbrt.simpleminers.packsdatagen.simple.SimplePackGenerator;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
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

import static com.matyrobbrt.simpleminers.packsdatagen.ore.OreMinerResults.mod;

@RegisterPack("ore")
@ParametersAreNonnullByDefault
public class OrePackGenerator extends SimplePackGenerator {
    public static final TagKey<Item> GEM_CATALYSTS = TagKey.create(Registry.ITEM_REGISTRY, mod("catalysts/gem"));

    @Override
    protected void addMiners(CachedOutput cachedOutput, DataGenerator generator) throws IOException {
        new OreMinerProvider(generator).run(cachedOutput);
    }

    @Override
    protected void addMinerResults(ResultConsumer consumer, RegistryOps<JsonElement> ops) {
        new OreMinerResults(generator, ops).gather(consumer);
    }

    @Override
    protected void addRecipes(Consumer<FinishedRecipe> consumer) {
        new OreMinerRecipes(generator).buildCraftingRecipes(consumer);
    }

    @Override
    protected void addItemTags(TagProviderBuilder<Item> provider) {
        provider.tag(OrePackGenerator.GEM_CATALYSTS, it -> it.addOptional(mod("gem_catalyst")));
    }

    @Override
    protected void addItemModels(ItemModelProvider provider) {
        provider.basicItem(new ResourceLocation(SimpleMiners.MOD_ID, "gem_catalyst"));
    }
}
