package com.matyrobbrt.simpleminers.data;

import com.matyrobbrt.simpleminers.SimpleMiners;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SimpleMiners.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class DataProvider {
    @SubscribeEvent
    static void gather(final GatherDataEvent event) {
        if (Boolean.getBoolean("simpleminers.enableDPGeneration")) return;

        final var gen = event.getGenerator();

        gen.addProvider(event.includeClient(), new LangProvider(gen));
        gen.addProvider(event.includeClient(), new AssetsProvider(gen, event.getExistingFileHelper()));

        gen.addProvider(event.includeServer(), new RecipeProvider(gen));
    }
}
