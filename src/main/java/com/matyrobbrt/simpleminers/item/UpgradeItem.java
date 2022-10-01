package com.matyrobbrt.simpleminers.item;

import com.matyrobbrt.simpleminers.miner.upgrade.MinerUpgradeType;
import net.minecraft.world.item.Item;

public class UpgradeItem extends Item implements MinerUpgrade {
    private final MinerUpgradeType type;

    public UpgradeItem(Properties pProperties, MinerUpgradeType type) {
        super(pProperties);
        this.type = type;
    }

    @Override
    public MinerUpgradeType getType() {
        return type;
    }
}
