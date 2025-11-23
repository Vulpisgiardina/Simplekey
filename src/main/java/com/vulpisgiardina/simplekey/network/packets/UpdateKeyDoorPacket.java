package com.vulpisgiardina.simplekey.network.packets;

import com.vulpisgiardina.simplekey.Simplekey;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record UpdateKeyDoorPacket(BlockPos pos, int newCode) implements CustomPacketPayload {

    // このパケットを識別するためのユニークなID
    public static final Type<UpdateKeyDoorPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Simplekey.MODID, "update_key_door"));

    // パケットの送受信時に、データをどうやってバイト列に変換/復元するかを定義
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateKeyDoorPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, // BlockPos用のコーデック
            UpdateKeyDoorPacket::pos,
            ByteBufCodecs.INT, // int用のコーデック
            UpdateKeyDoorPacket::newCode,
            UpdateKeyDoorPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
