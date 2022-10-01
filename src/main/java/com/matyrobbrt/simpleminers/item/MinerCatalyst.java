package com.matyrobbrt.simpleminers.item;

import net.minecraft.world.item.Item;

public interface MinerCatalyst {
    class Impl extends Item implements MinerCatalyst {
        public Impl(Properties pProperties) {
            super(pProperties);
        }
    }
}
