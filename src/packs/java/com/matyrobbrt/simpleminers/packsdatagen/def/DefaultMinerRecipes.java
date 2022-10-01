package com.matyrobbrt.simpleminers.packsdatagen.def;

import com.matyrobbrt.simpleminers.Registration;
import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.data.base.SimpleShapedRecipeBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.ItemExistsCondition;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

import static com.matyrobbrt.simpleminers.packsdatagen.def.DefaultMinerResults.mod;

@ParametersAreNonnullByDefault
public class DefaultMinerRecipes extends RecipeProvider {
    public DefaultMinerRecipes(DataGenerator pGenerator) {
        super(pGenerator);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> pFinishedRecipeConsumer) {
        final ResourceLocation minerId = new ResourceLocation(SimpleMiners.MOD_ID, "ore_miner");

        ConditionalRecipe.builder()
                .addCondition(new ItemExistsCondition(minerId))
                .addRecipe(new SimpleShapedRecipeBuilder(minerId, 1)
                        .pattern("IPI")
                        .pattern("RCR")
                        .pattern("IRI")
                        .define('I', Tags.Items.INGOTS_IRON)
                        .define('P', Items.DIAMOND_PICKAXE)
                        .define('R', Tags.Items.DUSTS_REDSTONE)
                        .define('C', Tags.Items.CHESTS)
                        .finish(new ResourceLocation("hi")))
                .build(pFinishedRecipeConsumer, mod("ore_miner"));

        final ResourceLocation gemCatalyst = new ResourceLocation(SimpleMiners.MOD_ID, "gem_catalyst");
        ConditionalRecipe.builder()
                .addCondition(new ItemExistsCondition(gemCatalyst))
                .addRecipe(new SimpleShapedRecipeBuilder(gemCatalyst, 1)
                        .pattern("D")
                        .pattern("B")
                        .pattern("E")
                        .define('D', Tags.Items.GEMS_DIAMOND)
                        .define('E', Tags.Items.GEMS_EMERALD)
                        .define('B', Registration.CATALYST_BASE.get())
                        .finish(new ResourceLocation("hi")))
                .build(pFinishedRecipeConsumer, mod("gem_catalyst"));
    }
}
