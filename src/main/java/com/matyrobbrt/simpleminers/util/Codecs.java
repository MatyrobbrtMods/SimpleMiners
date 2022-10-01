package com.matyrobbrt.simpleminers.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Function;

public class Codecs {
    @SuppressWarnings("ConstantConditions")
    public static final Codec<ItemStack> STACK_CODEC = Codec.either(ItemStack.CODEC, ResourceLocation.CODEC).xmap(
            either -> either.map(Function.identity(), it -> ForgeRegistries.ITEMS.getValue(it).getDefaultInstance()),
            Either::left
    );
}
