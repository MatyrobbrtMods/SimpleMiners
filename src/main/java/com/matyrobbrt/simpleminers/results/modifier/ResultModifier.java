package com.matyrobbrt.simpleminers.results.modifier;

import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.miner.MinerBE;
import com.matyrobbrt.simpleminers.results.predicate.AndPredicate;
import com.matyrobbrt.simpleminers.results.predicate.ResultPredicate;
import com.matyrobbrt.simpleminers.results.predicate.ResultPredicates;
import com.matyrobbrt.simpleminers.util.Utils;
import com.mojang.serialization.Codec;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import oshi.annotation.concurrent.Immutable;

import java.util.List;
import java.util.function.Function;

public interface ResultModifier {
    ResourceKey<Registry<Codec<? extends ResultModifier>>> MODIFIER_REGISTRY = SimpleMiners.registryKey("result_modifier");
    Registry<Codec<? extends ResultModifier>> MODIFIERS = SimpleMiners.registry(MODIFIER_REGISTRY, "nop");

    Codec<ResultModifier> DIRECT_CODEC = MODIFIERS.byNameCodec().dispatch(ResultModifier::codec, Function.identity());
    Codec<ResultModifier> LIST_DIRECT_CODEC = Utils.singleOrListOf(DIRECT_CODEC)
            .xmap(AndModifier::new, mod -> mod instanceof AndModifier and ? and.modifiers() : List.of(mod));

    default int modifyWeight(MinerBE miner, int originalWeight, @Immutable ItemStack originalStack) {
        return originalWeight;
    }

    default ItemStack modifyStack(ItemStack stack) {
        return stack;
    }

    Codec<? extends ResultModifier> codec();

    default List<Component> getDescription() {
        return List.of();
    }

    static BiomeWeightBonusModifier biomeWeightBonus(BiomeWeightBonusModifier.BonusEntry... entries) {
        return new BiomeWeightBonusModifier(List.of(entries));
    }
    static BiomeWeightBonusModifier biomeWeightBonus(HolderSet<Biome> biomes, int bonus) {
        return biomeWeightBonus(new BiomeWeightBonusModifier.BonusEntry(() -> biomes, bonus));
    }

    static CatalystWeightBonusModifier catalystWeightBonus(CatalystWeightBonusModifier.Entry... entries) {
        return new CatalystWeightBonusModifier(List.of(entries));
    }

    default ResultModifier and(ResultModifier other) {
        if (other == ResultModifiers.NOP) return this;
        return new AndModifier(List.of(this, other));
    }
}
