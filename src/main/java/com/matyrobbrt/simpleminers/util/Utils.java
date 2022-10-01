package com.matyrobbrt.simpleminers.util;

import com.google.gson.JsonElement;
import com.matyrobbrt.simplegui.util.Action;
import com.matyrobbrt.simplegui.util.InteractionType;
import com.matyrobbrt.simpleminers.util.cap.SlotItemHandler;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.holdersets.AndHolderSet;
import net.minecraftforge.registries.holdersets.CompositeHolderSet;
import net.minecraftforge.registries.holdersets.NotHolderSet;
import net.minecraftforge.registries.holdersets.OrHolderSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class Utils {
    @SuppressWarnings("deprecation")
    public static final Codec<HolderSet<Item>> ITEM_LIST_CODEC = RegistryCodecs.homogeneousList(Registry.ITEM_REGISTRY, Registry.ITEM.byNameCodec());

    public static final InteractionType.Predicate INTERNAL_ONLY = ((stack, interactionType) -> interactionType == InteractionType.INTERNAL);

    @SuppressWarnings("UnusedReturnValue")
    @NotNull
    public static ItemStack insertItem(SlotItemHandler dest, @NotNull ItemStack stack, boolean simulate, InteractionType interaction) {
        if (stack.isEmpty())
            return stack;

        for (int i = 0; i < dest.getSlots(); i++) {
            stack = dest.slots().get(i).insertItem(stack, simulate ? Action.SIMULATE : Action.EXECUTE, interaction);
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }

        return stack;
    }

    /**
     * Creates a codec that can process for either a single member of provided codec or a list, always decoding to a
     * list.
     */
    public static <O> Codec<List<O>> singleOrListOf(Codec<O> codec) {
        return Codec.either(codec, codec.listOf()).xmap(either -> either.map(
                List::of, Function.identity()
        ), it -> it.size() == 1 ? Either.left(it.get(0)) : Either.right(it));
    }

    public static int clampToInt(double d) {
        if (d < Integer.MAX_VALUE) {
            return (int) d;
        }
        return Integer.MAX_VALUE;
    }

    public static String getCompressedCount(int count) {
        if (count >= 1_000_000) {
            final var n = ((double) count) / 1_000_000;
            return (Math.round(n * 10.0) / 10.0) + "M";
        } else if (count > 1000) {
            final var n = ((double) count) / 1000;
            return (Math.round(n * 10.0) / 10.0) + "k";
        }
        return String.valueOf(count);
    }

    public static <T> Component humanReadableHolderSet(HolderSet<T> holderSet) {
        if (holderSet instanceof HolderSet.Named<T> named) {
            return Component.literal("#" + named.key().location());
        } else if (holderSet instanceof OrHolderSet<T> or) {
            return Translations.ANY_OF.get(fromComposite(or));
        } else if (holderSet instanceof AndHolderSet<T> and) {
            return Translations.ALL_OF.get(fromComposite(and));
        } else if (holderSet instanceof NotHolderSet<T> not) {
            return Translations.NOT.get(humanReadableHolderSet(not));
        }
        final List<String> names = holderSet.stream()
                .flatMap(it -> it.unwrapKey().map(set -> set.location().toString()).stream())
                .toList();
        return Component.literal(String.join(", ", names));
    }

    private static <T> Component fromComposite(CompositeHolderSet<T> composite) {
        final List<Component> components = composite.getComponents()
                .stream().map(Utils::humanReadableHolderSet)
                .toList();
        return join(Component.literal(" / "), components, it -> it.copy().withStyle(ChatFormatting.AQUA));
    }

    public static MutableComponent join(Component delimiter, Iterable<? extends Component> itr, Function<Component, Component> creator) {
        return join(delimiter, itr.iterator(), creator);
    }
    public static MutableComponent join(Component delimiter, Iterator<? extends Component> it, Function<Component, Component> creator) {
        MutableComponent component = Component.empty();
        while (it.hasNext()) {
            final var val = it.next();
            component = component.append(creator.apply(val));
            if (it.hasNext()) {
                component = component.append(delimiter);
            }
        }
        return component;
    }

    public static String intWithSign(int i) {
        return i <= 0 ? String.valueOf(i) : "+" + i;
    }

    public static void forEachInJson(@Nullable JsonElement element, Consumer<JsonElement> consumer) {
        if (element == null) return;
        if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(consumer);
        } else {
            consumer.accept(element);
        }
    }
}
