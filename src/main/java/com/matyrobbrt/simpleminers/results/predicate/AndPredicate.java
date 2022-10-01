package com.matyrobbrt.simpleminers.results.predicate;

import com.matyrobbrt.simpleminers.miner.MinerBE;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;

import java.util.List;

public record AndPredicate(List<ResultPredicate> predicates) implements ResultPredicate {
    @Override
    public boolean canProduce(MinerBE miner) {
        return predicates.stream().allMatch(it -> it.canProduce(miner));
    }

    @Override
    public Codec<? extends ResultPredicate> codec() {
        return ResultPredicates.AND_CODEC;
    }

    @Override
    public List<Component> getDescription() {
        return predicates.stream().flatMap(it -> it.getDescription().stream()).toList();
    }
}
