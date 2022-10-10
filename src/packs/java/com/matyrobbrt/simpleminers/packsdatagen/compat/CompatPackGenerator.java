package com.matyrobbrt.simpleminers.packsdatagen.compat;

import com.matyrobbrt.simpleminers.packsdatagen.DatagenCheating;
import com.matyrobbrt.simpleminers.packsdatagen.simple.SimplePackGenerator;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.EnumSet;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class CompatPackGenerator extends SimplePackGenerator {
    protected final String modId;
    protected CompatPackGenerator(String modId, GeneratorType type1, GeneratorType... types) {
        super(EnumSet.of(type1, types));
        this.modId = modId;
    }

    protected final Block block(String name) {
        return DatagenCheating.block(new ResourceLocation(modId, name));
    }
    protected final Item item(String name) {
        return DatagenCheating.item(new ResourceLocation(modId, name));
    }

    @Override
    public String packId() {
        return modId;
    }
}
