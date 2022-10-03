package com.matyrobbrt.simpleminers.packsdatagen.wood;

import com.matyrobbrt.simpleminers.data.base.MinerTypeBuilder;
import com.matyrobbrt.simpleminers.miner.MinerType;
import com.matyrobbrt.simpleminers.miner.upgrade.MinerUpgradeType;
import com.matyrobbrt.simpleminers.packsdatagen.PackGenerator;
import com.matyrobbrt.simpleminers.packsdatagen.RegisterPack;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;

@RegisterPack("wood")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WoodGenerator implements PackGenerator {
    @Override
    public void gather(DataGenerator generator, ExistingFileHelper existingFileHelper, SideProvider sides) {
        generator.addProvider(true, new WoodMinerResults(generator));

        generator.addProvider(sides.includeServer(), new WoodMinerRecipes(generator));
        generator.addProvider(sides.includeServer(), new DataProvider() {
            @Override
            public void run(CachedOutput pOutput) throws IOException {
                MinerTypeBuilder.builder()
                        .translation("Wood Miner")
                        .rollsPerOperation(1)
                        .ticksPerOperation(400)
                        .energy(new MinerType.EnergyInfo(80))
                        .upgrades(upgradeBuilder -> upgradeBuilder
                                .add(MinerUpgradeType.SPEED, builder -> builder
                                        .add("timeDecrease", 20)
                                        .add("energyUsage", 60))
                                .add(MinerUpgradeType.ENERGY, builder -> builder
                                        .add("usageDecrease", 20)))
                        .model(new MinerType.ModelData(new ResourceLocation("textures/block/oak_log.png")))
                        .save("wood", generator, pOutput);
            }

            @Override
            public String getName() {
                return "Wood Miner";
            }
        });
    }
}
