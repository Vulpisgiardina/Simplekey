package com.vulpisgiardina.simplekey.block.entity;

import com.vulpisgiardina.simplekey.core.init.BlockEntityTypeInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class KeyDoorBlockEntity extends BlockEntity {
    public KeyDoorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.KEY_DOOR_BLOCK_ENTITY.get(), pos, state);
    }
}
