package com.matyrobbrt.simpleminers.data.base.result;

import com.matyrobbrt.simpleminers.results.ResultSet;
import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
public interface ResultConsumer {
    void accept(ResourceLocation id, ResultSet set);
}
