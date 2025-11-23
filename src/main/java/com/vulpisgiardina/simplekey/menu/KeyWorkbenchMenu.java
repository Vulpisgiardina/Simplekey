package com.vulpisgiardina.simplekey.menu;

import com.vulpisgiardina.simplekey.core.init.BlockInit;
import com.vulpisgiardina.simplekey.core.init.MenuInit;
import com.vulpisgiardina.simplekey.menu.slot.KeyOnlySlot;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

public class KeyWorkbenchMenu extends AbstractContainerMenu {
    public final BlockPos pos;
    private final ContainerLevelAccess levelAccess;
    public final ItemStacksResourceHandler itemHandler; // 鍵スロット用のインベントリ
    public final ContainerData data; // ダイヤルの値を同期するためのデータ

    private static final int KEY_SLOT_INDEX = 0;
    private static final int PLAYER_INV_START_INDEX = 1;
    private static final int PLAYER_INV_END_INDEX = 28; // 1 + 27
    private static final int PLAYER_HOTBAR_START_INDEX = 28;
    private static final int PLAYER_HOTBAR_END_INDEX = 37; // 28

    // Client
    public KeyWorkbenchMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInv, extraData.readBlockPos());
    }

    // Server
    public KeyWorkbenchMenu(int containerId, Inventory playerInv, BlockPos pos) {
        super(MenuInit.KEY_WORKBENCH_MENU.get(), containerId);
        this.pos = pos;
        this.levelAccess = ContainerLevelAccess.create(playerInv.player.level(), pos);
        this.itemHandler = new ItemStacksResourceHandler(1); // サーバー側のインベントリ実体
        this.data = new SimpleContainerData(8);     // サーバー側のデータ実体

        // --- スロットとデータの配置 ---
        this.addSlot(new KeyOnlySlot(this.itemHandler, 0, 16, 20));
        addPlayerInventory(playerInv);
        this.addDataSlots(this.data);
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack sourceStack = slot.getItem();
            itemstack = sourceStack.copy();

            // プレイヤーインベントリ/ホットバー → 鍵スロットへの移動
            if (index >= PLAYER_INV_START_INDEX) {
                // moveItemStackToの第3引数(endIndex)は含まないので、+1する
                if (!this.moveItemStackTo(sourceStack, KEY_SLOT_INDEX, PLAYER_INV_START_INDEX, false)) {
                    return ItemStack.EMPTY;
                }
            }
            // 鍵スロット → プレイヤーインベントリへの移動
            else if (!this.moveItemStackTo(sourceStack, PLAYER_INV_START_INDEX, PLAYER_HOTBAR_END_INDEX, false)) {
                return ItemStack.EMPTY;
            }

            if (sourceStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);

        // サーバー側で、GUIが閉じられたときにスロット0のアイテムをプレイヤーに返す
        this.levelAccess.execute((level, pos) -> {
            this.clearContainer(player, new SimpleContainer(this.itemHandler.copyToList().get(0)));
        });
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.levelAccess, player, BlockInit.KEY_WORKBENCH.get());
    }
}
