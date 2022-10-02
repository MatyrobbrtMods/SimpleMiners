package com.matyrobbrt.simpleminers.packsdatagen.compat;

import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.register.IEItems;
import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.data.base.MinerResultProvider;
import com.matyrobbrt.simpleminers.data.base.result.ResultConsumer;
import com.matyrobbrt.simpleminers.data.base.result.ResultRecipeBuilder;
import com.matyrobbrt.simpleminers.packsdatagen.PackGenerator;
import com.matyrobbrt.simpleminers.packsdatagen.RegisterPack;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@RegisterPack("immersiveengineering")
public class ImmersiveEngineeringGenerator implements PackGenerator {
    @Override
    public void gather(DataGenerator generator, ExistingFileHelper existingFileHelper, SideProvider sides) {
        generator.addProvider(sides.includeServer(), new MinerResultProvider(generator) {
            @Override
            protected void gather(ResultConsumer consumer) {
                ResultRecipeBuilder.builder("ore", "immersiveengineering")
                        .add(3, ore(EnumMetals.URANIUM))
                        .add(4, ore(EnumMetals.LEAD), ore(EnumMetals.ALUMINUM), ore(EnumMetals.NICKEL), ore(EnumMetals.SILVER))
                        .save(consumer, new ResourceLocation(SimpleMiners.MOD_ID, "compat/immersiveengineering"));
            }
        });
    }

    private static Item ore(EnumMetals metal) {
        return IEBlocks.Metals.ORES.get(metal).asItem();
    }
}
