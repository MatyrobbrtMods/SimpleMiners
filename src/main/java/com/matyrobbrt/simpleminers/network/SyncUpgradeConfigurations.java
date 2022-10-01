package com.matyrobbrt.simpleminers.network;

import com.matyrobbrt.simpleminers.miner.upgrade.MinerUpgradeType;
import com.matyrobbrt.simpleminers.miner.upgrade.UpgradeConfiguration;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;

public record SyncUpgradeConfigurations(Map<MinerUpgradeType, Map<String, UpgradeConfiguration>> map) implements Packet {

    @SuppressWarnings("ConstantConditions")
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeMap(map, (buf1, minerUpgradeType) -> buf1.writeResourceLocation(MinerUpgradeType.UPGRADE_TYPES.getKey(minerUpgradeType)),
                (buf1, configuration) -> buf1.writeMap(configuration, FriendlyByteBuf::writeUtf,
                        (buf2, configuration1) -> buf2.writeNbt((CompoundTag) JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, ((UpgradeConfiguration.Impl) configuration1).jsonObject()))));
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        map.forEach(UpgradeConfiguration.Store::put);
    }

    @SuppressWarnings("ConstantConditions")
    public static SyncUpgradeConfigurations decode(FriendlyByteBuf buf) {
        return new SyncUpgradeConfigurations(buf.readMap(buf1 ->
                MinerUpgradeType.UPGRADE_TYPES.get(buf1.readResourceLocation()),
        buf1 -> buf1.readMap(FriendlyByteBuf::readUtf, buf2 -> new UpgradeConfiguration.Impl(
                NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, buf2.readNbt()).getAsJsonObject()
        ))));
    }
}
