package com.matyrobbrt.simpleminers.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.miner.MinerType;
import com.matyrobbrt.simpleminers.miner.upgrade.MinerUpgradeType;
import com.matyrobbrt.simpleminers.miner.upgrade.UpgradeConfiguration;
import com.matyrobbrt.simpleminers.util.pack.BuiltInPacksRepository;
import com.matyrobbrt.simpleminers.util.pack.SimpleMinersRepositorySource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.fml.ModList;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.StreamSupport;

public class JsonLoader {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setLenient().create();

    @Nullable
    public static MinerType read(String name, JsonObject json) {
        final int ticksPerOperation = GsonHelper.getAsInt(json, "ticksPerOperation", 10);
        final int rollsPerOperation = GsonHelper.getAsInt(json, "rollsPerOperation", 1);

        final MinerType.EnergyInfo energy;
        if (json.has("energy")) {
            final var energyJson = json.get("energy").getAsJsonObject();
            final int usage = GsonHelper.getAsInt(energyJson, "usagePerTick", 100);
            energy = new MinerType.EnergyInfo(
                    true, GsonHelper.getAsInt(energyJson, "capacity", usage * 100),
                    GsonHelper.getAsInt(energyJson, "ioRate", usage * 10), usage
            );
        } else {
            energy = new MinerType.EnergyInfo(false, 0, 0, 0);
        }

        final Map<MinerUpgradeType, UpgradeConfiguration> upgrades = new HashMap<>();

        if (json.has("upgrades")) {
            final var upgradesJson = json.getAsJsonObject("upgrades");
            upgradesJson.keySet().forEach(key -> {
                final MinerUpgradeType type = MinerUpgradeType.UPGRADE_TYPES.get(new ResourceLocation(key));
                if (type != null) {
                    upgrades.put(type, new UpgradeConfiguration.Impl(upgradesJson.getAsJsonObject(key)));
                }
            });
        }

        final Item.Properties properties;
        if (json.has("itemProperties")) {
            properties = readProps(json.getAsJsonObject("itemProperties"));
        } else {
            properties = new Item.Properties().tab(SimpleMiners.ITEM_TAB);
        }

        final BlockBehaviour.Properties bprops;
        if (!json.has("blockProperties")) {
            bprops = BlockBehaviour.Properties.of(Material.HEAVY_METAL);
        } else {
            bprops = readBlockProps(json.getAsJsonObject("blockProperties"));
        }

        if (json.has("requiredMods")) {
            if (StreamSupport.stream(GsonHelper.getAsJsonArray(json, "requiredMods").spliterator(), false)
                    .anyMatch(it -> !ModList.get().isLoaded(it.getAsString()))) {
                return null;
            }
        }

        final MinerType.ModelData modelData;
        if (json.has("modelData")) {
            final var modelJson = json.getAsJsonObject("modelData");
            modelData = new MinerType.ModelData(new ResourceLocation(modelJson.get("overlay").getAsString()));
        } else {
            modelData = null;
        }

        final String translation = GsonHelper.getAsString(json, "translation", null);

        return new MinerType(name, energy, ticksPerOperation, rollsPerOperation, upgrades, bprops, properties, modelData, translation);
    }

    public static Item.Properties readProps(JsonObject json) {
        final Item.Properties props = new Item.Properties();

        props.stacksTo(GsonHelper.getAsInt(json, "maxStackSize", 64));
        if (GsonHelper.getAsBoolean(json, "fireResistant", false)) props.fireResistant();

        if (json.has("rarity")) {
            final String rarity = json.get("rarity").getAsString();
            props.rarity(switch (rarity) {
                case "common" -> Rarity.COMMON;
                case "epic" -> Rarity.EPIC;
                case "rare" -> Rarity.RARE;
                case "uncommon" -> Rarity.UNCOMMON;
                default -> throw new IllegalArgumentException("Unknown rarity: " + rarity);
            });
        }

        return props.tab(SimpleMiners.ITEM_TAB);
    }

