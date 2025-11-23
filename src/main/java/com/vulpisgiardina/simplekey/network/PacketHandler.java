package com.vulpisgiardina.simplekey.network;

import com.vulpisgiardina.simplekey.Simplekey;
import com.vulpisgiardina.simplekey.menu.KeyWorkbenchMenu;
import com.vulpisgiardina.simplekey.network.packets.UpdateKeyDoorPacket;
import com.vulpisgiardina.simplekey.core.init.DataComponentInit;
import com.vulpisgiardina.simplekey.network.packets.UpdateKeyOnWorkbenchPacket;
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
                    Simplekey.LOGGER.info("PacketHandler: itemstack is not empty.");
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
}
