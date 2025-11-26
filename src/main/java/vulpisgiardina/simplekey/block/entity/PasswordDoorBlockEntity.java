package vulpisgiardina.simplekey.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import vulpisgiardina.simplekey.core.init.BlockEntityTypeInit;

public class PasswordDoorBlockEntity extends BlockEntity {
    public PasswordDoorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.PASSWORD_DOOR_BLOCK_ENTITY.get(), pos, state);
    }
}
