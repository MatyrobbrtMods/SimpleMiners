package com.matyrobbrt.simpleminers.client.widget;

import com.matyrobbrt.simplegui.client.ClientUtil;
import com.matyrobbrt.simplegui.client.Gui;
import com.matyrobbrt.simplegui.client.element.GuiElement;
import com.matyrobbrt.simplegui.util.Color;
import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.client.widget.base.ScrollList;
import com.matyrobbrt.simpleminers.miner.upgrade.MinerUpgradeType;
import com.matyrobbrt.simpleminers.miner.upgrade.UpgradeHolder;
import com.matyrobbrt.simpleminers.util.Translations;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ObjIntConsumer;

public class UpgradeScrollList extends ScrollList implements ScrollList.SelectionProvider {

    public static final ResourceLocation LOCATION = new ResourceLocation(SimpleMiners.MOD_ID, "gui/upgrades_list.png");

    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 60;

    protected final UpgradeHolder holder;
    private MinerUpgradeType current = null;
    public UpgradeScrollList(Gui gui, int x, int y, int width, int height, UpgradeHolder holder) {
        super(gui, x, y, width, height, TEXTURE_HEIGHT / 3);
        this.holder = holder;
    }

    @Override
    public void renderForeground(PoseStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        forEachUpgrade((upgrade, multipliedElement) -> this.drawTextScaledBound(matrix, upgrade.getName(), relativeX + 18 + 2, relativeY + 6 + multipliedElement,
                0xffffff, 40));
    }

    @SuppressWarnings("SameParameterValue")
    void drawTextScaledBound(PoseStack matrix, Component component, float x, float y, int color, float maxLength) {
        int length = getStringWidth(component);

        if (length <= maxLength) {
            drawTextExact(matrix, component, x, y, color);
        } else {
            drawTextWithScale(matrix, component, x, y, color, maxLength / length);
        }
        ClientUtil.resetColour();
    }

    @Override
    public void renderToolTip(@NotNull PoseStack matrix, int mouseX, int mouseY) {
        super.renderToolTip(matrix, mouseX, mouseY);
        if (mouseX >= x && mouseX < x + barXShift) {
            forEachUpgrade((upgrade, multipliedElement) -> {
                if (mouseY >= y + multipliedElement && mouseY < y + multipliedElement + elementHeight) {
                    final List<Component> tooltips = new ArrayList<>();
                    tooltips.add(upgrade.getDescription());
                    tooltips.add(Translations.TOOLTIP_INSTALLED_UPGRADES.get(
                            Component.literal(String.valueOf(holder.findTyped(upgrade))).withStyle(ChatFormatting.GOLD),
                            Component.literal(String.valueOf(upgrade.getMaxAmount())).withStyle(ChatFormatting.AQUA)
                    ));
                    tooltips.add(Translations.TOOLTIP_RC_UNINSTALL.get());
                    gui().displayTooltips(matrix, mouseX, mouseY, tooltips);
                }
            });
        }
    }

    @Override
    protected void renderElements(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        if (hasSelection() && holder.findTyped(current) == 0) {
            clearSelection();
        }
        RenderSystem.setShaderTexture(0, LOCATION);
        forEachUpgrade((upgrade, multipliedElement) -> {
            int shiftedY = y + multipliedElement;
            int j = 1;
            if (upgrade == current) {
                j = 2;
            } else if (mouseX >= x && mouseX < barX && mouseY >= shiftedY && mouseY < shiftedY + elementHeight) {
                j = 0;
            }
            ClientUtil.setColour(upgrade.getColor());
            blit(matrix, x, shiftedY, 0, elementHeight * j, TEXTURE_WIDTH, elementHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            ClientUtil.resetColour();
        });
        forEachUpgrade((upgrade, multipliedElement) -> gui().renderItem(matrix, upgrade.createStack(), x + 2, y + 2 + multipliedElement));
    }

    @Override
    protected int getMaxElements() {
        return holder.getUpgrades().size();
    }

    @Override
    public boolean hasSelection() {
        return current != null;
    }

    @Override
    public void select(int index) {
        final var currentUpgrades = holder.getUpgrades().keySet();
        if (index >= 0 && index < currentUpgrades.size()) {
            final var newSelection = currentUpgrades.toArray(MinerUpgradeType[]::new)[index];
            if (current != newSelection) {
                current = newSelection;
            }
        }
    }

    @Override
    public void clearSelection() {
        current = null;
    }

    @Nullable
    protected MinerUpgradeType findAtPos(double mouseY) {
        final var upgrades = holder.getUpgrades().keySet().toArray(new MinerUpgradeType[0]);
        int currentSelection = getCurrentSelection();
        for (int i = 0; i < getFocusedElements(); i++) {
            int index = currentSelection + i;
            if (index > upgrades.length - 1) {
                break;
            }
            if (y + elementHeight * i <= mouseY && y + elementHeight * (i + 1) > mouseY) return upgrades[index];
        }
        return null;
    }

    protected void forEachUpgrade(ObjIntConsumer<MinerUpgradeType> consumer) {
        final var upgrades = holder.getUpgrades().keySet().toArray(new MinerUpgradeType[0]);
        int currentSelection = getCurrentSelection();
        for (int i = 0; i < getFocusedElements(); i++) {
            int index = currentSelection + i;
            if (index > upgrades.length - 1) {
                break;
            }
            consumer.accept(upgrades[index], elementHeight * i + 1);
        }
    }

    @Override
    public void syncFrom(GuiElement element) {
        super.syncFrom(element);
        this.current = ((UpgradeScrollList) element).current;
    }
}
