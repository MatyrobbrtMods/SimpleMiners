package com.matyrobbrt.simpleminers.results.predicate;

import com.matyrobbrt.simpleminers.miner.MinerBE;
import com.matyrobbrt.simpleminers.util.Translations;
import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.List;

public record InDimensionPredicate(ResourceKey<Level> dimension) implements ResultPredicate {
    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean canProduce(MinerBE miner) {
        return miner.getLevel().dimension() == dimension;
    }

    @Override
    public Codec<? extends ResultPredicate> codec() {
        return ResultPredicates.IN_DIMENSION_CODEC;
    }

    @Override
    public List<Component> getDescription() {
        return List.of(Translations.IN_DIMENSION_PREDICATE.get(
                Component.literal(dimension.location().toString()).withStyle(ChatFormatting.GOLD)
        ));
    }
}
