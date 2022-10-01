package com.matyrobbrt.simpleminers.results.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;

import static com.matyrobbrt.simpleminers.util.Utils.singleOrListOf;

public final class ResultModifiers {
    public static final ResultModifier NOP = new ResultModifier() {
        @Override
        public Codec<? extends ResultModifier> codec() {
            return NOP_CODEC;
        }

        @Override
        public ResultModifier and(ResultModifier other) {
            return other;
        }
    };
    public static final Codec<ResultModifier> NOP_CODEC = register("nop", Codec.unit(NOP));

    public static final Codec<BiomeWeightBonusModifier> BIOME_WEIGHT_BONUS = register("biome_weight_bonus", RecordCodecBuilder.create(in -> in.group(
            singleOrListOf(BiomeWeightBonusModifier.BonusEntry.CODEC).fieldOf("bonuses").forGetter(BiomeWeightBonusModifier::bonuses)
    ).apply(in, BiomeWeightBonusModifier::new)));

    public static final Codec<AndModifier> AND_CODEC = register("and", RecordCodecBuilder.create(in -> in.group(
            singleOrListOf(ResultModifier.LIST_DIRECT_CODEC).fieldOf("values").forGetter(AndModifier::modifiers)
    ).apply(in, AndModifier::new)));

    public static final Codec<CatalystWeightBonusModifier> CATALYST_WEIGHT_BONUS_CODEC = register("catalyst_weight_bonus", RecordCodecBuilder.create(in -> in.group(
            singleOrListOf(CatalystWeightBonusModifier.Entry.CODEC).fieldOf("bonuses").forGetter(CatalystWeightBonusModifier::bonuses)
    ).apply(in, CatalystWeightBonusModifier::new)));

    private static <T extends Codec<? extends ResultModifier>> T register(String name, T value) {
        return Registry.register(ResultModifier.MODIFIERS, name, value);
    }

    public static void clinit() {
    }
}
