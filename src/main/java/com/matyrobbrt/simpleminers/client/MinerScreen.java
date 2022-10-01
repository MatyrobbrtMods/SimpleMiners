package com.matyrobbrt.simpleminers.client;

import com.matyrobbrt.simplegui.client.Gui;
import com.matyrobbrt.simplegui.client.SimpleGui;
import com.matyrobbrt.simplegui.client.Texture;
import com.matyrobbrt.simplegui.client.element.GuiElement;
import com.matyrobbrt.simplegui.client.element.builder.ButtonBuilder;
import com.matyrobbrt.simplegui.client.element.button.SimpleButton;
import com.matyrobbrt.simplegui.client.element.slot.SimpleGuiVirtualSlot;
import com.matyrobbrt.simplegui.client.element.window.Window;
import com.matyrobbrt.simplegui.inventory.WindowType;
import com.matyrobbrt.simplegui.util.Color;
import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.client.widget.EnergyTab;
import com.matyrobbrt.simpleminers.client.widget.MineResultScrollList;
import com.matyrobbrt.simpleminers.client.widget.ProgressBarElement;
import com.matyrobbrt.simpleminers.client.widget.UpgradeScrollList;
import com.matyrobbrt.simpleminers.client.widget.WarningWidget;
import com.matyrobbrt.simpleminers.client.widget.base.ToggleButton;
import com.matyrobbrt.simpleminers.menu.MinerMenu;
import com.matyrobbrt.simpleminers.menu.VirtualContainerSlot;
import com.matyrobbrt.simpleminers.miner.MinerBE;
import com.matyrobbrt.simpleminers.network.SimpleMinersNetwork;
import com.matyrobbrt.simpleminers.network.UninstallUpgradePacket;
import com.matyrobbrt.simpleminers.results.ItemResult;
import com.matyrobbrt.simpleminers.results.ResultSet;
import com.matyrobbrt.simpleminers.util.Translations;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MinerScreen extends SimpleGui<MinerMenu> implements WarningWidget.WarningProvider {
    public MinerScreen(MinerMenu container, Inventory inv) {
        super(container, inv, Translations.GUI_MINER.get());
        inventoryLabelY += 1;
    }

    public static final Texture SEARCH_TEXTURE = new Texture(
            new ResourceLocation(SimpleMiners.MOD_ID, "gui/buttons/search.png"),
            0, 0,
            12, 12,
            12, 12
    );

    public static final Texture CATALYSTS_TEXTURE = new Texture(
            new ResourceLocation(SimpleMiners.MOD_ID, "gui/buttons/catalysts.png"),
            0, 0,
            12, 12,
            12, 12
    );

    public static final Texture UPGRADES_TEXTURE = new Texture(
            new ResourceLocation(SimpleMiners.MOD_ID, "gui/buttons/upgrades.png"),
            0, 0,
            12, 12,
            12, 12
    );

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void addGuiElements() {
        super.addGuiElements();
        addSlots(false);

        addElement(new ProgressBarElement(this, 143, 18, () -> menu.be.progress, menu.be::getTicksPerMine));

        if (menu.be.minerType.energy().enabled()) {
            addElement(new EnergyTab(this, imageWidth - 1, imageHeight - 26 - 2, menu.be.energy::getEnergyStored, menu.be.energy::getMaxEnergyStored,
                    () -> menu.be.energy.maxReceive, menu.be::getEnergyUsage));
        }

        // Add the warning widget last, so it can receive warnings from all other widgets
        addElement(new WarningWidget(this, this, imageWidth - 4 - 12, 4));

        final int buttonStartX = imageWidth - 20;
        final int buttonStopY = 4 + 10 + 4 + 52;
        addElement(ButtonBuilder.builder(this, buttonStartX, buttonStopY)
                .withWidth(12).withHeight(12)
                .opensWindow(() -> new ResultsWindow(
                        this, 10, 10
                ))
                .withTexture(SEARCH_TEXTURE)
                .onHover(GuiElement.Hoverable.displayTooltip(
                        Translations.TOOLTIP_SHOW_RESULTS.get()
                ))
                .withColor(SimpleButton.State.ACTIVE, Color.rgb(0xFF006E))
                .build());

        addElement(ButtonBuilder.builder(this, buttonStartX, buttonStopY - 12 - 2)
                .withWidth(12).withHeight(12)
                .opensWindow(() -> new CatalystWindow(
                        this, 10, 10
                ))
                .withTexture(CATALYSTS_TEXTURE)
                .onHover(GuiElement.Hoverable.displayTooltip(
                        Translations.TOOLTIP_ADD_CATALYSTS.get()
                ))
                .withColor(SimpleButton.State.ACTIVE, Color.rgb(0x38D7FF))
                .build());

        addElement(ButtonBuilder.builder(this, buttonStartX, buttonStopY - 12 * 2 - 2 * 2)
                .withWidth(12).withHeight(12)
                .opensWindow(() -> new UpgradesWindow(
                        this, 10, 10
                ))
                .withTexture(UPGRADES_TEXTURE)
                .onHover(GuiElement.Hoverable.displayTooltip(
                        Translations.TOOLTIP_INSTALL_UPGRADES.get()
                ))
                .withColor(SimpleButton.State.ACTIVE, Color.rgb(0x78FF3A))
                .build());
    }

    @Override
    protected void renderLabels(@NotNull PoseStack matrix, int mouseX, int mouseY) {
        this.font.draw(matrix, this.title, this.titleLabelX, this.titleLabelY, 4210752);
        this.font.draw(matrix, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752);
        super.renderLabels(matrix, mouseX, mouseY);
    }

    private final List<Component> warnings = new ArrayList<>();
    @Override
    public List<Component> getWarnings() {
        return warnings;
    }

    @Override
    public void warn(Component warning) {
        warnings.add(warning);
    }

    @Override
    public void clearWarnings() {
        warnings.clear();
    }

    public final class ResultsWindow extends Window {
        private static final WindowType WINDOW_TYPE = new WindowType(new ResourceLocation(SimpleMiners.MOD_ID, "results"));

        public static final Texture SHOW_ALL = new Texture(
                new ResourceLocation(SimpleMiners.MOD_ID, "gui/buttons/show_all.png"),
                0, 0,
                12, 12,
                12, 12
        );
        public static final Texture COLLAPSE = new Texture(
                new ResourceLocation(SimpleMiners.MOD_ID, "gui/buttons/collapse.png"),
                0, 0,
                12, 12,
                12, 12
        );
        public static final Texture REFRESH = new Texture(
                new ResourceLocation(SimpleMiners.MOD_ID, "gui/buttons/refresh.png"),
                0, 0,
                12, 12,
                12, 12
        );

        public ResultsWindow(Gui gui, int x, int y) {
            super(gui, x, y, 5 * 18 + 6 * 3 + 2, 3 * 18 + 12 + 6 * 2 + 2, WINDOW_TYPE);
            setStrategy(InteractionStrategy.ALL);

            final ToggleButton toggle = addChild(new ToggleButton(
                    gui(), relativeX + 6, relativeY + 6, 12, 12,
                    Component.empty(), Hoverable.displayTooltip(
                    Translations.TOOLTIP_SHOW_RESULTS.get()
            ),
                    SHOW_ALL, COLLAPSE
            ));

            final DoubleList results = pollResults();
            final MineResultScrollList scrollList = addChild(new MineResultScrollList(
                    gui(), relativeX + 6, relativeY + 6 + 12 + 2,
                    results.all(), results.possible(), toggle::isToggled
            ));

            addChild(ButtonBuilder.builder(this, 6 + 12 + 4, 6)
                    .onHover(Hoverable.displayTooltip(Translations.TOOLTIP_REFRESH_RESULTS.get()))
                    .withHeight(12).withWidth(12)
                    .withTexture(REFRESH)
                    .onClick(() -> {
                        scrollList.reset();
                        final DoubleList res = pollResults();
                        scrollList.allResults = res.all();
                        scrollList.possibleResults = res.possible();
                    }));
        }

        @SuppressWarnings("ConstantConditions")
        public DoubleList pollResults() {
            final List<ItemResult> all = menu.be.getLevel().registryAccess().registryOrThrow(ResultSet.RESULTS_REGISTRY)
                    .stream().filter(it -> it.minerType().equals(menu.be.minerType.name()))
                    .flatMap(it -> it.get().stream())
                    .sorted(Comparator.comparing(ItemResult::weight).reversed())
                    .toList();
            final List<ItemResult> possible = all.stream()
                    .filter(it -> it.predicate().canProduce(menu.be))
                    .toList();
            return new DoubleList(all, possible);
        }

        record DoubleList(List<ItemResult> all, List<ItemResult> possible) {}
    }

    public final class CatalystWindow extends Window {

        public CatalystWindow(Gui gui, int x, int y) {
            super(gui, x, y,  4 * 18 + 2 * 6, 2 * 18 + 2 * 8 + 10, MinerBE.CATALYST_WINDOW);
            setStrategy(InteractionStrategy.ALL);

            findVirtualSlots(VirtualContainerSlot.class, data)
                    .forEach(slot -> addChild(new SimpleGuiVirtualSlot(this, gui(), relativeX + slot.getInitialPosition().x(),
                            relativeY + slot.getInitialPosition().y(), slot)));
        }

        @Override
        public void renderForeground(PoseStack matrix, int mouseX, int mouseY) {
            MINECRAFT.font.draw(matrix,Translations.GUI_CATALYSTS.get(), relativeX + 6, relativeY + 8, 4210752);
        }
    }

    public final class UpgradesWindow extends Window {
        private static final ResourceLocation EXTRACT_UPGRADE = new ResourceLocation(SimpleMiners.MOD_ID, "gui/extract_upgrade_slot.png");
        private static final ResourceLocation INSERT_UPGRADE = new ResourceLocation(SimpleMiners.MOD_ID, "gui/insert_upgrade_slot.png");

        public UpgradesWindow(Gui gui, int x, int y) {
            super(gui, x, y, 18 + 2 * 6 + 4 + 64 + 6, 3 * 8 + 60, MinerBE.UPGRADES_WINDOW);
            setStrategy(InteractionStrategy.ALL);
            findVirtualSlots(VirtualContainerSlot.class, data).forEach(slot ->
                    addChild(new SimpleGuiVirtualSlot(this, gui, relativeX + slot.getInitialPosition().x(),
                            relativeY + slot.getInitialPosition().y(), slot) {
                        @Override
                        protected ResourceLocation getTexture() {
                            // > 40: extract, < 40 insert
                            return slot.getInitialPosition().y() > 40 ? EXTRACT_UPGRADE : INSERT_UPGRADE;
                        }
                    }));

            addChild(new UpgradeScrollList(gui, relativeX + 6, relativeY + 18, 64 + 8, 62, menu.be.upgrades) {
                @Override
                public boolean mouseClicked(double mouseX, double mouseY, int button) {
                    if (this.clicked(mouseX, mouseY) && button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                        final var at = findAtPos(mouseY);
                        if (at != null) {
                            final int amount = Screen.hasShiftDown() ? holder.findTyped(at) : 1;
                            SimpleMinersNetwork.CHANNEL.sendToServer(new UninstallUpgradePacket(at, amount, menu.be.getBlockPos()));
                            return true;
                        }
                    }
                    return super.mouseClicked(mouseX, mouseY, button);
                }
            });

            // TODO "Supported upgrades" button
        }

        @Override
        public void renderForeground(PoseStack matrix, int mouseX, int mouseY) {
            MINECRAFT.font.draw(matrix,Translations.GUI_UPGRADES.get(), relativeX + 6, relativeY + 8, 4210752);
        }
    }
}
