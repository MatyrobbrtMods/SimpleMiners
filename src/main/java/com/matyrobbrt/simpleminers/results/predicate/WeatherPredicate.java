package com.matyrobbrt.simpleminers.results.predicate;

import com.matyrobbrt.simpleminers.miner.MinerBE;
import com.matyrobbrt.simpleminers.util.Translations;
import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.storage.LevelData;

import java.util.List;

public record WeatherPredicate(WeatherType weather, boolean require) implements ResultPredicate {

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean canProduce(MinerBE miner) {
        return weather.check(miner.getLevel().getLevelData()) == require;
    }

    @Override
    public List<Component> getDescription() {
        return List.of(Translations.WEATHER_PREDICATE.get(
                require ? Component.empty() : Component.literal("not"),
                Component.literal(weather.getSerializedName()).withStyle(ChatFormatting.AQUA)
        ));
    }

    @Override
    public Codec<? extends ResultPredicate> codec() {
        return ResultPredicates.WEATHER_CODEC;
    }

    @MethodsReturnNonnullByDefault
    public enum WeatherType implements StringRepresentable {
        RAINING {
            @Override
            public String getSerializedName() {
                return "raining";
            }

            @Override
            boolean check(LevelData data) {
                return data.isRaining();
            }
        },
        THUNDERING {
            @Override
            public String getSerializedName() {
                return "thundering";
            }

            @Override
            boolean check(LevelData data) {
                return data.isThundering();
            }
        };
        abstract boolean check(LevelData data);
    }
}
