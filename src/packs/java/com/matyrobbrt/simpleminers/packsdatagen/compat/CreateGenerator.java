package com.matyrobbrt.simpleminers.packsdatagen.compat;

import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.data.base.MinerResultProvider;
import com.matyrobbrt.simpleminers.data.base.result.ResultConsumer;
import com.matyrobbrt.simpleminers.data.base.result.ResultRecipeBuilder;
import com.matyrobbrt.simpleminers.packsdatagen.PackGenerator;
import com.matyrobbrt.simpleminers.packsdatagen.RegisterPack;
import com.matyrobbrt.simpleminers.results.predicate.InDimensionPredicate;
import com.mojang.serialization.JsonOps;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.palettes.AllPaletteStoneTypes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

@RegisterPack("create")
public class CreateGenerator implements PackGenerator {
    @Override
    public void gather(DataGenerator generator, ExistingFileHelper existingFileHelper, SideProvider sides) {
        generator.addProvider(sides.includeServer(), new MinerResultProvider(generator, RegistryOps.create(
                JsonOps.INSTANCE, RegistryAccess.builtinCopy()
        )) {
            @Override
            protected void gather(@NotNull ResultConsumer consumer) {
                ResultRecipeBuilder.builder("ore", "create")
                        .add(6, new InDimensionPredicate(Level.OVERWORLD), AllBlocks.ZINC_ORE.get())
                        .save(consumer, new ResourceLocation(SimpleMiners.MOD_ID, "compat/create_ores"));

                ResultRecipeBuilder.builder("stone", "create")
                        .addCopying(null, new InDimensionPredicate(Level.OVERWORLD), builder -> builder
                                .add(6, AllPaletteStoneTypes.SCORIA.baseBlock.get(), AllPaletteStoneTypes.LIMESTONE.baseBlock.get()))
                        .save(consumer, new ResourceLocation(SimpleMiners.MOD_ID, "compat/create_stones"));
            }
        });
    }
}
