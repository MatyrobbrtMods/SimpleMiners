package com.matyrobbrt.simpleminers.client.widget;

import com.google.common.collect.Lists;
import com.matyrobbrt.simplegui.client.Gui;
import com.matyrobbrt.simplegui.client.element.TexturedElement;
import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.util.Translations;
import com.matyrobbrt.simpleminers.util.Utils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.IntSupplier;

public class EnergyTab extends TexturedElement {
    public static final ResourceLocation LOCATION = new ResourceLocation(SimpleMiners.MOD_ID, "gui/energy_tab.png");

    private final IntSupplier currentAmount, maxAmount, ioRate, usagePerTick;

    public EnergyTab(Gui gui, int x, int y, IntSupplier currentAmount, IntSupplier maxAmount, IntSupplier ioRate, IntSupplier usagePerTick) {
        super(LOCATION, gui, x, y, 26, 26);
        this.currentAmount = currentAmount;
        this.maxAmount = maxAmount;
        this.ioRate = ioRate;
        this.usagePerTick = usagePerTick;
    }

    private static final int MAX_PROGRESS = 24;
    private static final int TEX_WIDTH = 52, TEX_HEIGHT = 26;

    public int getScaled() {
        final int cur = currentAmount.getAsInt();
        final int max = maxAmount.getAsInt();
        return cur != 0 && max != 0
                ? cur * MAX_PROGRESS / max
                : 0;
    }

    @Override
    public void drawBackground(@NotNull PoseStack matrix, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, getTexture());
        final int scaled = getScaled();
        final int remaining = MAX_PROGRESS - scaled;
        if (scaled > 0) {
            Screen.blit(matrix, x + 1, y + 1 + remaining, 27, 24 - scaled + 1, width - 1, scaled, TEX_WIDTH, TEX_HEIGHT);
        }
        for (int i = 0; i < remaining; i++) {
            Screen.blit(matrix, x + 1, y + 1 + i, 27, 25, width - 1, 1, TEX_WIDTH, TEX_HEIGHT);
        }
        Screen.blit(matrix, x, y, 0, 0, width, height, TEX_WIDTH, TEX_HEIGHT);

        if (currentAmount.getAsInt() < usagePerTick.getAsInt() && gui() instanceof WarningWidget.WarningProvider provider) {
            provider.warn(Translations.NEETO.get().withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public void renderToolTip(@NotNull PoseStack matrix, int mouseX, int mouseY) {
        final int current = currentAmount.getAsInt();
        final int max = maxAmount.getAsInt();
        final int io = ioRate.getAsInt();
        final int perTick = usagePerTick.getAsInt();

        final List<Component> components = Lists.newArrayList(
                Translations.GUI_ENERGY.get(Component.literal(Utils.getCompressedCount(current)).withStyle(ChatFormatting.GOLD), max),
                Translations.GUI_TRANSFER_RATE.get(Component.literal(Utils.getCompressedCount(io)).withStyle(ChatFormatting.AQUA)),
                Translations.GUI_USAGE_PER_TICK.get(Component.literal(Utils.getCompressedCount(perTick)).withStyle(ChatFormatting.GREEN))
        );

        gui().displayTooltips(matrix, mouseX, mouseY, components);
    }
}
