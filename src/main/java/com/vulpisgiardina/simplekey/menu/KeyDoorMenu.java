package com.vulpisgiardina.simplekey.menu;

import com.vulpisgiardina.simplekey.core.init.BlockInit;
import com.vulpisgiardina.simplekey.core.init.DataComponentInit;
import com.vulpisgiardina.simplekey.core.init.MenuInit;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class KeyDoorMenu extends AbstractContainerMenu {
    public final BlockPos blockPos;
    public final ContainerLevelAccess levelAccess;
    public final int initialCode;

    // Client
    public KeyDoorMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInv, playerInv.player.level(), extraData.readBlockPos(), extraData.readInt());
    }

    // Server
    public KeyDoorMenu(int containerId, Inventory playerInv, Level level, BlockPos pos) {
        this(containerId, playerInv, level, pos,
                level.getBlockEntity(pos).components().getOrDefault(DataComponentInit.KEYCODE, 0));
    }

    private KeyDoorMenu(int containerId, Inventory playerInv, Level level, BlockPos pos, int initialCode) {
        super(MenuInit.KEY_DOOR_MENU.get(), containerId);
        this.blockPos = pos;
        this.levelAccess = ContainerLevelAccess.create(level, pos);
        this.initialCode = initialCode; // ★ フィールドに値を設定
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(levelAccess, player, BlockInit.KEY_DOOR.get());
    }
}
