package vulpisgiardina.simplekey.network;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import vulpisgiardina.simplekey.Simplekey;
import vulpisgiardina.simplekey.menu.KeyWorkbenchMenu;
import vulpisgiardina.simplekey.network.packets.OpenDoorPacket;
import vulpisgiardina.simplekey.network.packets.UpdateKeyDoorPacket;
import vulpisgiardina.simplekey.core.init.DataComponentInit;
import vulpisgiardina.simplekey.network.packets.UpdateKeyOnWorkbenchPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import vulpisgiardina.simplekey.network.packets.UpdatePasswordDoorPacket;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1.0";

    public static void register(final PayloadRegistrar registrar) {
        registrar.playToServer(
                UpdateKeyDoorPacket.TYPE, // どのパケットIDか
                UpdateKeyDoorPacket.STREAM_CODEC, // どうやってデータを読み書きするか
                PacketHandler::handleUpdateKeyDoorPacket // 受け取ったときにどの処理を呼ぶか
        );

        registrar.playToServer(
                UpdateKeyOnWorkbenchPacket.TYPE,
                UpdateKeyOnWorkbenchPacket.STREAM_CODEC,
                PacketHandler::handleUpdateKeyOnWorkbenchPacket
        );

        registrar.playToServer(
                UpdatePasswordDoorPacket.TYPE,
                UpdatePasswordDoorPacket.STREAM_CODEC,
                PacketHandler::handleUpdatePasswordDoorPacket
        );

        registrar.playToServer(
                OpenDoorPacket.TYPE,
                OpenDoorPacket.STREAM_CODEC,
                PacketHandler::handleOpenDoorPacket
        );
    }

    // パケットを受け取ったときの処理
    private static void handleUpdateKeyDoorPacket(final UpdateKeyDoorPacket packet, final IPayloadContext context) {
        // ネットワーク処理は別スレッドで実行されることがあるため、
        // 必ずメインスレッドで実行されるように予約する
        context.enqueueWork(() -> {
            // サーバー側のプレイヤーとワールドを取得
            context.player();
            if (context.player().level() instanceof ServerLevel level) {
                BlockPos pos = packet.pos();
                // 権限や距離のチェックをここに入れるとより安全
                if (level.isLoaded(pos)) {
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be != null) {
                        // BlockEntityのDataComponentを新しい値で更新
                        DataComponentMap currentComponents = be.components();
                        DataComponentMap.Builder builder = DataComponentMap.builder().addAll(currentComponents);
                        builder.set(DataComponentInit.KEYCODE, packet.newCode());
                        be.setComponents(builder.build());
                        // setChanged()を呼ぶことで、このブロックのデータが変更されたことをシステムに伝え、
                        // ワールドセーブ時に保存されるようにする
                        be.setChanged();
                    }
                }
            }
        });
    }

    private static void handleUpdateKeyOnWorkbenchPacket(final UpdateKeyOnWorkbenchPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            context.player();
            AbstractContainerMenu menu = context.player().containerMenu;
            // 開いているGUIがKeyWorkbenchMenuか確認
            if (menu instanceof KeyWorkbenchMenu workbenchMenu) {
                // スロットにあるアイテムを取得
                Slot slot = workbenchMenu.getSlot(0);
                ItemStack keyStack = slot.getItem();
                Simplekey.LOGGER.info("PacketHandler: check itemstack...");
                if (!keyStack.isEmpty()) {
                    // DataComponentを更新
                    keyStack.set(DataComponentInit.KEYCODE, packet.newCode());
                    if (packet.newName().isEmpty()) {
                        Simplekey.LOGGER.info("PacketHandler: name removed.");
                        keyStack.remove(DataComponents.CUSTOM_NAME);
                    } else {
                        Simplekey.LOGGER.info("PacketHandler: name changed.");
                        keyStack.set(DataComponents.CUSTOM_NAME, Component.literal(packet.newName()));
                    }
                    slot.setChanged();
                }
            }
        });
    }

    private static void handleUpdatePasswordDoorPacket(final UpdatePasswordDoorPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            context.player();
            if (context.player().level() instanceof ServerLevel level) {
                BlockPos pos = packet.pos();
                if (level.isLoaded(pos)) {
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be != null) {
                        DataComponentMap currentComponents = be.components();
                        DataComponentMap.Builder builder = DataComponentMap.builder().addAll(currentComponents);
                        builder.set(DataComponentInit.PASSWORD, packet.newPassword());
                        be.setComponents(builder.build());
                        be.setChanged();
                    }
                }
            }
        });
    }

    private static void handleOpenDoorPacket(final OpenDoorPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            context.player();
            if (context.player().level() instanceof ServerLevel level) {
                BlockPos pos = packet.pos();
                if (level.isLoaded(pos)) {
                    BlockState state = level.getBlockState(pos);

                    // そのブロックがドアブロックであり、かつ「閉まっている」場合のみ処理する
                    if (state.getBlock() instanceof DoorBlock doorBlock && !state.getValue(DoorBlock.OPEN)) {
                        BlockState newState = state.setValue(DoorBlock.OPEN, true);
                        level.setBlock(pos, newState, 10);
                        level.playSound(null, pos, doorBlock.type().doorOpen(), SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
                        level.gameEvent(context.player(), GameEvent.BLOCK_OPEN, pos);
                    }
                }
            }
        });
    }
}
