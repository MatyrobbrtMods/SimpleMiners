package com.matyrobbrt.simpleminers.miner.upgrade;

import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

import java.util.HashMap;
import java.util.Map;

public interface UpgradeConfiguration {
    int getInt(String key, int fallback);
    default int getInt(String key) {
        return getInt(key, 0);
    }

    boolean getBoolean(String key, boolean fallback);
    default boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    static UpgradeConfiguration get(MinerUpgradeType type, String minerType) {
        return Store.CONFIGURATIONS.computeIfAbsent(type, k -> new HashMap<>()).getOrDefault(minerType, Store.DEFAULT);
    }

    record Impl(JsonObject jsonObject) implements UpgradeConfiguration {
        @Override
        public int getInt(String key, int fallback) {
            return GsonHelper.getAsInt(jsonObject, key, fallback);
        }

        @Override
        public boolean getBoolean(String key, boolean fallback) {
            return GsonHelper.getAsBoolean(jsonObject, key, fallback);
        }
    }

    final class Store {
        private static final UpgradeConfiguration DEFAULT = new Impl(new JsonObject());
        public static final Map<MinerUpgradeType, Map<String, UpgradeConfiguration>> CONFIGURATIONS = new HashMap<>();

        public static void put(MinerUpgradeType type, Map<String, UpgradeConfiguration> configuration) {
            CONFIGURATIONS.put(type, configuration);
        }

        public static void put(MinerUpgradeType type, String name, UpgradeConfiguration configuration) {
            CONFIGURATIONS.computeIfAbsent(type, it -> new HashMap<>()).put(name, configuration);
        }
    }
}
