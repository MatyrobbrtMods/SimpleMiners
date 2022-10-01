package com.matyrobbrt.simpleminers.client.widget.base;

import com.matyrobbrt.simplegui.client.Gui;
import com.matyrobbrt.simplegui.client.element.GuiElement;
import com.matyrobbrt.simplegui.client.element.TexturedElement;
import com.matyrobbrt.simpleminers.util.Utils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public abstract class ScrollableElement extends TexturedElement {
    protected double scroll;
    private boolean isDragging;
    private int dragOffset;
    protected final int maxBarHeight;
    protected final int barWidth;
    protected final int barHeight;
    protected final int barXShift;
    protected int barX;
    protected int barY;

    protected ScrollableElement(ResourceLocation resource, Gui gui, int x, int y, int width, int height,
                                int barXShift, int barYShift, int barWidth, int barHeight, int maxBarHeight) {
        super(resource, gui, x, y, width, height);
        this.barXShift = barXShift;
        this.barX = this.x + barXShift;
        this.barY = this.y + barYShift;
        this.barWidth = barWidth;
        this.barHeight = barHeight;
        this.maxBarHeight = maxBarHeight;
    }

    @Override
    public void resize(int prevLeft, int prevTop, int left, int top) {
        super.resize(prevLeft, prevTop, left, top);
        barX = barX - prevLeft + left;
        barY = barY - prevTop + top;
    }

    @Override
    public void move(int changeX, int changeY) {
        super.move(changeX, changeY);
        barX += changeX;
        barY += changeY;
    }

    protected abstract int getMaxElements();

    protected abstract int getFocusedElements();

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
        int scroll = getScroll();
        if (mouseX >= barX && mouseX <= barX + barWidth && mouseY >= barY + scroll && mouseY <= barY + scroll + barHeight) {
            if (needsScrollBars()) {
                double yAxis = mouseY - getGuiTop();
                dragOffset = (int) (yAxis - (scroll + barY));
                //Mark that we are dragging so that we can continue to "drag" even if our mouse goes off of being over the element
                isDragging = true;
            } else {
                this.scroll = 0;
            }
        }
    }

    @Override
    public void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        super.onDrag(mouseX, mouseY, deltaX, deltaY);
        if (needsScrollBars() && isDragging) {
            double yAxis = mouseY - getGuiTop();
            this.scroll = Mth.clamp((yAxis - barY - dragOffset) / getMax(), 0, 1);
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        super.onRelease(mouseX, mouseY);
        dragOffset = 0;
        isDragging = false;
    }

    protected boolean needsScrollBars() {
        return getMaxElements() > getFocusedElements();
    }

    protected final int getElements() {
        return getMaxElements() - getFocusedElements();
    }

    protected int getScrollElementScaler() {
        return 1;
    }

    private int getMax() {
        return maxBarHeight - barHeight;
    }

    public void reset() {
        scroll = 0;
    }

    protected int getScroll() {
        //Calculate thumb position along scrollbar
        int max = getMax();
        return Mth.clamp((int) (scroll * max), 0, max);
    }

    public int getCurrentSelection() {
        return needsScrollBars() ? (int) ((getElements() + 0.5) * scroll) : 0;
    }

    public boolean adjustScroll(double delta) {
        if (delta != 0 && needsScrollBars()) {
            int elements = Utils.clampToInt(Math.ceil(getElements() / (double) getScrollElementScaler()));
            if (elements > 0) {
                if (delta > 0) {
                    delta = 1;
                } else {
                    delta = -1;
                }
                scroll = (float) Mth.clamp(scroll - delta / elements, 0, 1);
                return true;
            }
        }
        return false;
    }

    protected void drawScrollBar(PoseStack matrix, int textureWidth, int textureHeight) {
        RenderSystem.setShaderTexture(0, getTexture());
        //Top border
        blit(matrix, barX - 1, barY - 1, 0, 0, textureWidth, 1, textureWidth, textureHeight);
        //Middle border
        blit(matrix, barX - 1, barY, 6, maxBarHeight, 0, 1, textureWidth, 1, textureWidth, textureHeight);
        //Bottom border
        blit(matrix, barX - 1, y + maxBarHeight + 2, 0, 0, textureWidth, 1, textureWidth, textureHeight);
        //Scroll bar
        blit(matrix, barX, barY + getScroll(), 0, 2, barWidth, barHeight, textureWidth, textureHeight);
    }

    @Override
    public boolean hasPersistentData() {
        return true;
    }

    @Override
    public void syncFrom(GuiElement element) {
        super.syncFrom(element);
        ScrollableElement old = (ScrollableElement) element;
        if (needsScrollBars() && old.needsScrollBars()) {
            //Only copy scrolling if we need scroll bars and used to also need scroll bars
            scroll = old.scroll;
        }
    }
}
