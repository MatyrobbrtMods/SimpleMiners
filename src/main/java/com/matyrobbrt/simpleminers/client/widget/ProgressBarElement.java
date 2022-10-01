package com.matyrobbrt.simpleminers.client.widget;

import com.matyrobbrt.simplegui.client.Gui;
import com.matyrobbrt.simplegui.client.element.TexturedElement;
import com.matyrobbrt.simpleminers.SimpleMiners;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntSupplier;

public class ProgressBarElement extends TexturedElement {
    public static final ResourceLocation TEXTURE = new ResourceLocation(SimpleMiners.MOD_ID, "gui/progress_bar.png");
    private final IntSupplier currentProgress, maxProgress;
    public ProgressBarElement(Gui gui, int x, int y, IntSupplier currentProgress, IntSupplier maxProgress) {
        super(TEXTURE, gui, x, y, 16, 52);
        this.currentProgress = currentProgress;
        this.maxProgress = maxProgress;
    }

    public static final int TEX_WIDTH = 16 * 2, TEX_HEIGHT = 52;
    public static final int MAX_PROGRESS = 52;
    public int calcProgress() {
        final var cur = currentProgress.getAsInt();
        final var max = maxProgress.getAsInt();
        return Math.max(0, cur != 0 && max != 0
                ? cur * MAX_PROGRESS / max
                : 0);
    }

    @Override
    public void drawBackground(@NotNull PoseStack matrix, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, getTexture());
        int progress = calcProgress();
        if (progress > MAX_PROGRESS) progress = MAX_PROGRESS;
        final int remaining = MAX_PROGRESS - progress;
        if (progress > 0)
            Screen.blit(matrix, x, y + remaining, 0, remaining, width, progress, TEX_WIDTH, TEX_HEIGHT);
        if (remaining > 0)
            Screen.blit(matrix, x, y, 16, 0, width, remaining, TEX_WIDTH, TEX_HEIGHT);
    }
}
