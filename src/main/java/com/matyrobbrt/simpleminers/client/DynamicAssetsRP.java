package com.matyrobbrt.simpleminers.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.util.JsonBuilder;
import com.mojang.blaze3d.platform.NativeImage;
import cpw.mods.modlauncher.api.LamdbaExceptionUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.ResourcePackFileNotFoundException;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DynamicAssetsRP extends AbstractPackResources {
    public static final DynamicAssetsRP INSTANCE = new DynamicAssetsRP();
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private final Map<String, IOSupplier> suppliers = new HashMap<>();

    private final byte[] packMcMeta;

    public DynamicAssetsRP() {
        super(new File("dummy"));

        packMcMeta = "{\"pack\":{\"description\":\"SimpleMiners Dynamic Assets\",\"pack_format\":%s}}"
                .formatted(PackType.CLIENT_RESOURCES.getVersion(SharedConstants.getCurrentVersion()))
                .getBytes(StandardCharsets.UTF_8);
        suppliers.put("pack.mcmeta", () -> new ByteArrayInputStream(packMcMeta));
    }

    @Override
    protected InputStream getResource(String pResourcePath) throws IOException {
        final IOSupplier sup = suppliers.get(pResourcePath);
        if (sup == null) {
            throw new ResourcePackFileNotFoundException(this.file, pResourcePath);
        }
        return sup.get();
    }

    @Override
    protected boolean hasResource(String pResourcePath) {
        return suppliers.containsKey(pResourcePath);
    }

    @Override
    public Collection<ResourceLocation> getResources(PackType pType, String pNamespace, String pPath, Predicate<ResourceLocation> pFilter) {
        if (!pNamespace.equals(SimpleMiners.MOD_ID) || pType != PackType.CLIENT_RESOURCES) return List.of();
        return suppliers.keySet()
                .stream()
                .map(it -> new ResourceLocation(SimpleMiners.MOD_ID, it))
                .filter(pFilter)
                .toList();
    }

    @Override
    public Set<String> getNamespaces(PackType pType) {
        return Set.of(SimpleMiners.MOD_ID);
    }

    @Override
    public void close() {
        suppliers.forEach((s, ioSupplier) -> ioSupplier.close());
    }

    public void add(String path, IOSupplier supplier) {
        this.suppliers.put(path, supplier);
    }

    public void add(String path, String text) {
        final byte[] bytes = text.trim().getBytes(StandardCharsets.UTF_8);
        add(path, () -> new ByteArrayInputStream(bytes));
    }

    public void add(String path, JsonBuilder json) {
        final byte[] bytes = GSON.toJson(json.delegate()).getBytes(StandardCharsets.UTF_8);
        add(path, () -> new ByteArrayInputStream(bytes));
    }

    public void addRecoloured(ResourceLocation from, String path, int fromColour, int toColour) {
        add(path, new IOSupplier.Lazy(() -> {
            try (final var is = Minecraft.getInstance().getResourceManager().open(from)) {
                final var image = NativeImage.read(is);
                TextureRecolouring.recolour(image, fromColour, toColour);
                final byte[] data = image.asByteArray();
                image.close();
                return () -> new ByteArrayInputStream(data);
            }
        }));
    }

    public void addAnimatedWithOverlay(ResourceLocation base, ResourceLocation source, String path, int divisions) {
        add(path, new IOSupplier.Lazy(() -> {
            try (final var baseIs = Minecraft.getInstance().getResourceManager().open(base);
                final var sourceIs = Minecraft.getInstance().getResourceManager().open(source)) {
                final var baseImage = NativeImage.read(baseIs);
                final var sourceImage = NativeImage.read(sourceIs);
                TextureRecolouring.overlayAnimated(baseImage, sourceImage, divisions);
                final byte[] data = baseImage.asByteArray();
                baseImage.close();
                sourceImage.close();
                return () -> new ByteArrayInputStream(data);
            }
        }));
    }
    public void addWithOverlay(ResourceLocation base, ResourceLocation source, String path) {
        add(path, new IOSupplier.Lazy(() -> {
            try (final var baseIs = Minecraft.getInstance().getResourceManager().open(base);
                final var sourceIs = Minecraft.getInstance().getResourceManager().open(source)) {
                final var baseImage = NativeImage.read(baseIs);
                final var sourceImage = NativeImage.read(sourceIs);
                TextureRecolouring.overlay(baseImage, sourceImage);
                final byte[] data = baseImage.asByteArray();
                baseImage.close();
                sourceImage.close();
                return () -> new ByteArrayInputStream(data);
            }
        }));
    }

    public void copy(ResourceLocation from, String path) {
        add(path, () -> Minecraft.getInstance().getResourceManager().open(from));
    }

    public Map<String, String> getLang(String languageKey) {
        return ((Lang) suppliers.computeIfAbsent("assets/simpleminers/lang/" + languageKey + ".json", k -> new Lang(new HashMap<>()))).map();
    }

    interface IOSupplier {
        InputStream get() throws IOException;
        default void close() {

        }

        class Lazy implements IOSupplier {
            private IOSupplier value;
            private final LamdbaExceptionUtils.Supplier_WithExceptions<IOSupplier, IOException> provider;

            public Lazy(LamdbaExceptionUtils.Supplier_WithExceptions<IOSupplier, IOException> value) {
                this.provider = value;
            }

            @Override
            public InputStream get() throws IOException {
                if (value == null) value = provider.get();
                return value.get();
            }
        }
    }

    record Lang(Map<String, String> map) implements IOSupplier {
        private static final Gson GSON = new Gson();

        @Override
        public InputStream get() throws IOException {
            return new ByteArrayInputStream(GSON.toJson(map).getBytes(StandardCharsets.UTF_8));
        }
    }
}
