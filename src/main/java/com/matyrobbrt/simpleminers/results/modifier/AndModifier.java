package com.matyrobbrt.simpleminers.results.modifier;

import com.matyrobbrt.simpleminers.miner.MinerBE;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record AndModifier(List<ResultModifier> modifiers) implements ResultModifier {
    @Override
    public int modifyWeight(MinerBE miner, int originalWeight, ItemStack originalStack) {
        int weight = originalWeight;
        for (final var mod : modifiers)
            weight = mod.modifyWeight(miner, weight, originalStack);
        return weight;
    }

    @Override
    public ItemStack modifyStack(ItemStack stack) {
        for (final var mod : modifiers())
            stack = mod.modifyStack(stack);
        return stack;
    }

    @Override
    public Codec<? extends ResultModifier> codec() {
        return ResultModifiers.AND_CODEC;
    }

    @Override
    public List<Component> getDescription() {
        return modifiers.stream().flatMap(it -> it.getDescription().stream()).toList();
    }
}
