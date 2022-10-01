package com.matyrobbrt.simpleminers.jei;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.matyrobbrt.simpleminers.Registration;
import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.results.ItemResult;
import com.matyrobbrt.simpleminers.results.ResultSet;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.Tags;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

@JeiPlugin
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SimpleMinersJEI implements IModPlugin {
    public static final ResourceLocation ID = new ResourceLocation(SimpleMiners.MOD_ID, SimpleMiners.MOD_ID);

    public static final RecipeType<JeiResultWrapper> RESULT_TYPE = RecipeType.create(SimpleMiners.MOD_ID, "result", JeiResultWrapper.class);

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        final var help = registration.getJeiHelpers();
        registration.addRecipeCategories(new MiningCategory(help));
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        final ListMultimap<String, ItemResult> results = Multimaps.newListMultimap(new HashMap<>(), ArrayList::new);
        Minecraft.getInstance().level.registryAccess().registryOrThrow(ResultSet.RESULTS_REGISTRY)
            .forEach(it -> results.putAll(it.minerType(), it.get()));
        final List<JeiResultWrapper> resultWrappers = results.asMap().entrySet().stream()
                .map(entry -> new JeiResultWrapper(entry.getKey(), List.copyOf(entry.getValue())))
                .toList();
        registration.addRecipes(RESULT_TYPE, resultWrappers);

        registration.addRecipes(RecipeTypes.CRAFTING, List.of(getFortuneUpgradeRecipe(new ResourceLocation(SimpleMiners.MOD_ID, "fortune_upgrade"))));
    }

    private CraftingRecipe getFortuneUpgradeRecipe(ResourceLocation id) {
        final NonNullList<Ingredient> ingredients = NonNullList.withSize(3 * 3, Ingredient.EMPTY);

        final Ingredient redstone = Ingredient.of(Tags.Items.DUSTS_REDSTONE);
        ingredients.set(1, redstone);
        ingredients.set(7, redstone);

        ingredients.set(3, Ingredient.of(Items.DIAMOND_PICKAXE));
        ingredients.set(4, Ingredient.of(Registration.UPGRADE_BASE.get()));

        final Stream.Builder<ItemStack> books = Stream.builder();
        for (int i = 1; i <= 3; i++) {
            books.add(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(Enchantments.BLOCK_FORTUNE, i)));
        }
        ingredients.set(5, Ingredient.of(books.build()));

        return new ShapedRecipe(id, "", 3, 3, ingredients, Registration.FORTUNE_UPGRADE.get().getDefaultInstance());
    }
}
