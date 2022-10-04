package com.matyrobbrt.simpleminers.results.predicate;

import com.matyrobbrt.simpleminers.miner.MinerBE;
import com.matyrobbrt.simpleminers.util.Translations;
import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

import java.util.List;

@MethodsReturnNonnullByDefault
public record PositionPredicate(Comparison comparison, Axis axis, int position) implements ResultPredicate {
    @Override
    public boolean canProduce(MinerBE miner) {
        return comparison.check(axis.get(miner.getBlockPos()), position);
    }

    @Override
    public Codec<? extends ResultPredicate> codec() {
        return ResultPredicates.POSITION_CODEC;
    }

    @Override
    public List<Component> getDescription() {
        return List.of(Translations.POSITION_PREDICATE.get(
            Component.literal(axis.getSerializedName()).withStyle(ChatFormatting.AQUA),
            comparison.getTranslation(),
            Component.literal(String.valueOf(position)).withStyle(ChatFormatting.GOLD)
        ));
    }

    public enum Comparison implements StringRepresentable {
        GREATER {
            @Override
            public String getSerializedName() {
                return "greater";
            }

            @Override
            public boolean check(int minerPos, int targetPos) {
                return minerPos > targetPos;
            }
        }, GREATER_OR_EQUAL {
            @Override
            public String getSerializedName() {
                return "greater_or_equal";
            }

            @Override
            public boolean check(int minerPos, int targetPos) {
                return minerPos >= targetPos;
            }

            @Override
            public String getTranslation() {
                return "greater or equal to";
            }
        },

        SMALLER {
            @Override
            public String getSerializedName() {
                return "smaller";
            }

            @Override
            public boolean check(int minerPos, int targetPos) {
                return minerPos < targetPos;
            }
        }, SMALLER_OR_EQUAL {
            @Override
            public String getSerializedName() {
                return "smaller_or_equal";
            }

            @Override
            public boolean check(int minerPos, int targetPos) {
                return minerPos <= targetPos;
            }

            @Override
            public String getTranslation() {
                return "smaller or equal to";
            }
        },

        EQUAL {
            @Override
            public String getSerializedName() {
                return "equal";
            }

            @Override
            public boolean check(int minerPos, int targetPos) {
                return minerPos == targetPos;
            }

            @Override
            public String getTranslation() {
                return "equal to";
            }
        };

        public abstract boolean check(int minerPos, int targetPos);
        public String getTranslation() {
            return getSerializedName() + " than";
        }
    }
    public enum Axis implements StringRepresentable {
        X {
            @Override
            public String getSerializedName() {
                return "x";
            }

            @Override
            public int get(BlockPos pos) {
                return pos.getX();
            }
        }, Y {
            @Override
            public String getSerializedName() {
                return "y";
            }

            @Override
            public int get(BlockPos pos) {
                return pos.getY();
            }
        }, Z {
            @Override
            public String getSerializedName() {
                return "z";
            }

            @Override
            public int get(BlockPos pos) {
                return pos.getZ();
            }
        };

        public abstract int get(BlockPos pos);
    }
}
