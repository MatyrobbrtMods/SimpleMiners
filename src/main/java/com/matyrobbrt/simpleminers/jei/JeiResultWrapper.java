package com.matyrobbrt.simpleminers.jei;

import com.matyrobbrt.simpleminers.results.ItemResult;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryExtension;

import java.util.List;

public record JeiResultWrapper(String minerType, List<ItemResult> results) implements IRecipeCategoryExtension {
}
