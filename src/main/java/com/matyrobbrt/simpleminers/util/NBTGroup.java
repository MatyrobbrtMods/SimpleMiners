package com.matyrobbrt.simpleminers.util;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

@SuppressWarnings("rawtypes")
public class NBTGroup implements INBTSerializable<CompoundTag> {
    private final Map<String, SerializationEntry> serializables = new HashMap<>();
    private NBTGroup() {}

    public static NBTGroup make() {
        return new NBTGroup();
    }

    @CanIgnoreReturnValue
    public NBTGroup add(String key, @Nullable INBTSerializable serializable) {
        if (serializable == null) return this;
        serializables.put(key, new SerializationEntry(serializable, null));
        return this;
    }

    @CanIgnoreReturnValue
    public NBTGroup add(String key, @Nullable INBTSerializable serializable, int tagType) {
        if (serializable == null) return this;
        serializables.put(key, new SerializationEntry(serializable, tagType));
        return this;
    }

    @CanIgnoreReturnValue
    public NBTGroup addInt(String key, IntSupplier getter, IntConsumer setter) {
       return add(key, new INBTSerializable<IntTag>() {
            @Override
            public IntTag serializeNBT() {
                return IntTag.valueOf(getter.getAsInt());
            }

            @Override
            public void deserializeNBT(IntTag nbt) {
                setter.accept(nbt.getAsInt());
            }
        }, Tag.TAG_INT);
    }

    @CanIgnoreReturnValue
    public NBTGroup grouped(String key, Consumer<NBTGroup> groupConsumer) {
        final var group = new NBTGroup();
        groupConsumer.accept(group);
        return add(key, group, Tag.TAG_COMPOUND);
    }

    @Override
    public CompoundTag serializeNBT() {
        final CompoundTag tag = new CompoundTag();
        serializables.forEach((key, ser) -> tag.put(key, ser.serializable.serializeNBT()));
        return tag;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void deserializeNBT(CompoundTag nbt) {
        serializables.forEach((key, ser) -> {
            if (ser.test(nbt, key)) {
                ser.serializable.deserializeNBT(nbt.get(key));
            }
        });
    }

    record SerializationEntry(INBTSerializable serializable, @Nullable Integer tagType) {
        public boolean test(CompoundTag compoundTag, String key) {
            return tagType == null ? compoundTag.contains(key) : compoundTag.contains(key, tagType);
        }
    }
}
