package com.matyrobbrt.simpleminers.packsdatagen.compat;

import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.data.base.MinerResultProvider;
import com.matyrobbrt.simpleminers.data.base.result.ResultConsumer;
import com.matyrobbrt.simpleminers.data.base.result.ResultRecipeBuilder;
import com.matyrobbrt.simpleminers.packsdatagen.PackGenerator;
import com.matyrobbrt.simpleminers.packsdatagen.RegisterPack;
import com.matyrobbrt.simpleminers.results.predicate.InDimensionPredicate;
import it.zerono.mods.extremereactors.gamecontent.Content;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

@RegisterPack("bigreactors")
public class BigReactorsGenerator implements PackGenerator {
    @Override
    public void gather(DataGenerator generator, ExistingFileHelper existingFileHelper, SideProvider sides) {
        generator.addProvider(sides.includeServer(), new MinerResultProvider(generator) {
            @Override
            protected void gather(@NotNull ResultConsumer consumer) {
                ResultRecipeBuilder.builder("ore", "bigreactors")
                        .add(3, new InDimensionPredicate(Level.END), Content.Items.ANGLESITE_ORE_BLOCK.get())
                        .add(3, new InDimensionPredicate(Level.NETHER), Content.Items.BENITOITE_ORE_BLOCK.get())
                        .add(3, new InDimensionPredicate(Level.OVERWORLD), Content.Items.YELLORITE_ORE_BLOCK.get())
                        .save(consumer, new ResourceLocation(SimpleMiners.MOD_ID, "compat/bigreactors_ores"));
            }
        });
    }
}
