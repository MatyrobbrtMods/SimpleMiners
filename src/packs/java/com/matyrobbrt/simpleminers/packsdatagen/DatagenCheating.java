package com.matyrobbrt.simpleminers.packsdatagen;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class DatagenCheating {
    private static final Cheater<Item> ITEMS = new Cheater<>(ForgeRegistries.ITEMS, () -> new Item(new Item.Properties()));
    private static final Cheater<Block> BLOCKS = new Cheater<>(ForgeRegistries.BLOCKS, () -> new Block(BlockBehaviour.Properties.of(Material.HEAVY_METAL)));

    public static Item item(String modId, String path) {
        return item(new ResourceLocation(modId, path));
    }

    public static Item item(ResourceLocation location) {
        return ITEMS.cheat(location);
    }

    public static Block block(ResourceLocation location) {
        final var block = BLOCKS.cheat(location);
        Item.BY_BLOCK.put(block, ITEMS.cheat(location, () -> new BlockItem(block, new Item.Properties())));
        return block;
    }

    public record Cheater<T>(ForgeRegistry<T> registry, Set<ResourceLocation> registered, Supplier<T> factory) {
        public Cheater(IForgeRegistry<T> registry, Supplier<T> factory) {
            this((ForgeRegistry<T>) registry, new HashSet<>(), factory);
        }

        @SuppressWarnings("ConstantConditions")
        public T cheat(ResourceLocation location, Supplier<T> factory) {
            if (!registered.contains(location)) {
                registry.unfreeze();
                registry.register(location, factory.get());
                registered.add(location);
            }
            return registry.getValue(location);
        }
        public T cheat(ResourceLocation location) {
            return cheat(location, factory);
        }
    }
}
