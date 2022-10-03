package com.matyrobbrt.simpleminers.packsdatagen.compat;

import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.data.base.MinerResultProvider;
import com.matyrobbrt.simpleminers.data.base.result.ResultConsumer;
import com.matyrobbrt.simpleminers.data.base.result.ResultRecipeBuilder;
import com.matyrobbrt.simpleminers.packsdatagen.DatagenCheating;
import com.matyrobbrt.simpleminers.packsdatagen.PackGenerator;
import com.matyrobbrt.simpleminers.packsdatagen.RegisterPack;
import com.matyrobbrt.simpleminers.results.predicate.InDimensionPredicate;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.ParametersAreNonnullByDefault;

@RegisterPack("powah")
@ParametersAreNonnullByDefault
public class PowahGenerator implements PackGenerator {
    @Override
    public void gather(DataGenerator generator, ExistingFileHelper existingFileHelper, SideProvider sides) {
        generator.addProvider(sides.includeServer(), new MinerResultProvider(generator) {
            @Override
            protected void gather(ResultConsumer consumer) {
                ResultRecipeBuilder.builder("ore", "powah")
                        .addCopying(null, new InDimensionPredicate(Level.OVERWORLD), builder -> builder
                                .add(4, cheat("uraninite_ore_poor"))
                                .add(3, cheat("uraninite_ore"))
                                .add(2, cheat("uraninite_ore_dense")))
                        .save(consumer, new ResourceLocation(SimpleMiners.MOD_ID, "compat/powah"));
            }
        });
    }

    private static Item cheat(String name) {
        return DatagenCheating.item("powah", name);
    }
}
