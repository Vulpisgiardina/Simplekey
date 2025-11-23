package com.vulpisgiardina.simplekey.network.packets;

import com.vulpisgiardina.simplekey.Simplekey;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record UpdateKeyOnWorkbenchPacket(int newCode, String newName) implements CustomPacketPayload {
    public static final Type<UpdateKeyOnWorkbenchPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Simplekey.MODID, "update_key_on_workbench"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateKeyOnWorkbenchPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            UpdateKeyOnWorkbenchPacket::newCode,
            ByteBufCodecs.STRING_UTF8,
            UpdateKeyOnWorkbenchPacket::newName,
            UpdateKeyOnWorkbenchPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
