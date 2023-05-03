package com.matyrobbrt.simpleminers.results.modifier;

import com.matyrobbrt.simpleminers.miner.MinerBE;
import com.matyrobbrt.simpleminers.util.Translations;
import com.matyrobbrt.simpleminers.util.Utils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;

import java.util.List;
import java.util.function.Supplier;

public record BiomeWeightBonusModifier(List<BonusEntry> bonuses) implements ResultModifier {
    @Override
    public int modifyWeight(MinerBE miner, int originalWeight, ItemStack originalStack) {
        @SuppressWarnings("ConstantConditions") final Holder<Biome> biome = miner.getLevel().getBiome(miner.getBlockPos());

        int weight = originalWeight;

        for (final BonusEntry bonus : bonuses) {
            if (bonus.biomes().get().contains(biome)) {
                weight += bonus.bonus();
            }
        }

        return weight;
    }

    @Override
    public Codec<? extends ResultModifier> codec() {
        return ResultModifiers.BIOME_WEIGHT_BONUS;
    }

    @Override
    public List<Component> getDescription() {
        return bonuses.stream()
                .<Component>map(it -> Translations.BIOME_WEIGHT_BONUS.get(
                        Component.literal(Utils.intWithSign(it.bonus)).withStyle(ChatFormatting.AQUA),
                        Utils.humanReadableHolderSet(it.biomes().get()).copy().withStyle(ChatFormatting.GOLD)
                )).toList();
    }

    public record BonusEntry(Supplier<HolderSet<Biome>> biomes, int bonus) {
        public BonusEntry(HolderSet<Biome> biomes, int bonus) {
            this(() -> biomes, bonus);
        }
        public static final Codec<BonusEntry> CODEC = RecordCodecBuilder.create(in -> in.group(
                Utils.lazyWithRegistryAccess(Biome.LIST_CODEC).fieldOf("biomes").forGetter(BonusEntry::biomes),
                Codec.INT.fieldOf("bonus").forGetter(BonusEntry::bonus)
        ).apply(in, BonusEntry::new));
    }
}
