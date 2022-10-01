package com.matyrobbrt.simpleminers.network;

import com.matyrobbrt.simpleminers.SimpleMiners;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Function;

public class SimpleMinersNetwork {
    public static final String VERSION = "1.0";
    public static final ResourceLocation NAME = new ResourceLocation(SimpleMiners.MOD_ID, "network");
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder.named(NAME)
            .networkProtocolVersion(() -> VERSION)
            .clientAcceptedVersions(str -> str.equals(VERSION))
            .serverAcceptedVersions(str -> str.equals(VERSION))
            .simpleChannel();

    public static void register() {
        class Registrar {
            int id = 0;
            <P extends Packet> void register(Class<P> pkt, Function<FriendlyByteBuf, P> decoder) {
                CHANNEL.messageBuilder(pkt, id++)
                        .consumerMainThread((packet, contextSupplier) -> {
                            final var ctx = contextSupplier.get();
                            packet.handle(ctx);
                        })
                        .encoder(Packet::encode)
                        .decoder(decoder)
                        .add();
            }
        }

        final var registry = new Registrar();
        registry.register(UninstallUpgradePacket.class, UninstallUpgradePacket::decode);
        registry.register(SyncUpgradesPacket.class, SyncUpgradesPacket::decode);
        registry.register(SyncUpgradeConfigurations.class, SyncUpgradeConfigurations::decode);
    }
}
