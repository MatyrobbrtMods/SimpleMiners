package com.matyrobbrt.simpleminers.miner;

import com.matyrobbrt.simpleminers.miner.upgrade.MinerUpgradeType;
import com.matyrobbrt.simpleminers.miner.upgrade.UpgradeConfiguration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public record MinerType(String name, EnergyInfo energy, int ticksPerOperation, int rollsPerOperation, Map<MinerUpgradeType, UpgradeConfiguration> upgrades, BlockBehaviour.Properties blockProperties, Item.Properties itemProperties, ModelData modelData, @Nullable String translation) {

    public static final Map<String, MinerBlock> BLOCKS = new HashMap<>();

    public MinerBlock block() {
        return BLOCKS.get(name);
    }

    public record EnergyInfo(boolean enabled, int capacity, int ioRate, int usagePerTick) {
        public EnergyInfo(int usagePerTick) {
            this(true, usagePerTick * 100, usagePerTick * 10, usagePerTick);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MinerType type && type.name.equals(this.name);
    }

    public record ModelData(ResourceLocation overlay) {}
}
