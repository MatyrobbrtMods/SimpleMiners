package com.matyrobbrt.simpleminers.packsdatagen;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;

public interface PackGenerator {
    void gather(DataGenerator generator, ExistingFileHelper existingFileHelper, SideProvider sides);

    interface SideProvider {
        boolean includeClient();
        boolean includeServer();
    }

    @Nullable
    default String packId() {
        return null;
    }
}
