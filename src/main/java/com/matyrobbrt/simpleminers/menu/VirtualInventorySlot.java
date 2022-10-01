package com.matyrobbrt.simpleminers.menu;

import com.matyrobbrt.simplegui.inventory.ContentsListener;
import com.matyrobbrt.simplegui.inventory.SelectedWindowData;
import com.matyrobbrt.simplegui.inventory.slot.impl.BasicInventorySlot;
import com.matyrobbrt.simplegui.util.InteractionType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Predicate;

public class VirtualInventorySlot extends BasicInventorySlot {
    public static VirtualInventorySlot at(@javax.annotation.Nullable ContentsListener listener, int x, int y, SelectedWindowData windowData) {
        return at(e -> true, listener, x, y, windowData);
    }

    public static VirtualInventorySlot at(Predicate<@NonNull ItemStack> validator, @javax.annotation.Nullable ContentsListener listener, int x, int y, SelectedWindowData windowData) {
        Objects.requireNonNull(validator, "Item validity check cannot be null");
        return new VirtualInventorySlot(InteractionType.Predicate.TRUE, InteractionType.Predicate.TRUE, validator, listener, x, y, windowData);
    }
    public static VirtualInventorySlot at(InteractionType.Predicate canInsert, Predicate<@NonNull ItemStack> validator, @javax.annotation.Nullable ContentsListener listener, int x, int y, SelectedWindowData windowData) {
        Objects.requireNonNull(validator, "Item validity check cannot be null");
        return new VirtualInventorySlot(InteractionType.Predicate.TRUE, canInsert, validator, listener, x, y, windowData);
    }

    private final SelectedWindowData windowData;
    public VirtualInventorySlot(InteractionType.Predicate canExtract, InteractionType.Predicate canInsert, Predicate<@NonNull ItemStack> validator, @Nullable ContentsListener listener, int x, int y, SelectedWindowData windowData) {
        super(canExtract, canInsert, validator, listener, x, y);
        this.windowData = windowData;
    }

    @NotNull
    @Override
    public Slot createContainerSlot() {
        return new VirtualContainerSlot(this, windowData, this::setStackUnchecked);
    }
}
