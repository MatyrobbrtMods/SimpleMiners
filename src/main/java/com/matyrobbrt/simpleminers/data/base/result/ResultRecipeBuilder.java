package com.matyrobbrt.simpleminers.data.base.result;

import com.matyrobbrt.simpleminers.results.ItemResult;
import com.matyrobbrt.simpleminers.results.modifier.ResultModifier;
import com.matyrobbrt.simpleminers.results.modifier.ResultModifiers;
import com.matyrobbrt.simpleminers.results.predicate.ResultPredicate;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unchecked")
public interface ResultRecipeBuilder<T extends ResultRecipeBuilder<?>> {
    static ResultRecipeBuilderImpl builder(String minerType) {
        return ResultRecipeBuilderImpl.builder(minerType);
    }
    static ResultRecipeBuilderImpl builder(String minerType, String requiredMod) {
        return ResultRecipeBuilderImpl.builder(minerType, requiredMod);
    }
    @SuppressWarnings("rawtypes")
    static ResultRecipeBuilder accumulating(List<ItemResult> results) {
        return new ResultRecipeBuilder<>() {
            @Override
            public ResultRecipeBuilder<?> add(List<ItemResult> list) {
                results.addAll(list);
                return this;
            }
        };
    }

    default T add(ItemResult... results) {
        return add(Arrays.asList(results));
    }

    T add(List<ItemResult> results);

    default T add(int weight, ItemStack stack, ResultModifier modifier) {
        return add(new ItemResult(stack, weight, modifier));
    }
    default T add(int weight, ItemStack stack, ResultPredicate predicate) {
        return add(new ItemResult(stack, weight, predicate));
    }

    default T add(int weight, Item item) {
        return add(weight, item.getDefaultInstance(), ResultModifiers.NOP);
    }

    default T add(int weight, Item... items) {
        for (Item item : items) {
            add(weight, item);
        }
        return (T) this;
    }

    default T add(int weight, ResultModifier modifier, Item... items) {
        for (Item item : items) {
            add(weight, item.getDefaultInstance(), modifier);
        }
        return (T) this;
    }
    default T add(int weight, ResultPredicate predicate, Item... items) {
        for (Item item : items) {
            add(weight, item.getDefaultInstance(), predicate);
        }
        return (T) this;
    }
}
