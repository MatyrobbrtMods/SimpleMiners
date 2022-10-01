package com.matyrobbrt.simpleminers.miner.upgrade;

import com.matyrobbrt.simplegui.util.Action;
import com.matyrobbrt.simplegui.util.InteractionType;
import com.matyrobbrt.simpleminers.menu.VirtualInventorySlot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;

public interface UpgradeHolder extends INBTSerializable<CompoundTag> {
    int findTyped(MinerUpgradeType type);

    Map<MinerUpgradeType, Integer> getUpgrades();

    void removeUpgrade(MinerUpgradeType type, int amount);

    /**
     * @return inserted amount
     */
    int insertUpgrade(MinerUpgradeType type, int amount);

    void syncFrom(Map<MinerUpgradeType, Integer> upgradeMap);

    static UpgradeHolder make(VirtualInventorySlot upgradesOut) {
        return new Impl(upgradesOut);
    }
}

@SuppressWarnings("ConstantConditions")
class Impl implements UpgradeHolder {
    private final VirtualInventorySlot upgradesOut;
    Impl(VirtualInventorySlot upgradesOut) {
        this.upgradesOut = upgradesOut;
    }

    private final Map<MinerUpgradeType, Integer> upgrades = new HashMap<>();

    @Override
    public int findTyped(MinerUpgradeType type) {
        return upgrades.getOrDefault(type, 0);
    }

    @Override
    public Map<MinerUpgradeType, Integer> getUpgrades() {
        return upgrades;
    }

    @Override
    public void removeUpgrade(MinerUpgradeType type, int amount) {
        final int current = findTyped(type);
        amount = Math.min(amount, current);
        if (amount > 0) {
            final ItemStack upgradeStack = type.createStack();
            upgradeStack.setCount(amount);
            amount -= upgradesOut.insertItem(upgradeStack, Action.SIMULATE, InteractionType.INTERNAL).getCount();

            upgradeStack.setCount(amount);
            upgradesOut.insertItem(upgradeStack, Action.EXECUTE, InteractionType.INTERNAL);

            if (amount >= current) {
                upgrades.remove(type);
            } else {
                upgrades.put(type, current - amount);
            }
        }
    }

    @Override
    public int insertUpgrade(MinerUpgradeType type, int amount) {
        final int canStillInsert = type.getMaxAmount() - findTyped(type);
        final int toInsert = Math.min(amount, canStillInsert);
        if (toInsert > 0) {
            getUpgrades().put(type, findTyped(type) + toInsert);
        }
        return toInsert;
    }

    @Override
    public void syncFrom(Map<MinerUpgradeType, Integer> upgradeMap) {
        upgrades.clear();
        upgrades.putAll(upgradeMap);
    }

    @Override
    public CompoundTag serializeNBT() {
        final var tag = new CompoundTag();
        getUpgrades().forEach((upgrade, count) -> {
            if (count > 0) {
                tag.putInt(MinerUpgradeType.UPGRADE_TYPES.getKey(upgrade).toString(), count);
            }
        });
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        nbt.getAllKeys().forEach(key -> {
            final int amount = nbt.getInt(key);
            final MinerUpgradeType type = MinerUpgradeType.UPGRADE_TYPES.get(new ResourceLocation(key));
            if (type != null) {
                upgrades.put(type, amount);
            }
        });
    }
}
