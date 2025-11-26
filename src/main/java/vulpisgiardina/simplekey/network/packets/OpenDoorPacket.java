package vulpisgiardina.simplekey.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import vulpisgiardina.simplekey.Simplekey;

public record OpenDoorPacket(BlockPos pos) implements CustomPacketPayload {

    public static final Type<OpenDoorPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Simplekey.MODID, "open_door"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenDoorPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            OpenDoorPacket::pos,
            OpenDoorPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
