package com.matyrobbrt.simpleminers.packsdatagen.ore;

import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.data.base.TagProviderBuilder;
import com.matyrobbrt.simpleminers.packsdatagen.RegisterPack;
import com.matyrobbrt.simpleminers.packsdatagen.simple.SimplePackGenerator;
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

import javax.annotation.ParametersAreNonnullByDefault;

import static com.matyrobbrt.simpleminers.packsdatagen.ore.OreMinerResults.mod;

@RegisterPack("ore")
@ParametersAreNonnullByDefault
public class OrePackGenerator extends SimplePackGenerator {
    public static final TagKey<Item> GEM_CATALYSTS = TagKey.create(Registry.ITEM_REGISTRY, mod("catalysts/gem"));

    @Override
    public void gather(DataGenerator gen, ExistingFileHelper helper, SideProvider sides) {
        super.gather(gen, helper, sides);
        gen.addProvider(true, new OreMinerProvider(gen));

        gen.addProvider(sides.includeServer(), new OreMinerRecipes(gen));
        gen.addProvider(sides.includeServer(), new OreMinerResults(gen, RegistryOps.create(
                JsonOps.INSTANCE, RegistryAccess.builtinCopy()
        )));
    }

    @Override
    protected void addItemTags(TagProviderBuilder<Item> provider) {
        provider.tag(OrePackGenerator.GEM_CATALYSTS, it -> it.addOptional(mod("gem_catalyst")));
    }

    @Override
    protected void addItemModels(ItemModelProvider provider) {
        provider.basicItem(new ResourceLocation(SimpleMiners.MOD_ID, "gem_catalyst"));
    }
}
