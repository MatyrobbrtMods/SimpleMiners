package com.matyrobbrt.simpleminers.client.widget.base;

import com.matyrobbrt.simplegui.client.Gui;
import com.matyrobbrt.simplegui.client.Texture;
import com.matyrobbrt.simplegui.client.element.GuiElement;
import com.matyrobbrt.simplegui.client.element.button.SimpleButton;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ToggleButton extends SimpleButton {
    private boolean toggled;

    private final Texture on, off;

    public ToggleButton(Gui gui, int x, int y, int width, int height, Component text, @Nullable GuiElement.Hoverable onHover, Texture on, Texture off) {
        super(gui, x, y, width, height, text, null, onHover);
        this.on = on;
        this.off = off;
    }

    @Override
    protected void onLeftClick() {
        toggle();
    }

    @Override
    protected void onRightClick() {
        toggle();
    }

    private void toggle() {
        this.toggled = !this.toggled;
    }

    public boolean isToggled() {
        return toggled;
    }

    @Override
    public void drawBackground(@NotNull PoseStack matrix, int mouseX, int mouseY, float partialTick) {
        super.drawBackground(matrix, mouseX, mouseY, partialTick);
        drawButton(matrix, mouseX, mouseY);
        if (toggled) {
            on.render(matrix, x, y);
        } else {
            off.render(matrix, x, y);
        }
    }
}
