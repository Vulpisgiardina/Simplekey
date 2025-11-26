package vulpisgiardina.simplekey.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import vulpisgiardina.simplekey.core.init.BlockInit;
import vulpisgiardina.simplekey.core.init.DataComponentInit;
import vulpisgiardina.simplekey.core.init.MenuInit;

public class PasswordDoorMenu extends AbstractContainerMenu {
    public final BlockPos blockPos;
    public final ContainerLevelAccess levelAccess;
    public final String initialPassword;
    public final boolean isEditable;

    // Client
    public PasswordDoorMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInv, playerInv.player.level(), extraData.readBlockPos(), extraData.readUtf(), extraData.readBoolean());
    }

    // Server
    public PasswordDoorMenu(int containerId, Inventory playerInv, Level level, BlockPos pos, boolean isEditable) {
        this(containerId, playerInv, level, pos,
                level.getBlockEntity(pos).components().getOrDefault(DataComponentInit.PASSWORD, "0000"), isEditable);
    }

    private PasswordDoorMenu(int containerId, Inventory playerInv, Level level, BlockPos pos, String initialPassword, boolean isEditable) {
        super(MenuInit.PASSWORD_DOOR_MENU.get(), containerId);
        this.blockPos = pos;
        this.levelAccess = ContainerLevelAccess.create(level, pos);
        this.initialPassword = initialPassword;
        this.isEditable = isEditable;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(levelAccess, player, BlockInit.PASSWORD_DOOR.get());
    }
}
