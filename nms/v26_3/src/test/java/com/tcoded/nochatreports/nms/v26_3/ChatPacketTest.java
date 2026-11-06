package com.tcoded.nochatreports.nms.v26_3;

import com.tcoded.nochatreports.nms.v26_3.wrapper.PlayerChatPacketImpl;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.ChatTypeDecoration;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FilterMask;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ChatPacketTest {

    @BeforeAll
    static void initializeRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void unsignedContentIsOptionalComponentOnPaper263() throws Exception {
        Method method = ClientboundPlayerChatPacket.class.getMethod("unsignedContent");
        assertEquals(Optional.class, method.getReturnType());
        ParameterizedType generic = (ParameterizedType) method.getGenericReturnType();
        assertEquals(Component.class, generic.getActualTypeArguments()[0]);
    }

    @Test
    void keepsUnsignedContentAndChatDecoration() {
        Component unsigned = Component.literal("formatted message").withStyle(ChatFormatting.GOLD);
        ClientboundPlayerChatPacket packet = packet(Optional.of(unsigned));

        ClientboundSystemChatPacket result = (ClientboundSystemChatPacket) new PlayerChatPacketImpl(packet).toSystem().toNmsPacket();

        assertEquals(packet.chatType().decorate(unsigned), result.content());
        assertFalse(result.overlay());
    }

    @Test
    void fallsBackToSignedBodyWhenUnsignedContentIsAbsent() {
        ClientboundPlayerChatPacket packet = packet(Optional.empty());

        ClientboundSystemChatPacket result = (ClientboundSystemChatPacket) new PlayerChatPacketImpl(packet).toSystem().toNmsPacket();

        assertEquals(packet.chatType().decorate(Component.literal("original message")), result.content());
        assertFalse(result.overlay());
    }

    private static ClientboundPlayerChatPacket packet(Optional<Component> unsigned) {
        SignedMessageBody.Packed body = new SignedMessageBody.Packed(
                "original message",
                Instant.EPOCH,
                1L,
                LastSeenMessages.Packed.EMPTY
        );
        ChatType type = new ChatType(
                ChatTypeDecoration.withSender("chat.type.text"),
                ChatTypeDecoration.withSender("chat.type.text.narrate")
        );
        ChatType.Bound bound = new ChatType.Bound(Holder.direct(type), Component.literal("Alex"), Optional.empty());
        return new ClientboundPlayerChatPacket(
                0,
                new UUID(1L, 2L),
                1,
                Optional.empty(),
                body,
                unsigned,
                FilterMask.PASS_THROUGH,
                bound
        );
    }
}
