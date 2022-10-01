package com.matyrobbrt.simpleminers.menu;

import com.matyrobbrt.simplegui.inventory.ContentsListener;
import com.matyrobbrt.simplegui.inventory.SelectedWindowData;
import com.matyrobbrt.simplegui.util.InteractionType;
import com.matyrobbrt.simpleminers.item.MinerCatalyst;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CatalystVirtualSlot extends VirtualInventorySlot {
    public static CatalystVirtualSlot at(int x, int y, SelectedWindowData windowData) {
        return new CatalystVirtualSlot(InteractionType.Predicate.TRUE, null, x, y, windowData);
    }

    public CatalystVirtualSlot(InteractionType.Predicate canExtract, @Nullable ContentsListener listener, int x, int y, SelectedWindowData windowData) {
        super(canExtract, (stack, interactionType) -> stack.getItem() instanceof MinerCatalyst,
                stack -> stack.getItem() instanceof MinerCatalyst, listener, x, y, windowData);
    }

    @Override
    public int getLimit(@NotNull ItemStack stack) {
        return 1; // Only one catalyst / slot
    }
}
