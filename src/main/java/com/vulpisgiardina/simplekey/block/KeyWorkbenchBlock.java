package com.vulpisgiardina.simplekey.block;

import com.vulpisgiardina.simplekey.menu.KeyWorkbenchMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class KeyWorkbenchBlock extends Block {
    private static final Component CONTAINER_TITLE = Component.translatable("container.simplekey.key_workbench");

    public KeyWorkbenchBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            // serverPlayer.openMenu(getMenuProvider(state, level, pos));
            MenuProvider menuProvider = getMenuProvider(state, level, pos);
            if (menuProvider != null) {
                serverPlayer.openMenu(menuProvider, friendlyByteBuf -> {
                    friendlyByteBuf.writeBlockPos(pos);
                });
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider(
                (containerId, playerInv, p) -> new KeyWorkbenchMenu(containerId, playerInv, pos),
                CONTAINER_TITLE
        );
    }
}
