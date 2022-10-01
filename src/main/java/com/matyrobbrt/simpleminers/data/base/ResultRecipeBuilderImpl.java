package com.matyrobbrt.simpleminers.data.base;

import com.google.gson.JsonElement;
import com.matyrobbrt.simpleminers.results.IItemResult;
import com.matyrobbrt.simpleminers.results.ItemResult;
import com.matyrobbrt.simpleminers.results.ResultSet;
import com.matyrobbrt.simpleminers.results.modifier.ResultModifier;
import com.matyrobbrt.simpleminers.results.predicate.ResultPredicate;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record ResultRecipeBuilderImpl(String minerType, @Nullable String requiredMod, List<ItemResult> results, List<ResultSet.Copying> copying) implements ResultRecipeBuilder<ResultRecipeBuilderImpl> {
    static ResultRecipeBuilderImpl builder(String minerType) {
        return new ResultRecipeBuilderImpl(minerType, null, new ArrayList<>(), new ArrayList<>());
    }
    static ResultRecipeBuilderImpl builder(String minerType, String requiredMod) {
        return new ResultRecipeBuilderImpl(minerType, requiredMod, new ArrayList<>(), new ArrayList<>());
    }

    @Override
    public ResultRecipeBuilderImpl add(List<ItemResult> results) {
        this.results.addAll(results);
        return this;
    }

    @SuppressWarnings("unchecked")
    public ResultRecipeBuilderImpl addCopying(@Nullable ResultModifier modifier, @Nullable ResultPredicate predicate, Consumer<ResultRecipeBuilder<?>> builderConsumer) {
        final List<ItemResult> results = new ArrayList<>();
        builderConsumer.accept(ResultRecipeBuilder.accumulating(results));
        this.copying.add(new ResultSet.Copying(Optional.ofNullable(modifier), Optional.ofNullable(predicate), (List<IItemResult>) (Object) results));
        return this;
    }

    public void save(ResultConsumer consumer, ResourceLocation id) {
        consumer.accept(id, new ResultSet(minerType, results, copying, requiredMod));
    }
}
