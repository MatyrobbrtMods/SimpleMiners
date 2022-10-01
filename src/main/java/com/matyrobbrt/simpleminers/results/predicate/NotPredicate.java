package com.matyrobbrt.simpleminers.results.predicate;

import com.google.common.collect.Lists;
import com.matyrobbrt.simpleminers.miner.MinerBE;
import com.matyrobbrt.simpleminers.util.Translations;
import com.matyrobbrt.simpleminers.util.Utils;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Function;

public record NotPredicate(ResultPredicate predicate) implements ResultPredicate {
    @Override
    public boolean canProduce(MinerBE miner) {
        return !predicate.canProduce(miner);
    }

    @Override
    public Codec<? extends ResultPredicate> codec() {
        return ResultPredicates.NOT_CODEC;
    }

    @Override
    public List<Component> getDescription() {
        final var desc = predicate.getDescription();
        if (desc.isEmpty()) return desc;
        return Lists.newArrayList(Translations.NOT_CAP.get(
                Utils.join(Component.literal(System.lineSeparator()), predicate.getDescription(), Function.identity())
        ));
    }
}
