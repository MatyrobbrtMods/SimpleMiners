package com.matyrobbrt.simpleminers;

import com.matyrobbrt.simpleminers.client.SimpleMinersClient;
import com.matyrobbrt.simpleminers.item.MinerCatalyst;
import com.matyrobbrt.simpleminers.miner.MinerBE;
import com.matyrobbrt.simpleminers.miner.MinerBlock;
import com.matyrobbrt.simpleminers.miner.MinerType;
import com.matyrobbrt.simpleminers.miner.upgrade.UpgradeConfiguration;
import com.matyrobbrt.simpleminers.network.SimpleMinersNetwork;
import com.matyrobbrt.simpleminers.network.SyncUpgradeConfigurations;
import com.matyrobbrt.simpleminers.results.ResultSet;
import com.matyrobbrt.simpleminers.results.modifier.ResultModifiers;
import com.matyrobbrt.simpleminers.results.predicate.ResultPredicates;
import com.matyrobbrt.simpleminers.util.JsonLoader;
import com.matyrobbrt.simpleminers.util.SimpleMinersRepositorySource;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryBuilder;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Mod(SimpleMiners.MOD_ID)
public class SimpleMiners {
    public static final String MOD_ID = "simpleminers";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Path BASE_PATH = FMLPaths.GAMEDIR.get().resolve(MOD_ID).toAbsolutePath();

    public static List<MinerType> miners;
    public static Map<Item, JsonLoader.CatalystData> catalysts;

    public SimpleMiners() {
        final var bus = FMLJavaModLoadingContext.get().getModEventBus();
        Registration.ITEMS.register(bus);
        Registration.BLOCKS.register(bus);
        Registration.MENU_TYPES.register(bus);
        Registration.RECIPE_TYPES.register(bus);
        Registration.BLOCK_ENTITY_TYPES.register(bus);
        Registration.RECIPE_SERIALIZERS.register(bus);

        ResultModifiers.clinit();
        ResultPredicates.clinit();

        bus.addListener((final FMLCommonSetupEvent event) -> SimpleMinersNetwork.register());

        MinecraftForge.EVENT_BUS.addListener((final PlayerEvent.PlayerLoggedInEvent event) -> SimpleMinersNetwork.CHANNEL.send(PacketDistributor.PLAYER
                        .with(() -> (ServerPlayer) event.getEntity()),
                new SyncUpgradeConfigurations(UpgradeConfiguration.Store.CONFIGURATIONS)));

        bus.addListener((final RegisterEvent event) -> event.register(Registry.BLOCK_REGISTRY, helper -> {
            try {
                miners = JsonLoader.loadMinersFromDir();
            } catch (Exception e) {
                LOGGER.error("Encountered exception reading miners: ", e);
                throw new RuntimeException(e);
            }

            miners.forEach(miner -> registerMiner(miner, helper));
        }));

        bus.addListener((final RegisterEvent event) -> event.register(Registry.ITEM_REGISTRY, helper -> {
            try {
                SimpleMiners.catalysts = new HashMap<>();
                final var catalysts = JsonLoader.loadCatalysts();
                catalysts.forEach((name, props) -> {
                    final var item = new MinerCatalyst.Impl(props.properties().tab(ITEM_TAB));
                    helper.register(new ResourceLocation(MOD_ID, name), item);
                    SimpleMiners.catalysts.put(item, props);
                });
            } catch (Exception e) {
                LOGGER.error("Encountered exception reading catalysts: ", e);
                throw new RuntimeException(e);
            }
        }));

        bus.addListener((final AddPackFindersEvent event) -> {
            if (event.getPackType() == PackType.SERVER_DATA) {
                event.addRepositorySource(SimpleMinersRepositorySource.INSTANCE);
            }
        });

        bus.addListener((final NewRegistryEvent event) -> ResultSet.REGISTRY.accept(
                event.create(new RegistryBuilder<ResultSet>()
                        .disableSaving()
                        .setName(ResultSet.RESULTS_REGISTRY.location())
                        .dataPackRegistry(ResultSet.REQUIRED_MOD_AWARE_CODEC, ResultSet.CODEC))
        ));

        if (FMLEnvironment.dist == Dist.CLIENT) {
            //noinspection InstantiationOfUtilityClass
            new SimpleMinersClient(bus);
        }
    }

    private static void registerMiner(MinerType type, RegisterEvent.RegisterHelper<Block> helper) {
        final Mutable<Supplier<BlockEntityType<?>>> mutable = new MutableObject<>();
        final MinerBlock miner = new MinerBlock(
                type.blockProperties(),
                type, () -> mutable.getValue().get()
        );
        MinerType.BLOCKS.put(type.name(), miner);

        final ResourceLocation name = new ResourceLocation(MOD_ID, type.name() + "_miner");
        helper.register(name, miner);

        Registration.ITEMS.register(name.getPath(), () -> new BlockItem(
                miner, new Item.Properties().tab(ITEM_TAB)
        ));

        //noinspection ConstantConditions
        mutable.setValue(Registration.BLOCK_ENTITY_TYPES.register(
                type.name() + "_miner",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new MinerBE(mutable.getValue().get(), pos, state, type),
                        miner
                ).build(null)
        ));

        type.upgrades().forEach((upgrade, conf) -> UpgradeConfiguration.Store.put(upgrade, type.name(), conf));
    }

    public static <T> ResourceKey<Registry<T>> registryKey(String location) {
        return ResourceKey.createRegistryKey(new ResourceLocation(MOD_ID, location));
    }

    @SuppressWarnings("unchecked")
    public static <T> Registry<T> registry(ResourceKey<? extends Registry<T>> key, @Nullable String defaultValue) {
        return (Registry<T>) Registry.<Registry<?>>register((Registry<? super Registry<?>>) Registry.REGISTRY, key.location().toString(),
                defaultValue == null ? new MappedRegistry<>(key, Lifecycle.experimental(), null)
                : new DefaultedRegistry<>(defaultValue, key, Lifecycle.experimental(), null));
    }

    public static final CreativeModeTab ITEM_TAB = new CreativeModeTab(CreativeModeTab.getGroupCountSafe(), SimpleMiners.MOD_ID) {
        @Override
        public @NotNull ItemStack makeIcon() {
            return getTabIcon().getDefaultInstance();
        }
    };

    public static Item getTabIcon() {
        return miners.isEmpty() ? Registration.ENERGY_UPGRADE.get() : miners.get(0).block().asItem();
    }
}
