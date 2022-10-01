package com.matyrobbrt.simpleminers.network;

import com.matyrobbrt.simpleminers.miner.MinerBE;
import com.matyrobbrt.simpleminers.miner.upgrade.MinerUpgradeType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

@SuppressWarnings("ConstantConditions")
public record UninstallUpgradePacket(MinerUpgradeType type, int amount, BlockPos pos) implements Packet {
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(MinerUpgradeType.UPGRADE_TYPES.getKey(type));
        buf.writeInt(amount);
        buf.writeBlockPos(pos);
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        if (context.getSender().level.getBlockEntity(pos) instanceof MinerBE miner) {
            miner.upgrades.removeUpgrade(type, amount);
        }
    }

    public static UninstallUpgradePacket decode(FriendlyByteBuf buf) {
        return new UninstallUpgradePacket(MinerUpgradeType.UPGRADE_TYPES.get(buf.readResourceLocation()), buf.readInt(), buf.readBlockPos());
    }
}
