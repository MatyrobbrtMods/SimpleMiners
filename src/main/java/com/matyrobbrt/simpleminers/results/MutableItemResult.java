package com.matyrobbrt.simpleminers.results;

import com.matyrobbrt.simpleminers.results.modifier.ResultModifier;
import com.matyrobbrt.simpleminers.results.predicate.ResultPredicate;
import com.matyrobbrt.simpleminers.util.Utils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@FieldsAreNonnullByDefault
public class MutableItemResult implements IItemResult {
    public static final Codec<MutableItemResult> CODEC = RecordCodecBuilder.create(in -> IItemResult.codecStructure(in).apply(in, MutableItemResult::new));
    public static final Codec<List<MutableItemResult>> LIST_CODEC = Utils.singleOrListOf(CODEC);

    public MutableItemResult(ItemStack item, int weight, ResultPredicate predicate, ResultModifier modifier) {
        this.item = item;
        this.weight = weight;
        this.predicate = predicate;
        this.modifier = modifier;
    }

    private ItemStack item;
    private int weight;
    private ResultPredicate predicate;
    private ResultModifier modifier;

    @Override
    public ItemStack item() {
        return item;
    }

    @Override
    public int weight() {
        return weight;
    }

    @Override
    public ResultPredicate predicate() {
        return predicate;
    }

    @Override
    public ResultModifier modifier() {
        return modifier;
    }

    public MutableItemResult with(ItemStack item) {
        this.item = item;
        return this;
    }
    public MutableItemResult with(int weight) {
        this.weight = weight;
        return this;
    }
    public MutableItemResult with(ResultPredicate predicate) {
        this.predicate = predicate;
        return this;
    }
    public MutableItemResult with(ResultModifier modifier) {
        this.modifier = modifier;
        return this;
    }

    public MutableItemResult and(ResultPredicate predicate) {
        return with(this.predicate.and(predicate));
    }
    public MutableItemResult and(ResultModifier modifier) {
        return with(this.modifier.and(modifier));
    }

    public ItemResult freeze() {
        return new ItemResult(item(), weight(), predicate(), modifier());
    }
}
