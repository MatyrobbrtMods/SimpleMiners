package com.matyrobbrt.simpleminers.packsdatagen.def;

import com.matyrobbrt.simpleminers.SimpleMiners;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class DefaultPackAssets extends ItemModelProvider {
    public DefaultPackAssets(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, SimpleMiners.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(new ResourceLocation(SimpleMiners.MOD_ID, "mekanism_catalyst"));
        basicItem(new ResourceLocation(SimpleMiners.MOD_ID, "gem_catalyst"));
    }
}
