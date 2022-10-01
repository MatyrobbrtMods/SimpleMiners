package com.matyrobbrt.simpleminers.results.predicate;

import com.matyrobbrt.simpleminers.miner.MinerBE;
import com.matyrobbrt.simpleminers.util.Utils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public final class ResultPredicates {
    public static final ResultPredicate TRUE = new ResultPredicate() {
        @Override
        public Codec<? extends ResultPredicate> codec() {
            return TRUE_CODEC;
        }

        @Override
        public ResultPredicate and(ResultPredicate other) {
            return other;
        }
    };
    public static final Codec<ResultPredicate> TRUE_CODEC = register("true", Codec.unit(TRUE));

    public static final ResultPredicate FALSE = new ResultPredicate() {
        @Override
        public boolean canProduce(MinerBE miner) {
            return false;
        }

        @Override
        public Codec<? extends ResultPredicate> codec() {
            return FALSE_CODEC;
        }
    };
    public static final Codec<ResultPredicate> FALSE_CODEC = register("false", Codec.unit(FALSE));

    public static final Codec<InBiomePredicate> IN_BIOME_CODEC = register("in_biome", RecordCodecBuilder.create(in -> in.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(InBiomePredicate::biomes)
    ).apply(in, InBiomePredicate::new)));

    public static final Codec<AndPredicate> AND_CODEC = register("and", RecordCodecBuilder.create(in -> in.group(
            Utils.singleOrListOf(ResultPredicate.LIST_DIRECT_CODEC).fieldOf("values").forGetter(AndPredicate::predicates)
    ).apply(in, AndPredicate::new)));

    public static final Codec<NotPredicate> NOT_CODEC = register("not", RecordCodecBuilder.create(in -> in.group(
            ResultPredicate.DIRECT_CODEC.fieldOf("value").forGetter(NotPredicate::predicate)
    ).apply(in, NotPredicate::new)));

    public static final Codec<WeatherPredicate> WEATHER_CODEC = register("weather", RecordCodecBuilder.create(in -> in.group(
            StringRepresentable.fromEnum(WeatherPredicate.WeatherType::values).fieldOf("weather").forGetter(WeatherPredicate::weather),
            Codec.BOOL.optionalFieldOf("require", true).forGetter(WeatherPredicate::require)
    ).apply(in, WeatherPredicate::new)));

    public static final Codec<InDimensionPredicate> IN_DIMENSION_CODEC = register("in_dimension", RecordCodecBuilder.create(in -> in.group(
            Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(InDimensionPredicate::dimension)
    ).apply(in, InDimensionPredicate::new)));

    private static <T extends Codec<? extends ResultPredicate>> T register(String name, T value) {
        return Registry.register(ResultPredicate.PREDICATES, name, value);
    }

    public static void clinit() {}
}
