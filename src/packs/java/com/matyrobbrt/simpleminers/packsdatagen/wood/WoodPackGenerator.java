package com.matyrobbrt.simpleminers.packsdatagen.wood;

import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.data.base.TagProviderBuilder;
import com.matyrobbrt.simpleminers.packsdatagen.RegisterPack;
import com.matyrobbrt.simpleminers.packsdatagen.SimplePackGenerator;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.ParametersAreNonnullByDefault;

@RegisterPack("wood")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WoodPackGenerator extends SimplePackGenerator {
    static final TagKey<Item> LEAF_CATALYSTS = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(SimpleMiners.MOD_ID, "catalysts/leaf"));

    @Override
    public void gather(DataGenerator generator, ExistingFileHelper existingFileHelper, SideProvider sides) {
        super.gather(generator, existingFileHelper, sides);

        generator.addProvider(true, new WoodMinerProvider(generator));

        generator.addProvider(sides.includeServer(), new WoodMinerRecipes(generator));
        generator.addProvider(sides.includeServer(), new WoodMinerResults(generator));
    }

    @Override
    protected void addItemTags(TagProviderBuilder<Item> provider) {
        provider.tag(LEAF_CATALYSTS, it -> it.addOptional(new ResourceLocation(SimpleMiners.MOD_ID, "leaf_catalyst")));
    }

    @Override
    protected void addItemModels(ItemModelProvider provider) {
        provider.basicItem(new ResourceLocation(SimpleMiners.MOD_ID, "leaf_catalyst"));
    }
}
