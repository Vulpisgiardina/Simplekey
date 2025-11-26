package vulpisgiardina.simplekey.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import vulpisgiardina.simplekey.Simplekey;

public record UpdatePasswordDoorPacket(BlockPos pos, String newPassword) implements CustomPacketPayload {

    public static final Type<UpdatePasswordDoorPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Simplekey.MODID, "update_password_door"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdatePasswordDoorPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            UpdatePasswordDoorPacket::pos,
            ByteBufCodecs.STRING_UTF8,
            UpdatePasswordDoorPacket::newPassword,
            UpdatePasswordDoorPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
