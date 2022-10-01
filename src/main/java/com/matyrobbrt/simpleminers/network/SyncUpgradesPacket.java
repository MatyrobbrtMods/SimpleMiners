package com.matyrobbrt.simpleminers.network;

import com.matyrobbrt.simpleminers.menu.MinerMenu;
import com.matyrobbrt.simpleminers.miner.upgrade.MinerUpgradeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;

@SuppressWarnings("ConstantConditions")
public record SyncUpgradesPacket(int windowId, Map<MinerUpgradeType, Integer> map) implements Packet {
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeByte(windowId);
        buf.writeMap(map, (buf1, minerUpgradeType) -> buf1.writeResourceLocation(MinerUpgradeType.UPGRADE_TYPES.getKey(minerUpgradeType)), FriendlyByteBuf::writeInt);
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        AbstractContainerMenu container = getPlayer().containerMenu;
        if (container.containerId == windowId && container instanceof MinerMenu menu) {
            menu.be.upgrades.syncFrom(map);
        }
    }

    public static LocalPlayer getPlayer() {
        return Minecraft.getInstance().player;
    }

    public static SyncUpgradesPacket decode(FriendlyByteBuf buf) {
        return new SyncUpgradesPacket(buf.readUnsignedByte(), buf.readMap(buf1 -> MinerUpgradeType.UPGRADE_TYPES.get(buf1.readResourceLocation()), FriendlyByteBuf::readInt));
    }
}
