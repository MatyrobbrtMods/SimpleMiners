package com.matyrobbrt.simpleminers.client;

import com.matyrobbrt.simpleminers.Registration;
import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.menu.MinerMenu;
import com.matyrobbrt.simpleminers.miner.MinerType;
import com.matyrobbrt.simpleminers.util.JsonBuilder;
import com.matyrobbrt.simpleminers.util.pack.BuiltInPacksRepository;
import com.matyrobbrt.simpleminers.util.pack.SimpleMinersRepositorySource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.function.Function;

public class SimpleMinersClient {
    public SimpleMinersClient(IEventBus modBus) {
        modBus.addListener((final FMLClientSetupEvent event) -> event.enqueueWork(() -> {
            //noinspection RedundantTypeArguments
            MenuScreens.<MinerMenu, MinerScreen>register(Registration.MINER_MENU.get(), (menu, inv, t) -> new MinerScreen(menu, inv));

            SimpleMiners.miners.forEach(SimpleMinersClient::registerModels);

            SimpleMiners.catalysts.forEach((item, catalystData) -> {
                if (catalystData.translation() != null) {
                    DynamicAssetsRP.INSTANCE.getLang("en_us").put(item.getDescriptionId(), catalystData.translation());
                }
            });
        }));

        modBus.addListener((final AddPackFindersEvent event) -> {
            if (event.getPackType() != PackType.CLIENT_RESOURCES) return;

            event.addRepositorySource(BuiltInPacksRepository.instance);
            event.addRepositorySource(SimpleMinersRepositorySource.INSTANCE);

            event.addRepositorySource((pInfoConsumer, pInfoFactory) -> pInfoConsumer.accept(Pack.create(
                    "simpleminers_dynamic_assets",
                    true, () -> DynamicAssetsRP.INSTANCE,
                    pInfoFactory, Pack.Position.TOP, PackSource.BUILT_IN
            )));
        });
    }

    public static void registerModels(MinerType minerType) {
        if (minerType.modelData() == null) return;

        DynamicAssetsRP.INSTANCE.addAnimatedWithOverlay(new ResourceLocation(SimpleMiners.MOD_ID, "textures/block/drill.png"),
                minerType.modelData().overlay(), "assets/simpleminers/textures/block/%s_miner.png".formatted(minerType.name()), 4);
        DynamicAssetsRP.INSTANCE.add("assets/simpleminers/textures/block/%s_miner.png.mcmeta".formatted(minerType.name()), new JsonBuilder()
                .add("animation", new JsonBuilder()
                        .add("height", 16)
                        .add("frametime", 1)
                        .addArray("frames", 0, 1, 2, 3, 2, 1)));

        DynamicAssetsRP.INSTANCE.addWithOverlay(new ResourceLocation(SimpleMiners.MOD_ID, "textures/block/drill_down.png"),
                minerType.modelData().overlay(), "assets/simpleminers/textures/block/%s_miner_down.png".formatted(minerType.name()));
        DynamicAssetsRP.INSTANCE.addWithOverlay(new ResourceLocation(SimpleMiners.MOD_ID, "textures/block/drill_top.png"),
                minerType.modelData().overlay(), "assets/simpleminers/textures/block/%s_miner_top.png".formatted(minerType.name()));

        final Function<String, String> textureGetter = b -> "simpleminers:block/%s_miner%s"
                .formatted(minerType.name(), b);
        DynamicAssetsRP.INSTANCE.add("assets/simpleminers/models/block/%s_miner.json".formatted(minerType.name()), new JsonBuilder()
                .add("parent", "minecraft:block/orientable")
                .add("textures", new JsonBuilder()
                        .add("side", textureGetter.apply(""))
                        .add("front", textureGetter.apply(""))
                        .add("top", textureGetter.apply("_top"))
                        .add("bottom", textureGetter.apply("_down"))));

        DynamicAssetsRP.INSTANCE.add("assets/simpleminers/blockstates/%s_miner.json".formatted(minerType.name()), new JsonBuilder()
                .add("variants", new JsonBuilder()
                        .add("", new JsonBuilder()
                                .add("model", "simpleminers:block/" + minerType.name() + "_miner"))));

        DynamicAssetsRP.INSTANCE.add("assets/simpleminers/models/item/%s_miner.json".formatted(minerType.name()), new JsonBuilder()
                .add("parent", "simpleminers:block/" + minerType.name() + "_miner"));

        if (minerType.translation() != null) {
            DynamicAssetsRP.INSTANCE.getLang("en_us").put(minerType.block().getDescriptionId(), minerType.translation());
        }
    }

    public static RegistryAccess registryAccess() {
        return Minecraft.getInstance().level.registryAccess();
    }
}
