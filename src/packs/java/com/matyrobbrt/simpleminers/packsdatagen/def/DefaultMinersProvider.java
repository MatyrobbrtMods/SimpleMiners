package com.matyrobbrt.simpleminers.packsdatagen.def;

import com.matyrobbrt.simpleminers.data.base.CatalystBuilder;
import com.matyrobbrt.simpleminers.data.base.MinerTypeBuilder;
import com.matyrobbrt.simpleminers.miner.MinerType;
import com.matyrobbrt.simpleminers.miner.upgrade.MinerUpgradeType;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public record DefaultMinersProvider(DataGenerator generator) implements DataProvider {
    @Override
    public void run(CachedOutput pOutput) throws IOException {
        MinerTypeBuilder.builder()
                .translation("Ore Miner")
                .rollsPerOperation(1)
                .ticksPerOperation(400)
                .energy(new MinerType.EnergyInfo(100))
                .upgrades(upgradeBuilder -> upgradeBuilder
                        .add(MinerUpgradeType.SPEED, builder -> builder
                                .add("timeDecrease", 30)
                                .add("energyUsage", 50))
                        .add(MinerUpgradeType.ENERGY, builder -> builder
                                .add("usageDecrease", 25)))
                .model(new MinerType.ModelData(new ResourceLocation("textures/block/iron_ore.png")))
                .save("ore", generator, pOutput);

        CatalystBuilder.builder()
                .add("gem_catalyst", builder -> builder
                        .rarity(Rarity.EPIC)
                        .translation("Gem Catalyst"))
                .add("mekanism_catalyst", builder -> builder
                        .rarity(Rarity.RARE)
                        .translation("Mekanism Catalyst"))
                .save(generator, pOutput);
    }

    @Override
    public String getName() {
        return "Default Miners";
    }
}
