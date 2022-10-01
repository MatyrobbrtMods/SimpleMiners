package com.matyrobbrt.simpleminers.results;

import com.matyrobbrt.simpleminers.results.modifier.ResultModifier;
import com.matyrobbrt.simpleminers.results.modifier.ResultModifiers;
import com.matyrobbrt.simpleminers.results.predicate.ResultPredicate;
import com.matyrobbrt.simpleminers.results.predicate.ResultPredicates;
import com.matyrobbrt.simpleminers.util.Codecs;
import com.matyrobbrt.simpleminers.util.Utils;
import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IItemResult {
    Codec<IItemResult> CODEC = RecordCodecBuilder.create(in -> codecStructure(in).apply(in, ItemResult::new));
    Codec<List<IItemResult>> CODEC_LIST = Utils.singleOrListOf(CODEC);

    static <T extends IItemResult> Products.P4<RecordCodecBuilder.Mu<T>, ItemStack, Integer, ResultPredicate, ResultModifier> codecStructure(RecordCodecBuilder.Instance<T> builder) {
        return builder.group(
                Codecs.STACK_CODEC.fieldOf("item").forGetter(IItemResult::item),
                Codec.INT.optionalFieldOf("weight", ItemResult.DEFAULT_WEIGHT).forGetter(IItemResult::weight),
                ResultPredicate.LIST_DIRECT_CODEC.optionalFieldOf("predicate", ResultPredicates.TRUE).forGetter(IItemResult::predicate),
                ResultModifier.LIST_DIRECT_CODEC.optionalFieldOf("modifier", ResultModifiers.NOP).forGetter(IItemResult::modifier)
        );
    }

    ItemStack item();
    int weight();
    ResultPredicate predicate();
    ResultModifier modifier();

    default MutableItemResult mutableCopy() {
        return new MutableItemResult(item(), weight(), predicate(), modifier());
    }
}
