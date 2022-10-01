package com.matyrobbrt.simpleminers.client.widget.base;

import com.matyrobbrt.simplegui.client.Gui;
import com.matyrobbrt.simpleminers.SimpleMiners;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ScrollList extends ScrollableElement {
    public static final ResourceLocation SCROLL_LIST = new ResourceLocation(SimpleMiners.MOD_ID, "gui/scroll_list.png");
    public static final int TEXTURE_WIDTH = 6;
    public static final int TEXTURE_HEIGHT = 6;

    @Nullable
    protected SelectionProvider selection;
    protected final int elementHeight;

    protected ScrollList(Gui gui, int x, int y, int width, int height, int elementHeight, @Nullable SelectionProvider selection) {
        super(SCROLL_LIST, gui, x, y, width, height, width - 6, 2, 4, 4, height - 4);
        this.elementHeight = elementHeight;
        this.selection = selection;
    }
    protected ScrollList(Gui gui, int x, int y, int width, int height, int elementHeight) {
        this(gui, x, y, width, height,elementHeight, null);
        this.selection = this instanceof SelectionProvider prov ? prov : null;
    }

    @Override
    protected int getFocusedElements() {
        return (height - 2) / elementHeight;
    }

    protected abstract void renderElements(PoseStack matrix, int mouseX, int mouseY, float partialTicks);

    @Override
    public void drawBackground(@NotNull PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        //Draw Scroll
        drawScrollBar(matrix, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        //Draw the elements
        renderElements(matrix, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
        if (selection != null && mouseX >= x + 1 && mouseX < barX - 1 && mouseY >= y + 1 && mouseY < y + height - 1) {
            int index = getCurrentSelection();
            int focused = getFocusedElements();
            int maxElements = getMaxElements();
            for (int i = 0; i < focused && index + i < maxElements; i++) {
                int shiftedY = y + 1 + elementHeight * i;
                if (mouseY >= shiftedY && mouseY <= shiftedY + elementHeight) {
                    selection.select(index + i);
                    return;
                }
            }
            // Only clear the selection if we clicked in the area but not on a selectable index
            selection.clearSelection();
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return isMouseOver(mouseX, mouseY) && adjustScroll(delta) || super.mouseScrolled(mouseX, mouseY, delta);
    }

    public interface SelectionProvider {
        boolean hasSelection();
        void select(int index);
        void clearSelection();
    }
}
