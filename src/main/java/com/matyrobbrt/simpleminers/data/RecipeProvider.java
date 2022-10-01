package com.matyrobbrt.simpleminers.data;

import com.matyrobbrt.simpleminers.Registration;
import com.matyrobbrt.simpleminers.SimpleMiners;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.Tags;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

import static net.minecraft.data.recipes.ShapedRecipeBuilder.shaped;

@ParametersAreNonnullByDefault
public class RecipeProvider extends net.minecraft.data.recipes.RecipeProvider {
    public RecipeProvider(DataGenerator pGenerator) {
        super(pGenerator);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> pFinishedRecipeConsumer) {
        final Item upgradeBase = Registration.UPGRADE_BASE.get();
        final CriterionTriggerInstance hasBase = has(upgradeBase);

        shaped(upgradeBase, 4)
                .pattern(" I ")
                .pattern("gDg")
                .pattern(" G ")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('D', Tags.Items.DYES_BLACK)
                .define('G', Tags.Items.NUGGETS_GOLD)
                .define('g', Tags.Items.GLASS)
                .unlockedBy("has_iron", has(Tags.Items.INGOTS_IRON))
                .save(pFinishedRecipeConsumer, mod("upgrade_base"));

        shaped(Registration.SPEED_UPGRADE.get(), 1)
                .pattern(" R ")
                .pattern("IBI")
                .pattern(" R ")
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('I', Tags.Items.INGOTS_IRON)
                .define('B', upgradeBase)
                .unlockedBy("has_base", hasBase)
                .save(pFinishedRecipeConsumer, mod("speed_upgrade"));

        shaped(Registration.ENERGY_UPGRADE.get(), 1)
                .pattern(" R ")
                .pattern("GBG")
                .pattern(" R ")
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('G', Tags.Items.INGOTS_GOLD)
                .define('B', upgradeBase)
                .unlockedBy("has_base", hasBase)
                .save(pFinishedRecipeConsumer, mod("energy_upgrade"));

        shaped(Registration.PRODUCTION_UPGRADE.get(), 1)
                .pattern(" R ")
                .pattern("DBD")
                .pattern(" R ")
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('D', Tags.Items.GEMS_DIAMOND)
                .define('B', upgradeBase)
                .unlockedBy("has_base", hasBase)
                .save(pFinishedRecipeConsumer, mod("production_upgrade"));

        shaped(Registration.CATALYST_BASE.get(), 4)
                .pattern(" G ")
                .pattern("RIR")
                .pattern(" G ")
                .define('G', Tags.Items.INGOTS_GOLD)
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('I', Tags.Items.INGOTS_IRON)
                .unlockedBy("has_gold", has(Tags.Items.INGOTS_GOLD))
                .save(pFinishedRecipeConsumer, mod("catalyst_base"));
    }

    private static ResourceLocation mod(String path) {
        return new ResourceLocation(SimpleMiners.MOD_ID, path);
    }

}
