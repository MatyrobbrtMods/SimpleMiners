package com.matyrobbrt.simpleminers.data.base;

import com.matyrobbrt.simpleminers.SimpleMiners;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public class TagProviderBuilder<T> extends TagsProvider<T> {
    private final List<Pair<TagKey<T>, Consumer<TagAppender<T>>>> consumers = new ArrayList<>();

    public TagProviderBuilder(DataGenerator pGenerator, Registry<T> pRegistry, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(pGenerator, pRegistry, modId, existingFileHelper);
    }

    public static <T> TagProviderBuilder<T> builder(DataGenerator generator, Registry<T> registry, String modId, ExistingFileHelper existingFileHelper) {
        return new TagProviderBuilder<>(generator, registry, modId, existingFileHelper);
    }
    public static <T> TagProviderBuilder<T> builder(DataGenerator generator, ResourceKey<Registry<T>> registry, String modId, ExistingFileHelper existingFileHelper) {
        return builder(generator, (Registry<T>) Registry.REGISTRY.get(registry.location()), modId, existingFileHelper);
    }
    public static <T> TagProviderBuilder<T> builder(DataGenerator generator, ResourceKey<Registry<T>> registry, ExistingFileHelper existingFileHelper) {
        return builder(generator, registry, SimpleMiners.MOD_ID, existingFileHelper);
    }

    public TagProviderBuilder<T> tag(TagKey<T> key, Consumer<TagAppender<T>> consumer) {
        consumers.add(Pair.of(key, consumer));
        return this;
    }
    public TagProviderBuilder<T> tag(ResourceLocation tag, Consumer<TagAppender<T>> consumer) {
        return tag(TagKey.create(registry.key(), tag), consumer);
    }

    @Override
    protected void addTags() {
        consumers.forEach(tg -> tg.getSecond().accept(super.tag(tg.getFirst())));
    }
}