    public static BlockBehaviour.Properties readBlockProps(JsonObject json) {
        final BlockBehaviour.Properties props = BlockBehaviour.Properties.of(Material.HEAVY_METAL);
        props.destroyTime(GsonHelper.getAsFloat(json, "destroyTime", 0));
        props.explosionResistance(GsonHelper.getAsFloat(json, "explosionResistance", 0));

        if (!GsonHelper.getAsBoolean(json, "collision", true)) props.noCollission();
        if (!GsonHelper.getAsBoolean(json, "occlusion", true)) props.noOcclusion();

        return props;
    }

    public static List<MinerType> loadMinersFromDir() throws IOException, URISyntaxException {
        final Path path = SimpleMiners.BASE_PATH.resolve("miners");
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }

        final List<MinerType> types = new ArrayList<>();
        final Set<String> known = new HashSet<>();

        loadMinersFromDir(types, known, path);

        final List<SimpleMinersRepositorySource.PackEntry> entries = new ArrayList<>();
        entries.addAll(SimpleMinersRepositorySource.INSTANCE.findCandidates());
        entries.addAll(BuiltInPacksRepository.instance.findPacks());

        for (SimpleMinersRepositorySource.PackEntry packEntry : entries) {
            final var inZip = packEntry.pathGetter().get("miners");
            if (Files.exists(inZip)) {
                loadMinersFromDir(types, known, inZip);
            }
            packEntry.pathGetter().close();
        }

        return types;
    }

    private static void loadMinersFromDir(List<MinerType> types, Set<String> known, Path path) throws IOException {
        try (final var stream = Files.walk(path, 1)) {
            final var iterator = stream.iterator();
            while (iterator.hasNext()) {
                final var p = iterator.next();
                final String fileName = p.getFileName().toString();
                if (Files.isDirectory(p) || !FilenameUtils.getExtension(fileName).equals("json")) continue;
                try (final var reader = Files.newBufferedReader(p)) {
                    final JsonObject json = GSON.fromJson(reader, JsonObject.class);
                    final String name = FilenameUtils.removeExtension(fileName);
                    if (!known.contains(name)) {
                        types.add(read(name, json));
                        known.add(name);
                    }
                }
            }
        }
    }

    public static Map<String, CatalystData> loadCatalysts() throws IOException, URISyntaxException {
        final Path path = SimpleMiners.BASE_PATH.resolve("catalysts.json");
        final Map<String, CatalystData> catalysts = new HashMap<>();

        final List<SimpleMinersRepositorySource.PackEntry> entries = new ArrayList<>();
        entries.addAll(SimpleMinersRepositorySource.INSTANCE.findCandidates());
        entries.addAll(BuiltInPacksRepository.instance.findPacks());

        for (SimpleMinersRepositorySource.PackEntry packEntry : entries) {
            final var inZip = packEntry.pathGetter().get("catalysts.json");
            if (Files.exists(inZip)) {
                loadCatalystsFromFile(catalysts, inZip);
            }
            packEntry.pathGetter().close();
        }

        if (Files.exists(path)) {
            // Read catalysts from file *after* builtins, so they can be overridden
            loadCatalystsFromFile(catalysts, path);
        }

        return catalysts;
    }

    private static void loadCatalystsFromFile(Map<String, CatalystData> map, Path path) throws IOException {
        try (final var reader = Files.newBufferedReader(path)) {
            final JsonObject json = GSON.fromJson(reader, JsonObject.class);
            json.keySet().forEach(key -> {
                final var pJson = json.getAsJsonObject(key);
                if (pJson.has("requiredMod")) {
                    if (StreamSupport.stream(GsonHelper.getAsJsonArray(pJson, "requiredMods").spliterator(), false)
                            .anyMatch(it -> !ModList.get().isLoaded(it.getAsString()))) {
                        return;
                    }
                }
                map.put(key, new CatalystData(readProps(pJson), GsonHelper.getAsString(pJson, "translation", null)));
            });
        }
    }

    public record CatalystData(Item.Properties properties, @Nullable String translation) {}
}
