package com.matyrobbrt.simpleminers.packsdatagen;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class DatagenCheating {
    private static final Cheater<Item> ITEMS = new Cheater<>(ForgeRegistries.ITEMS, () -> new Item(new Item.Properties()));

    public static Item item(String modId, String path) {
        return item(new ResourceLocation(modId, path));
    }

    public static Item item(ResourceLocation location) {
        return ITEMS.cheat(location);
    }

    public record Cheater<T>(ForgeRegistry<T> registry, List<ResourceLocation> registered, Supplier<T> factory) {
        public Cheater(IForgeRegistry<T> registry, Supplier<T> factory) {
            this((ForgeRegistry<T>) registry, new ArrayList<>(), factory);
        }

        @SuppressWarnings("ConstantConditions")
        public T cheat(ResourceLocation location) {
            if (!registered.contains(location)) {
                registry.unfreeze();
                registry.register(location, factory.get());
            }
            return registry.getValue(location);
        }
    }
}
