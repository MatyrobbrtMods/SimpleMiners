package com.matyrobbrt.simpleminers.data;

import com.matyrobbrt.simpleminers.Registration;
import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.miner.upgrade.MinerUpgradeType;
import com.matyrobbrt.simpleminers.util.Translations;
import net.minecraft.data.DataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraftforge.common.data.LanguageProvider;

public class LangProvider extends LanguageProvider {
    public LangProvider(DataGenerator gen) {
        super(gen, SimpleMiners.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        for (final var tran : Translations.values()) {
            add(tran.key, tran.english);
        }

        add(MinerUpgradeType.SPEED.getName(), "Speed Upgrade");
        add(MinerUpgradeType.SPEED.getDescription(), "Make the miner faster, at the cost of increased energy usage");

        add(MinerUpgradeType.ENERGY.getName(), "Energy Upgrade");
        add(MinerUpgradeType.ENERGY.getDescription(), "Make the miner more energy efficient");

        add(MinerUpgradeType.PRODUCTION.getName(), "Production Upgrade");
        add(MinerUpgradeType.PRODUCTION.getDescription(), "Have a bigger chance at mining one more item per operation");

        add(MinerUpgradeType.FORTUNE.getName(), "Fortune Upgrade");
        add(MinerUpgradeType.FORTUNE.getDescription(), "\"Mine\" the results with Fortune");

        add(Registration.SPEED_UPGRADE.get(), "Speed Upgrade");
        add(Registration.ENERGY_UPGRADE.get(), "Energy Upgrade");
        add(Registration.PRODUCTION_UPGRADE.get(), "Production Upgrade");
        add(Registration.FORTUNE_UPGRADE.get(), "Fortune Upgrade");

        add(Registration.UPGRADE_BASE.get(), "Upgrade Base");
        add(Registration.CATALYST_BASE.get(), "Catalyst Base");

        add(SimpleMiners.ITEM_TAB.getDisplayName(), "Simple Miners");
    }

    private void add(Component component, String text) {
        add(component.getContents() instanceof TranslatableContents tran ? tran.getKey() : component.getString(), text);
    }
}
