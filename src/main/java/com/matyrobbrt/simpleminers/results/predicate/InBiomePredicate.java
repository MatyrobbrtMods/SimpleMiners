package com.matyrobbrt.simpleminers.results.predicate;

import com.matyrobbrt.simpleminers.miner.MinerBE;
import com.matyrobbrt.simpleminers.util.Translations;
import com.matyrobbrt.simpleminers.util.Utils;
import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderSet;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.biome.Biome;

import java.util.List;
import java.util.function.Supplier;

public record InBiomePredicate(Supplier<HolderSet<Biome>> biomes) implements ResultPredicate {
    public InBiomePredicate(HolderSet<Biome> biomes) {
        this(() -> biomes);
    }
    @Override
    public boolean canProduce(MinerBE miner) {
        return biomes.get().contains(miner.getLevel().getBiome(miner.getBlockPos()));
    }

    @Override
    public Codec<? extends ResultPredicate> codec() {
        return ResultPredicates.IN_BIOME_CODEC;
    }

    @Override
    public List<Component> getDescription() {
        return List.of(Translations.IN_BIOME_PREDICATE.get(Utils.humanReadableHolderSet(biomes.get()).copy().withStyle(ChatFormatting.GOLD)));
    }
}
