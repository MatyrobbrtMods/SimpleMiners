package com.matyrobbrt.simpleminers.packsdatagen.def;

import com.matyrobbrt.simpleminers.packsdatagen.PackGenerator;
import com.matyrobbrt.simpleminers.packsdatagen.RegisterPack;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.RegistryOps;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterPack("default")
public class DefaultPackGenerator implements PackGenerator {
    @Override
    public void gather(DataGenerator gen, ExistingFileHelper helper, SideProvider sides) {
        gen.addProvider(sides.includeServer(), new DefaultMinerRecipes(gen));
        gen.addProvider(sides.includeServer(), new DefaultMinerResults(gen, RegistryOps.create(
                JsonOps.INSTANCE, RegistryAccess.builtinCopy()
        )));
        gen.addProvider(sides.includeServer(), new DefaultMinerTags(gen, helper));

        gen.addProvider(sides.includeClient(), new DefaultPackAssets(gen, helper));
    }

    @SubscribeEvent
    static void onEvent(final GatherDataEvent event) {
        if (!Boolean.getBoolean("simpleminers.enableDPGeneration")) return;

        final var gen = event.getGenerator();


    }
}
