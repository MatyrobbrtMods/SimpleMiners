package com.matyrobbrt.simpleminers.data.base;

import com.matyrobbrt.simpleminers.results.ResultSet;
import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
public interface ResultConsumer {
    void accept(ResourceLocation id, ResultSet set);
}
