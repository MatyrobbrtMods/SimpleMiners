package com.matyrobbrt.simpleminers.util.cap;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.matyrobbrt.simplegui.inventory.slot.InventorySlot;
import com.matyrobbrt.simplegui.util.Action;
import com.matyrobbrt.simplegui.util.InteractionType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record SlotItemHandler(List<InventorySlot> slots) implements IItemHandlerModifiable, INBTSerializable<CompoundTag> {
    @Override
    public int getSlots() {
        return slots.size();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return slots.get(slot).getStack();
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return slots.get(slot).insertItem(stack, simulate ? Action.SIMULATE : Action.EXECUTE, InteractionType.EXTERNAL);
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        return slots.get(slot).extractItem(amount, simulate ? Action.SIMULATE : Action.EXECUTE, InteractionType.EXTERNAL);
    }

    @Override
    public int getSlotLimit(int slotIndex) {
        final var slot = slots.get(slotIndex);
        return slot.getLimit(slot.getStack());
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return slots.get(slot).isItemValid(stack);
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        slots.get(slot).setStack(stack);
    }

    @Override
    public CompoundTag serializeNBT() {
        ListTag nbtTagList = new ListTag();
        for (int i = 0; i < slots.size(); i++) {
            if (!slots.get(i).isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("slot", i);
                itemTag.put("data", slots.get(i).serializeNBT());
                nbtTagList.add(itemTag);
            }
        }
        CompoundTag nbt = new CompoundTag();
        nbt.put("items", nbtTagList);
        nbt.putInt("size", slots.size());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        ListTag tagList = nbt.getList("items", Tag.TAG_COMPOUND);
        for (int i = 0; i < tagList.size(); i++) {
            CompoundTag itemTags = tagList.getCompound(i);
            int slot = itemTags.getInt("slot");

            if (slot >= 0 && slot < slots.size()) {
                slots.get(slot).deserializeNBT(itemTags.getCompound("data"));
            }
        }
    }

    public static final class Builder {
        private final List<InventorySlot> slots = new ArrayList<>();

        @CanIgnoreReturnValue
        public Builder add(InventorySlot slot) {
            slots.add(slot);
            return this;
        }

        public SlotItemHandler build() {
            // Make the list immutable so slots aren't dynamically and magically added
            return new SlotItemHandler(List.copyOf(slots));
        }
    }

}
