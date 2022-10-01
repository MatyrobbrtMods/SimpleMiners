package com.matyrobbrt.simpleminers.jei;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.miner.MinerType;
import com.matyrobbrt.simpleminers.results.ItemResult;
import com.matyrobbrt.simpleminers.util.Translations;
import com.mojang.datafixers.util.Pair;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MiningCategory extends BlankJEIRecipeCategory<JeiResultWrapper> {
    static final int FIRST_Y = 47;
    static final int FIRST_X = 2;
    static final int WIDTH = 164;
    static final int HEIGHT = 127;
    static final int ITEMS_PER_ROW = 10;
    static final int ITEMS_PER_COL = 5;
    static final int SPACING_X = 16;
    static final int SPACING_Y = 16;

    static final int ITEMS_PER_PAGE = ITEMS_PER_ROW * ITEMS_PER_COL;

    private final IDrawable background;

    MiningCategory(IJeiHelpers helpers) {
        super(helpers.getGuiHelper().createDrawableItemStack(SimpleMiners.getTabIcon().getDefaultInstance()));
        this.background = helpers.getGuiHelper().createDrawable(new ResourceLocation(SimpleMiners.MOD_ID, "gui/jei/mining.png"), 0, 0, WIDTH, HEIGHT);
    }

    @Override
    public RecipeType<JeiResultWrapper> getRecipeType() {
        return SimpleMinersJEI.RESULT_TYPE;
    }

    @Override
    public Component getTitle() {
        return Translations.JEI_MINING.get();
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, JeiResultWrapper recipe, IFocusGroup focuses) {
        int x = FIRST_X;
        int y = FIRST_Y;

        builder.addSlot(RecipeIngredientRole.CATALYST, 75, 3)
                .addIngredients(Ingredient.of(MinerType.BLOCKS.get(recipe.minerType())));

        final IFocus<ItemStack> firstFocus = focuses.getFocuses(VanillaTypes.ITEM_STACK, RecipeIngredientRole.OUTPUT).findFirst().orElse(null);

        if (firstFocus == null) {
            final ListMultimap<Integer, ItemResult> byWeight = Multimaps.newListMultimap(new HashMap<>(), ArrayList::new);
            recipe.results().forEach(result -> byWeight.put(result.weight(), result));
            @SuppressWarnings("UnstableApiUsage") final var asList = Multimaps.asMap(byWeight)
                    .entrySet().stream()
                    .sorted(Map.Entry.<Integer, List<ItemResult>>comparingByKey().reversed())
                    .map(entry -> Pair.of(entry.getKey(), entry.getValue()))
                    .toList();
            final List<List<Pair<Integer, List<ItemResult>>>> partitioned = splitIntoMultiple(asList, ITEMS_PER_PAGE);
            for (final var part : partitioned) {
                final var byRow = splitIntoMultiple(part, ITEMS_PER_ROW);
                for (final var row : byRow) {
                    final var values = row.stream().flatMap(it -> it.getSecond().stream()).toList();
                    builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                            .addTooltipCallback((recipeSlotView, tooltip) -> {
                                recipeSlotView.getDisplayedItemStack()
                                        .flatMap(stack -> values.stream()
                                                .filter(it -> ItemStack.isSame(it.item(), stack))
                                                .findFirst())
                                        .ifPresent(result -> tooltip.addAll(result.tooltip()));
                            })
                            .addItemStacks(values.stream().map(ItemResult::item).toList());
                    x += SPACING_X;

                    if (x >= FIRST_X + SPACING_X * ITEMS_PER_ROW ) {
                        x = FIRST_X;
                        y += SPACING_Y;
                    }
                }
            }
        } else {
            final var result = recipe.results().stream().filter(it -> ItemStack.isSame(it.item(), firstFocus.getTypedValue().getIngredient())).toList();
            final var rows = splitIntoMultiple(result, ITEMS_PER_ROW);
            for (final var row : rows) {
                builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                        .addTooltipCallback((recipeSlotView, tooltip) -> {
                            recipeSlotView.getDisplayedItemStack()
                                    .flatMap(stack -> row.stream()
                                            .filter(it -> ItemStack.isSame(it.item(), stack))
                                            .findFirst())
                                    .ifPresent(r -> tooltip.addAll(r.tooltip()));
                        })
                        .addItemStacks(row.stream().map(ItemResult::item).toList());
                x += SPACING_X;

                if (x >= FIRST_X + SPACING_X * ITEMS_PER_ROW ) {
                    x = FIRST_X;
                    y += SPACING_Y;
                }
            }
        }
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    private static <T> List<List<T>> splitIntoMultiple(List<T> in, int maxPer) {
        final int listCount = in.size() / maxPer + in.size() % maxPer;
        final List<List<T>> lists = new ArrayList<>();
        int current = 0;
        for (int i = 0; i < in.size(); i++) {
            getList(lists, current).add(in.get(i));
            current++;
            if (current >= listCount) {
                current = 0;
            }
        }
        return lists;
    }

    private static <T> List<T> getList(List<List<T>> lists, int index) {
        if (index >= lists.size()) {
            final var list = new ArrayList<T>();
            lists.add(list);
            return list;
        }
        return lists.get(index);
    }
}
