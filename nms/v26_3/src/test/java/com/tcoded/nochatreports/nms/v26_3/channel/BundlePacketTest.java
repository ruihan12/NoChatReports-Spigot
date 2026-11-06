package com.tcoded.nochatreports.nms.v26_3.channel;

import com.tcoded.nochatreports.nms.channel.GlobalPacketHandler;
import com.tcoded.nochatreports.nms.types.PacketWriteResult;
import io.netty.channel.Channel;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BundlePacketTest {

    @Test
    void rewritesImmutableBundleWithoutMutatingOriginalOrder() {
        ClientboundSystemChatPacket first = message("first");
        ClientboundSystemChatPacket cancelled = message("cancelled");
        ClientboundSystemChatPacket replaced = message("replaced");
        ClientboundSystemChatPacket last = message("last");
        ClientboundSystemChatPacket replacement = message("replacement");
        List<Packet<? super ClientGamePacketListener>> originalPackets = List.of(first, cancelled, replaced, last);
        ClientboundBundlePacket original = new ClientboundBundlePacket(originalPackets);

        GlobalPacketHandlerImpl handler = new GlobalPacketHandlerImpl(null) {
            @Override
            protected PacketWriteResult<?> handleAnyPacket(Channel channel, Object packet) {
                if (packet == cancelled) {
                    return new PacketWriteResult<>(false, packet);
                }
                if (packet == replaced) {
                    return new PacketWriteResult<>(true, replacement);
                }
                return new PacketWriteResult<>(true, packet);
            }
        };

        EmbeddedChannel channel = new EmbeddedChannel(new ChannelPacketHandler(handler));
        try {
            assertTrue(channel.writeOutbound(original));
            ClientboundBundlePacket output = channel.readOutbound();
            assertEquals(List.of(first, replacement, last), copy(output));
            assertEquals(List.of(first, cancelled, replaced, last), copy(original));
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    void completesPromiseWhenEntireBundleIsCancelled() {
        GlobalPacketHandler handler = new GlobalPacketHandlerImpl(null) {
            @Override
            protected PacketWriteResult<?> handleAnyPacket(Channel channel, Object packet) {
                return new PacketWriteResult<>(false, packet);
            }
        };
        EmbeddedChannel channel = new EmbeddedChannel(new ChannelPacketHandler(handler));
        try {
            var future = channel.writeOneOutbound(new ClientboundBundlePacket(List.of(message("cancelled"))));
            channel.flushOutbound();
            assertTrue(future.isSuccess());
            assertNull(channel.readOutbound());
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    private static List<Packet<? super ClientGamePacketListener>> copy(ClientboundBundlePacket bundle) {
        List<Packet<? super ClientGamePacketListener>> packets = new ArrayList<>();
        bundle.subPackets().forEach(packets::add);
        return packets;
    }

    private static ClientboundSystemChatPacket message(String text) {
        return new ClientboundSystemChatPacket(Component.literal(text), false);
    }
}
