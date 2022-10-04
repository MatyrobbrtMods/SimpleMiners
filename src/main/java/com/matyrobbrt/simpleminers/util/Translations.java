package com.matyrobbrt.simpleminers.util;

import com.matyrobbrt.simpleminers.SimpleMiners;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public enum Translations {
    GUI_ENERGY("gui", "energy", "Energy: %s / %s FE"),
    GUI_TRANSFER_RATE("gui", "transfer_rate", "I/O rate: %s FE"),
    GUI_USAGE_PER_TICK("gui", "usage_per_tick", "Usage: %s FE / tick"),
    NEETO("gui", "neeto", "Not enough energy to operate!"),

    TOOLTIP_RC_UNINSTALL("tooltip", "rcuninstall", "Right click to uninstall."),
    TOOLTIP_INSTALLED_UPGRADES("tooltip", "installed_upgrades", "Amount installed: %s / %s"),
    TOOLTIP_ADD_CATALYSTS("tooltip", "add_catalysts", "Add catalysts"),
    TOOLTIP_INSTALL_UPGRADES("tooltip", "install_upgrades", "Install upgrades"),
    TOOLTIP_WEIGHT("tooltip", "weight", "Weight: %s"),
    TOOLTIP_REQUIREMENTS("tooltip", "requirements", "Requirements:"),
    TOOLTIP_MODIFIERS("tooltip", "modifiers", "Modifiers:"),
    TOOLTIP_REFRESH_RESULTS("tooltip", "refresh_results", "Refresh the results"),
    TOOLTIP_SHOW_RESULTS("tooltip", "show_results", "Show what this miner type can mine"),
    TOOLTIP_SHOW_ALL_RESULTS("tooltip", "show_all_results", "Show all results, regardless of possibility"),

    ITEM_STORED_ENERGY("item_tooltip", "stored_energy", "Stored energy: %s FE"),
    ITEM_STORED_UPGRADES("item_tooltip", "stored_upgrades", "Stored upgrades: %s"),
    ITEM_STORED_CATALYSTS("item_tooltip", "stored_catalysts", "Stored catalysts: %s"),

    ANY_OF("text", "any_of", "any of %s"),
    ALL_OF("text", "all_of", "all of %s"),
    NOT("text", "not", "not %s"),
    NOT_CAP("text", "not_cap", "Not %s"),

    IN_BIOME_PREDICATE("result_predicate", "in_biome", "Biome(s): %s"),
    BIOME_WEIGHT_BONUS("result_modifier", "biome_weight_bonus", "%s weight if in biome(s) %s"),
    CATALYST_BONUS("result_modifier", "catalyst_bonus", "%s weight with catalyst %s (additive)"),
    CATALYST_BONUS_NON_ADDITIVE("result_modifier", "catalyst_bonus_non_additive", "%s weight with catalyst %s"),
    WEATHER_PREDICATE("result_predicate", "weather", "Requires weather:%s %s"),
    IN_DIMENSION_PREDICATE("result_predicate", "in_dimension", "In dimension: %s"),
    POSITION_PREDICATE("result_predicate", "position", "Miner position on axis %s is %s %s"),

    GUI_MINER("gui", "miner", "Miner"),
    GUI_CATALYSTS("gui", "catalysts", "Catalysts"),
    GUI_UPGRADES("gui", "upgrades", "Upgrades"),
    JEI_MINING("jei", "mining", "Mining");

    public final String key, english;

    Translations(String type, String key, String english) {
        this.key = type + "." + SimpleMiners.MOD_ID + "." + key;
        this.english = english;
    }

    public MutableComponent get(Object... args) {
        return Component.translatable(key, args);
    }
}
