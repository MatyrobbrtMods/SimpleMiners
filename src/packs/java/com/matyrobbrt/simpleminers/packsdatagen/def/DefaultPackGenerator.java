package com.matyrobbrt.simpleminers.packsdatagen.def;

import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.data.base.TagProviderBuilder;
import com.matyrobbrt.simpleminers.packsdatagen.PackGenerator;
import com.matyrobbrt.simpleminers.packsdatagen.RegisterPack;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import static com.matyrobbrt.simpleminers.packsdatagen.def.DefaultMinerResults.mod;

@RegisterPack("default")
public class DefaultPackGenerator implements PackGenerator {
    public static final TagKey<Item> GEM_CATALYSTS = TagKey.create(Registry.ITEM_REGISTRY, mod("catalysts/gem"));

    @SuppressWarnings("deprecation")
    @Override
    public void gather(DataGenerator gen, ExistingFileHelper helper, SideProvider sides) {
        gen.addProvider(true, new DefaultMinersProvider(gen));

        gen.addProvider(sides.includeClient(), new ItemModelProvider(gen, SimpleMiners.MOD_ID, helper) {
            @Override
            protected void registerModels() {
                basicItem(new ResourceLocation(SimpleMiners.MOD_ID, "gem_catalyst"));
            }
        });

        gen.addProvider(sides.includeServer(), new DefaultMinerRecipes(gen));
        gen.addProvider(sides.includeServer(), new DefaultMinerResults(gen, RegistryOps.create(
                JsonOps.INSTANCE, RegistryAccess.builtinCopy()
        )));
        gen.addProvider(sides.includeServer(), TagProviderBuilder.builder(gen, Registry.ITEM_REGISTRY, helper)
                .tag(DefaultPackGenerator.GEM_CATALYSTS, it -> it.addOptional(mod("gem_catalyst"))));
    }

}
