package com.matyrobbrt.simpleminers.packsdatagen.wood;

import com.google.gson.JsonObject;
import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.data.base.SimpleShapedRecipeBuilder;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.ItemExistsCondition;

import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.file.Path;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class WoodMinerRecipes extends RecipeProvider {
    public WoodMinerRecipes(DataGenerator pGenerator) {
        super(pGenerator);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> pFinishedRecipeConsumer) {
        final ResourceLocation minerId = new ResourceLocation(SimpleMiners.MOD_ID, "wood_miner");

        ConditionalRecipe.builder()
                .addCondition(new ItemExistsCondition(minerId))
                .addRecipe(new SimpleShapedRecipeBuilder(minerId, 1)
                        .pattern("IAI")
                        .pattern("RCR")
                        .pattern("IAI")
                        .define('I', Tags.Items.INGOTS_IRON)
                        .define('A', Items.DIAMOND_AXE)
                        .define('R', Tags.Items.DUSTS_REDSTONE)
                        .define('C', Tags.Items.CHESTS)
                        .finish(new ResourceLocation("hi")))
                .build(pFinishedRecipeConsumer, new ResourceLocation(SimpleMiners.MOD_ID, "wood_miner"));
    }

    @Override
    protected void saveAdvancement(CachedOutput pOutput, JsonObject pAdvancementJson, Path pPath) {
    }
}
