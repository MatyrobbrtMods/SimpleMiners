package com.matyrobbrt.simpleminers.data;

import com.matyrobbrt.simpleminers.Registration;
import com.matyrobbrt.simpleminers.SimpleMiners;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class AssetsProvider extends ItemModelProvider {
    public AssetsProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, SimpleMiners.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(Registration.SPEED_UPGRADE.get());
        basicItem(Registration.ENERGY_UPGRADE.get());
        basicItem(Registration.PRODUCTION_UPGRADE.get());
        basicItem(Registration.FORTUNE_UPGRADE.get());

        basicItem(Registration.UPGRADE_BASE.get());
        basicItem(Registration.CATALYST_BASE.get());
    }
}
