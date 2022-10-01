package com.matyrobbrt.simpleminers.results;

import com.google.gson.JsonElement;
import com.matyrobbrt.simpleminers.miner.MinerBE;
import com.matyrobbrt.simpleminers.results.modifier.ResultModifier;
import com.matyrobbrt.simpleminers.results.modifier.ResultModifiers;
import com.matyrobbrt.simpleminers.results.predicate.ResultPredicate;
import com.matyrobbrt.simpleminers.results.predicate.ResultPredicates;
import com.matyrobbrt.simpleminers.util.Translations;
import com.matyrobbrt.simpleminers.util.Utils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record ItemResult(ItemStack item, int weight, ResultPredicate predicate, ResultModifier modifier) implements IItemResult {

    public static final Codec<ItemResult> CODEC = RecordCodecBuilder.create(in -> IItemResult.codecStructure(in).apply(in, ItemResult::new));
    public static final Codec<List<ItemResult>> CODEC_LIST = Utils.singleOrListOf(CODEC);

    public ItemResult(ItemStack stack, int weight, ResultPredicate predicate) {
        this(stack, weight, predicate, ResultModifiers.NOP);
    }
    public ItemResult(ItemStack stack, int weight, ResultModifier modifier) {
        this(stack, weight, ResultPredicates.TRUE, modifier);
    }
    public ItemResult(ItemStack stack, int weight) {
        this(stack, weight, ResultPredicates.TRUE);
    }
    @SuppressWarnings("unused")
    public ItemResult(ItemStack stack) {
        this(stack, DEFAULT_WEIGHT);
    }

    public static final int DEFAULT_WEIGHT = 1;
    public static final Lazy<RegistryOps<JsonElement>> OPS = Lazy.of(() -> RegistryOps.create(JsonOps.INSTANCE, RegistryAccess.builtinCopy()));
    public static final Lazy<RegistryOps<Tag>> NBT_OPS = Lazy.of(() -> RegistryOps.create(NbtOps.INSTANCE, RegistryAccess.builtinCopy()));

    public List<Component> tooltip() {
        final List<Component> components = new ArrayList<>();
        components.add(Translations.TOOLTIP_WEIGHT.get(Component.literal(String.valueOf(weight())).withStyle(ChatFormatting.GOLD)));

        {
            final List<Component> desc = predicate().getDescription();
            if (!desc.isEmpty()) {
                components.add(Translations.TOOLTIP_REQUIREMENTS.get().withStyle(ChatFormatting.GOLD));
                components.addAll(desc);
            }
        }

        {
            final List<Component> desc = modifier().getDescription();
            if (!desc.isEmpty()) {
                if (components.size() > 1) components.add(Component.empty());
                components.add(Translations.TOOLTIP_MODIFIERS.get().withStyle(ChatFormatting.GOLD));
                components.addAll(desc);
            }
        }
        return components;
    }

    public Weighted weighted(MinerBE minerBE) {
        return new Weighted(Weight.of(modifier.modifyWeight(minerBE, weight(), item())), item, modifier);
    }

    public record Weighted(Weight weight, ItemStack stack, ResultModifier modifier) implements WeightedEntry, Supplier<ItemStack> {
        @Override
        public @NotNull Weight getWeight() {
            return weight;
        }

        @Override
        public ItemStack get() {
            return modifier.modifyStack(stack().copy());
        }
    }
}