package com.matyrobbrt.simpleminers.client.widget;

import com.matyrobbrt.simplegui.client.Gui;
import com.matyrobbrt.simplegui.jei.JEITarget;
import com.matyrobbrt.simplegui.util.Utils;
import com.matyrobbrt.simpleminers.client.widget.base.ScrollList;
import com.matyrobbrt.simpleminers.results.ItemResult;
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
import java.util.function.BooleanSupplier;

public class MineResultScrollList extends ScrollList implements JEITarget {
    private static final ResourceLocation SLOT = Utils.getResource("gui", "slot");
    private static final int ITEMS_PER_ROW = 5;

    public List<ItemResult> allResults;
    public List<ItemResult> possibleResults;

    private final BooleanSupplier showAll;

    public MineResultScrollList(Gui gui, int x, int y, List<ItemResult> allResults, List<ItemResult> possibleResults, BooleanSupplier showAll) {
        super(gui, x, y, 18 * ITEMS_PER_ROW + 8, 18 * 3 + 2, 18, null);
        this.allResults = allResults;
        this.possibleResults = possibleResults;
        this.showAll = showAll;
    }

    @Override
    protected void renderElements(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        forEachResult((result, x1, y1) -> {
            RenderSystem.setShaderTexture(0, SLOT);
            blit(matrix, x + x1, y + y1, 0, 0, 18, 18, 18, 18);
            gui().renderItem(matrix, result.item(), x + x1 + 1, y + y1 + 1);
        });
    }

    @Override
    public void renderToolTip(@NotNull PoseStack matrix, int mouseX, int mouseY) {
        this.findFirst((result, x1, y1) -> {
            if (isHovered(x1, y1, mouseX, mouseY)) {
                gui().renderItemTooltipWithExtra(matrix, result.item(), mouseX, mouseY, result.tooltip());
                return 1;
            }
            return null;
        });
    }

    public boolean isHovered(int relativeX, int relativeY, double mouseX, double mouseY) {
        final int actualX = relativeX + x;
        final int actualY = relativeY + y;
        return actualX <= mouseX && actualX + 17 >= mouseX && actualY <= mouseY && actualY + 17 >= mouseY;
    }

    @Override
    protected int getMaxElements() {
        final int count = (showAll.getAsBoolean() ? allResults : possibleResults).size();
        return count / ITEMS_PER_ROW + (count % ITEMS_PER_ROW > 0 ? 1 : 0);
    }

    private void forEachResult(ResultConsumer consumer) {
        final List<ItemResult> list = showAll.getAsBoolean() ? allResults : possibleResults;
        for (int i = 0; i < getFocusedElements(); i++) {
            int index = getCurrentSelection() * ITEMS_PER_ROW + i * ITEMS_PER_ROW;
            if (index >= list.size()) {
                break;
            }
            for (int z = 0; z < ITEMS_PER_ROW; z++) {
                final int actualIndex = index + z;
                if (actualIndex >= list.size()) break;
                consumer.accept(list.get(actualIndex), 18 * z, elementHeight * i + 1);
            }
        }
    }

    @Nullable
    private <T> T findFirst(ResultFunction<T> function) {
        final List<ItemResult> list = showAll.getAsBoolean() ? allResults : possibleResults;
        for (int i = 0; i < getFocusedElements(); i++) {
            int index = getCurrentSelection() * ITEMS_PER_ROW + i * ITEMS_PER_ROW;
            if (index >= list.size()) {
                break;
            }
            for (int z = 0; z < ITEMS_PER_ROW; z++) {
                final int actualIndex = index + z;
                if (actualIndex >= list.size()) break;
                final T res = function.accept(list.get(actualIndex), 18 * z, elementHeight * i + 1);
                if (res != null) return res;
            }
        }
        return null;
    }

    @Override
    public @Nullable Object getIngredient(double mouseX, double mouseY) {
        return findFirst((result, x1, y1) -> isHovered(x1, y1, mouseX, mouseY) ? result.item() : null);
    }

    @FunctionalInterface
    protected interface ResultConsumer {
        void accept(ItemResult result, int x, int y);
    }
    @FunctionalInterface
    protected interface ResultFunction<T> {
        T accept(ItemResult result, int x, int y);
    }
}
