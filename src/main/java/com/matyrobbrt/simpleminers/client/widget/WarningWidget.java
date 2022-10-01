package com.matyrobbrt.simpleminers.client.widget;

import com.matyrobbrt.simplegui.client.Gui;
import com.matyrobbrt.simplegui.client.element.TexturedElement;
import com.matyrobbrt.simpleminers.SimpleMiners;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WarningWidget extends TexturedElement {
    public static final ResourceLocation LOCATION = new ResourceLocation(SimpleMiners.MOD_ID, "gui/warning.png");

    private final WarningProvider provider;
    public WarningWidget(WarningProvider provider, Gui gui, int x, int y) {
        super(LOCATION, gui, x, y, 10, 10);
        this.provider = provider;
    }

    private static final int TEX_WIDTH = 10, TEX_HEIGHT = 10;

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);
        // Clear warnings before background is rendered (that is when warnings should be added)
        provider.clearWarnings();
    }

    @Override
    public void drawBackground(@NotNull PoseStack matrix, int mouseX, int mouseY, float partialTick) {
        if (!provider.getWarnings().isEmpty()) {
            RenderSystem.setShaderTexture(0, LOCATION);
            Screen.blit(matrix, x, y, 0, 0, width, height, TEX_WIDTH, TEX_HEIGHT);
        }
    }

    @Override
    public void renderToolTip(@NotNull PoseStack matrix, int mouseX, int mouseY) {
        gui().displayTooltips(matrix, mouseX, mouseY, provider.getWarnings());
    }

    public interface WarningProvider {
        List<Component> getWarnings();

        void warn(Component warning);

        void clearWarnings();
    }
}
