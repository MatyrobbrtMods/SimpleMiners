package com.matyrobbrt.simpleminers.menu;

import com.matyrobbrt.simpleminers.miner.upgrade.MinerUpgradeType;
import com.matyrobbrt.simpleminers.miner.upgrade.UpgradeHolder;

import java.util.Map;

public final class UpgradesDataSlot {
    private final UpgradeHolder holder;

    public UpgradesDataSlot(UpgradeHolder holder) {
        this.holder = holder;
    }

    private Map<MinerUpgradeType, Integer> previous;

    public boolean checkAndClearUpdateFlag() {
        final var current = holder.getUpgrades();
        final boolean flag = !current.equals(this.previous);
        this.previous = Map.copyOf(current);
        return flag;
    }
}
