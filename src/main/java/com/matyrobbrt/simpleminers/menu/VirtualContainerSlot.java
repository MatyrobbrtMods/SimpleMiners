package com.matyrobbrt.simpleminers.menu;

import com.matyrobbrt.simplegui.client.element.window.Window;
import com.matyrobbrt.simplegui.inventory.SelectedWindowData;
import com.matyrobbrt.simplegui.inventory.slot.VirtualSlot;
import com.matyrobbrt.simplegui.inventory.slot.impl.BasicInventorySlot;
import com.matyrobbrt.simplegui.inventory.slot.impl.InventoryContainerSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

public class VirtualContainerSlot extends InventoryContainerSlot implements VirtualSlot {

    private final InitialPosition initialPosition;
    private final SelectedWindowData windowData;
    private IntSupplier xPositionSupplier = () -> x;
    private IntSupplier yPositionSupplier = () -> y;
    private ItemStack stackToRender = ItemStack.EMPTY;
    @Nullable
    private String tooltipOverride;
    private boolean shouldDrawOverlay;
    @Nullable
    private GuiWindow window;

    public VirtualContainerSlot(BasicInventorySlot slot, SelectedWindowData windowData, Consumer<ItemStack> uncheckedSetter) {
        super(slot, 0, 0, uncheckedSetter);
        this.windowData = windowData;
        this.initialPosition = new InitialPosition(slot.x, slot.y);
    }

    @Override
    public GuiWindow getLinkedWindow() {
        return window;
    }

    @Override
    public int getActualX() {
        return xPositionSupplier.getAsInt();
    }

    @Override
    public int getActualY() {
        return yPositionSupplier.getAsInt();
    }

    @Override
    public void updatePosition(@org.jetbrains.annotations.Nullable Window window, IntSupplier xPositionSupplier, IntSupplier yPositionSupplier) {
        this.xPositionSupplier = xPositionSupplier;
        this.yPositionSupplier = yPositionSupplier;
        this.window = window;
    }

    @Override
    public void updateRenderInfo(@NotNull ItemStack stackToRender, boolean shouldDrawOverlay, @Nullable String tooltipOverride) {
        this.stackToRender = stackToRender;
        this.shouldDrawOverlay = shouldDrawOverlay;
        this.tooltipOverride = tooltipOverride;
    }

    @NotNull
    @Override
    public ItemStack getStackToRender() {
        return stackToRender;
    }

    @Override
    public boolean shouldDrawOverlay() {
        return shouldDrawOverlay;
    }

    @Nullable
    @Override
    public String getTooltipOverride() {
        return tooltipOverride;
    }

    @Override
    public Slot getSlot() {
        return this;
    }

    @Override
    public boolean exists(@Nullable SelectedWindowData windowData) {
        return this.windowData.equals(windowData);
    }

    public InitialPosition getInitialPosition() {
        return initialPosition;
    }

    public record InitialPosition(int x, int y) {}
}
