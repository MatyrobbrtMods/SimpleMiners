package com.matyrobbrt.simpleminers.results.predicate;

import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.miner.MinerBE;
import com.matyrobbrt.simpleminers.util.Utils;
import com.mojang.serialization.Codec;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public interface ResultPredicate {
    ResourceKey<Registry<Codec<? extends ResultPredicate>>> PREDICATE_REGISTRY = SimpleMiners.registryKey("result_predicate");
    Registry<Codec<? extends ResultPredicate>> PREDICATES = SimpleMiners.registry(PREDICATE_REGISTRY, "true");

    Codec<ResultPredicate> DIRECT_CODEC = PREDICATES.byNameCodec()
            .dispatch(ResultPredicate::codec, Function.identity());
    Codec<ResultPredicate> LIST_DIRECT_CODEC = Utils.singleOrListOf(DIRECT_CODEC)
            .xmap(AndPredicate::new, predicate -> predicate instanceof AndPredicate and ? and.predicates() : List.of(predicate));

    default boolean canProduce(MinerBE miner) {
        return true;
    }

    Codec<? extends ResultPredicate> codec();

    default List<Component> getDescription() {
        return List.of();
    }

    static ResultPredicate inBiome(HolderSet<Biome> biomes) {
        return new InBiomePredicate(biomes);
    }

    default ResultPredicate and(ResultPredicate other) {
        if (other == ResultPredicates.TRUE) return this;
        return new AndPredicate(List.of(this, other));
    }
}