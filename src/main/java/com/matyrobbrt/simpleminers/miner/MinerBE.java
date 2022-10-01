package com.matyrobbrt.simpleminers.miner;

import com.matyrobbrt.simplegui.inventory.ContentsListener;
import com.matyrobbrt.simplegui.inventory.SelectedWindowData;
import com.matyrobbrt.simplegui.inventory.WindowType;
import com.matyrobbrt.simplegui.inventory.slot.InventorySlot;
import com.matyrobbrt.simplegui.inventory.slot.impl.BasicInventorySlot;
import com.matyrobbrt.simplegui.util.Action;
import com.matyrobbrt.simplegui.util.InteractionType;
import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.item.MinerUpgrade;
import com.matyrobbrt.simpleminers.menu.CatalystVirtualSlot;
import com.matyrobbrt.simpleminers.menu.MinerMenu;
import com.matyrobbrt.simpleminers.menu.VirtualInventorySlot;
import com.matyrobbrt.simpleminers.miner.upgrade.MinerUpgradeType;
import com.matyrobbrt.simpleminers.miner.upgrade.UpgradeHolder;
import com.matyrobbrt.simpleminers.results.ResultSet;
import com.matyrobbrt.simpleminers.util.NBTGroup;
import com.matyrobbrt.simpleminers.util.Translations;
import com.matyrobbrt.simpleminers.util.Utils;
import com.matyrobbrt.simpleminers.util.cap.EnergyStorage;
import com.matyrobbrt.simpleminers.util.cap.SlotItemHandler;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MinerBE extends BlockEntity implements MenuProvider, ContentsListener {
    public static final WindowType CATALYST_WINDOW = new WindowType(new ResourceLocation(SimpleMiners.MOD_ID, "catalysts"));
    public static final WindowType UPGRADES_WINDOW = new WindowType(new ResourceLocation(SimpleMiners.MOD_ID, "upgrades"));

    public final SlotItemHandler itemHandler;
    private final LazyOptional<IItemHandler> itemHandlerLazy;

    public final SlotItemHandler catalysts;

    @Nullable
    public final EnergyStorage energy;
    private final LazyOptional<IEnergyStorage> energyLazy;

    public final VirtualInventorySlot upgradesIn;
    public final VirtualInventorySlot upgradesOut;
    public final UpgradeHolder upgrades;

    private final RandomSource random = RandomSource.create();
    public final MinerType minerType;
    public int progress;

    private final NBTGroup nbtGroup = NBTGroup.make();

    public MinerBE(BlockEntityType<?> beType, BlockPos pPos, BlockState pBlockState, MinerType minerType) {
        super(beType, pPos, pBlockState);
        this.minerType = minerType;

        {
            final var itemHandlerBuilder = new SlotItemHandler.Builder();
            for (int slotY = 0; slotY < 3; slotY++) {
                for (int slotX = 0; slotX < 7; slotX++) {
                    itemHandlerBuilder.add(BasicInventorySlot.at(InteractionType.Predicate.TRUE, Utils.INTERNAL_ONLY, this, 8 + slotX * 18, 18 + slotY * 18));
                }
            }
            this.itemHandler = itemHandlerBuilder.build();
            this.itemHandlerLazy = LazyOptional.of(() -> itemHandler);
            nbtGroup.add("inventory", itemHandler, Tag.TAG_COMPOUND);
        }

        if (minerType.energy().enabled()) {
            this.energy = new EnergyStorage(minerType.energy().capacity(), minerType.energy().ioRate(), 0);
            this.energyLazy = LazyOptional.of(() -> energy);
            nbtGroup.add("energy", energy, Tag.TAG_COMPOUND);
        } else {
            this.energy = null;
            this.energyLazy = LazyOptional.empty();
        }

        {
            final var window = new SelectedWindowData(CATALYST_WINDOW);
            final var catalystsBuilder = new SlotItemHandler.Builder();
            for (int slotY = 0; slotY < 2; slotY++) {
                for (int slotX = 0; slotX < 4; slotX++) {
                    catalystsBuilder.add(CatalystVirtualSlot.at(6 + slotX * 18, 12 + 8 + slotY * 18, window));
                }
            }
            this.catalysts = catalystsBuilder.build();
            nbtGroup.add("catalysts", catalysts, Tag.TAG_COMPOUND);
        }

        {
            int x = 80;
            final Predicate<ItemStack> isUpgrade = stack -> stack.getItem() instanceof MinerUpgrade up
                    && up.getType().isEnabled(minerType);

            this.upgradesOut = VirtualInventorySlot.at(Utils.INTERNAL_ONLY, isUpgrade, null, x, 54, new SelectedWindowData(UPGRADES_WINDOW));
            this.upgrades = UpgradeHolder.make(upgradesOut);

            this.upgradesIn = VirtualInventorySlot.at(isUpgrade.and(s -> {
                final MinerUpgradeType type = ((MinerUpgrade) (s.getItem())).getType();
                return type.getMaxAmount() - this.upgrades.findTyped(type) > 0;
            }), null, x, 26, new SelectedWindowData(UPGRADES_WINDOW));

            nbtGroup.grouped("upgrades", group -> group
                    .add("data", upgrades)
                    .add("inSlot", upgradesIn)
                    .add("outSlot", upgradesOut));
        }

        nbtGroup.addInt("progress", () -> progress, p -> progress = p);
    }

    public void serverTick() {
        {
            final ItemStack upgrade = upgradesIn.getStack();
            if (!upgrade.isEmpty() && upgrade.getItem() instanceof MinerUpgrade up) {
                final MinerUpgradeType type = up.getType();
                final int inserted = upgrades.insertUpgrade(type, upgrade.getCount());
                if (inserted > 0)
                    upgradesIn.extractItem(inserted, Action.EXECUTE, InteractionType.INTERNAL);
            }
        }

        if (energy != null) {
            final int energyUsage = getEnergyUsage();
            if (energy.getEnergyStored() >= energyUsage) {
                progress++;
                energy.extractInternal(energyUsage);
            } else if (progress >= 0) {
                progress--;
            }
        } else {
            progress++;
        }

        if (progress >= getTicksPerMine()) {
            final List<ItemStack> results = pollResult(minerType.rollsPerOperation() + getProductionBonus());

            for (var result : results) {
                for (final var up : upgrades.getUpgrades().keySet()) {
                    result = up.modifyOutput(result);
                }
                Utils.insertItem(
                        itemHandler, result, false, InteractionType.INTERNAL
                );
            }
            
            progress = 0;
        }
    }

    public int getTicksPerMine() {
        return Math.max(1, minerType.ticksPerOperation() - upgrades.findTyped(MinerUpgradeType.SPEED) * MinerUpgradeType.SPEED.getInt("timeDecrease", minerType, 10));
    }

    public int getEnergyUsage() {
        final int speedPenalty = upgrades.findTyped(MinerUpgradeType.SPEED) * MinerUpgradeType.SPEED.getInt("energyUsage", minerType, 30);
        final int energyUpgradeBonus = upgrades.findTyped(MinerUpgradeType.ENERGY) * MinerUpgradeType.ENERGY.getInt("usageDecrease", minerType, 15);
        return Math.max(0, minerType.energy().usagePerTick() + speedPenalty - energyUpgradeBonus);
    }

    @Override
    public Component getDisplayName() {
        return Translations.GUI_MINER.get();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new MinerMenu(this, i, inventory);
    }

    @Override
    public void onContentsChanged() {

    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.put("customData", nbtGroup.serializeNBT());
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        if (pTag.contains("customData", Tag.TAG_COMPOUND)) {
            nbtGroup.deserializeNBT(pTag.getCompound("customData"));
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction direction) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandlerLazy.cast();
        } else if (cap == ForgeCapabilities.ENERGY) {
            return energyLazy.cast();
        }
        return super.getCapability(cap, direction);
    }

    public List<ItemStack> findCatalysts(Predicate<Item> predicate) {
        return catalysts.slots().stream()
                .map(InventorySlot::getStack)
                .filter(stack -> predicate.test(stack.getItem()))
                .toList();
    }

    public int getProductionBonus() {
        return upgrades.findTyped(MinerUpgradeType.PRODUCTION) * 25 >= random.nextInt(100) ? 1 : 0;
    }

    @SuppressWarnings("ConstantConditions")
    public List<ItemStack> pollResult(int amount) {
        final var possible = getLevel().registryAccess().registryOrThrow(ResultSet.RESULTS_REGISTRY)
                .stream().filter(it -> it.minerType().equals(minerType.name()))
                .flatMap(it -> it.get().stream())
                .filter(it -> it.predicate().canProduce(this))
                .map(it -> it.weighted(this))
                .filter(it -> it.weight().asInt() != 0)
                .toList();

        if (!possible.isEmpty()) {
            final int totalWeight = WeightedRandom.getTotalWeight(possible);
            final List<ItemStack> polled = new ArrayList<>();
            for (int i = 0; i < amount; i++) {
                WeightedRandom.getRandomItem(random, possible, totalWeight)
                            .ifPresent(it -> polled.add(it.get()));
            }

            final int fortuneLevel = upgrades.findTyped(MinerUpgradeType.FORTUNE);
            if (fortuneLevel > 0) {
                return fortune(polled, fortuneLevel);
            }

            return polled;
        }

        return List.of();
    }

    @SuppressWarnings({"deprecation", "ConstantConditions"})
    public List<ItemStack> fortune(List<ItemStack> input, int level) {
        final List<ItemStack> out = new ArrayList<>();
        input.forEach(stack -> {
            if (stack.getItem() instanceof BlockItem blockItem) {
                final var block = blockItem.getBlock();
                final var tool = Items.DIAMOND_PICKAXE.getDefaultInstance();
                tool.enchant(Enchantments.BLOCK_FORTUNE, level);
                out.addAll(block.getDrops(block.defaultBlockState(), new LootContext.Builder((ServerLevel) getLevel())
                        .withRandom(getLevel().random)
                        .withParameter(LootContextParams.TOOL, tool)
                        .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(worldPosition))));
            } else {
                out.add(stack);
            }
        });
        return out;
    }

    public void dropContents(IItemHandler handler) {
        if (level == null)
            return;
        for (int i = 0; i < handler.getSlots(); i++) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), handler.getStackInSlot(i));
        }
    }
}
