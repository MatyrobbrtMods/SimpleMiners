package com.matyrobbrt.simpleminers.miner.upgrade;

import com.matyrobbrt.simplegui.util.Color;
import com.matyrobbrt.simpleminers.Registration;
import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.item.MinerUpgrade;
import com.matyrobbrt.simpleminers.miner.MinerBE;
import com.matyrobbrt.simpleminers.miner.MinerType;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public interface MinerUpgradeType {
    ResourceKey<Registry<MinerUpgradeType>> UPGRADE_TYPE_REGISTRY = SimpleMiners.registryKey("miner_upgrade_type");
    Registry<MinerUpgradeType> UPGRADE_TYPES = SimpleMiners.registry(UPGRADE_TYPE_REGISTRY, null);

    MinerUpgradeType SPEED = register(new ResourceLocation("speed"), () -> Registration.SPEED_UPGRADE.get().getDefaultInstance(), 8, 0xFF2608);
    MinerUpgradeType ENERGY = register(new ResourceLocation("energy"), () -> Registration.ENERGY_UPGRADE.get().getDefaultInstance(), 8, 0x38FF4F);
    MinerUpgradeType PRODUCTION = register(new ResourceLocation("production"), Registration.PRODUCTION_UPGRADE, 4, 0x891A89);
    MinerUpgradeType FORTUNE = register(new ResourceLocation("fortune"), Registration.FORTUNE_UPGRADE, 3, 0x00FFFF);

    ItemStack createStack();
    int getMaxAmount();
    Color getColor();

    default ItemStack modifyOutput(MinerBE miner, ItemStack original) {
        return original;
    }

    Component getName();
    Component getDescription();

    static <T extends Item & MinerUpgrade> MinerUpgradeType register(ResourceLocation name, RegistryObject<T> stack, int max, int color) {
        return register(name, () -> stack.get().getDefaultInstance(), max, color);
    }

    static MinerUpgradeType register(ResourceLocation name, Supplier<ItemStack> stack, int max, int color) {
        final Color clr = Color.rgb(color);
        return Registry.register(UPGRADE_TYPES, name, new MinerUpgradeType() {
            @Override
            public ItemStack createStack() {
                return stack.get();
            }

            @Override
            public int getMaxAmount() {
                return max;
            }

            @Override
            public Component getName() {
                return Component.translatable("miner_upgrade." + name.getNamespace() + "." + name.getPath() + ".name");
            }

            @Override
            public Component getDescription() {
                return Component.translatable("miner_upgrade." + name.getNamespace() + "." + name.getPath() + ".description");
            }

            @Override
            public Color getColor() {
                return clr;
            }
        });
    }

    default int getInt(String key, MinerType minerType, int fallback) {
        return UpgradeConfiguration.get(this, minerType.name()).getInt(key, fallback);
    }

    default boolean isEnabled(MinerType minerType) {
        return UpgradeConfiguration.get(this, minerType.name()).getBoolean("enabled", true);
    }
}
