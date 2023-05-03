package com.matyrobbrt.simpleminers.results.modifier;

import com.matyrobbrt.simpleminers.miner.MinerBE;
import com.matyrobbrt.simpleminers.util.Translations;
import com.matyrobbrt.simpleminers.util.Utils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderSet;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Supplier;

public record CatalystWeightBonusModifier(List<Entry> bonuses) implements ResultModifier {

    @Override
    public int modifyWeight(MinerBE miner, int originalWeight, ItemStack originalStack) {
        int weight = originalWeight;

        for (final Entry bonus : bonuses) {
            @SuppressWarnings("deprecation")
            final List<ItemStack> catalysts = miner.findCatalysts(item -> bonus.catalyst().get().contains(item.builtInRegistryHolder()));
            if (!catalysts.isEmpty()) {
                weight += bonus.additive() ? catalysts.size() * bonus.bonus() : bonus.bonus();
            }
        }

        return weight;
    }

    @Override
    public List<Component> getDescription() {
        return bonuses.stream()
                .map(Entry::desc)
                .toList();
    }

    @Override
    public Codec<? extends ResultModifier> codec() {
        return ResultModifiers.CATALYST_WEIGHT_BONUS_CODEC;
    }

    public record Entry(Supplier<HolderSet<Item>> catalyst, int bonus, boolean additive) {
        public Entry(HolderSet<Item> catalyst, int bonus, boolean additive) {
            this(() -> catalyst, bonus, additive);
        }
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(in -> in.group(
                Utils.lazyWithRegistryAccess(Utils.ITEM_LIST_CODEC).fieldOf("catalyst").forGetter(Entry::catalyst),
                Codec.INT.fieldOf("bonus").forGetter(Entry::bonus),
                Codec.BOOL.optionalFieldOf("additive", true).forGetter(Entry::additive)
        ).apply(in, Entry::new));
        public Component desc() {
            return (additive ? Translations.CATALYST_BONUS : Translations.CATALYST_BONUS_NON_ADDITIVE).get(
                    Component.literal(Utils.intWithSign(bonus())).withStyle(ChatFormatting.AQUA),
                    Utils.humanReadableHolderSet(catalyst().get()).copy().withStyle(ChatFormatting.GOLD)
            );
        }
    }
}
