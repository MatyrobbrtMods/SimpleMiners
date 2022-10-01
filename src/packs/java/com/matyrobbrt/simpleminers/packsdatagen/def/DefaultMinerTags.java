package com.matyrobbrt.simpleminers.packsdatagen.def;

import com.matyrobbrt.simpleminers.SimpleMiners;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import static com.matyrobbrt.simpleminers.packsdatagen.def.DefaultMinerResults.mod;

@SuppressWarnings("deprecation")
public class DefaultMinerTags extends TagsProvider<Item> {
    static final TagKey<Item> MEK_CATALYSTS = TagKey.create(Registry.ITEM_REGISTRY, mod("catalysts/mekanism"));
    static final TagKey<Item> GEM_CATALYSTS = TagKey.create(Registry.ITEM_REGISTRY, mod("catalysts/gem"));

    public DefaultMinerTags(DataGenerator pGenerator, @Nullable ExistingFileHelper existingFileHelper) {
        super(pGenerator, Registry.ITEM, SimpleMiners.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        tag(MEK_CATALYSTS).addOptional(mod("mekanism_catalyst"));
        tag(GEM_CATALYSTS).addOptional(mod("gem_catalyst"));
    }
}
