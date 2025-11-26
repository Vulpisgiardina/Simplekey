package vulpisgiardina.simplekey.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import vulpisgiardina.simplekey.core.init.BlockEntityTypeInit;
import vulpisgiardina.simplekey.core.init.DataComponentInit;
import vulpisgiardina.simplekey.core.init.ItemInit;
import vulpisgiardina.simplekey.menu.PasswordDoorMenu;

import javax.annotation.Nullable;

public class PasswordDoorBlock extends DoorBlock implements EntityBlock {
    public PasswordDoorBlock(BlockSetType blockSetType, Properties properties) {
        super(blockSetType, properties);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        // ドアが開いていれば閉める
        if (state.getValue(OPEN)) {
            if (!level.isClientSide()) {
                state = state.cycle(OPEN);
                level.setBlock(pos, state, 10);
                level.playSound(null, pos, this.type().doorClose(), SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
                level.gameEvent(player, GameEvent.BLOCK_CLOSE, pos);
            }
            return InteractionResult.SUCCESS;
        }

        // クリックされたのが上半分でも下半分でも、常に下半分の位置と状態を基準に処理を行う
        BlockPos lowerPos = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
        BlockState lowerState = level.getBlockState(lowerPos);
        // 下半分のブロックがこのドアでない場合(ありえないはずだが念のため)、何もしない
        if (!lowerState.is(this)) {
            return InteractionResult.PASS;
        }

        // 他のプレイヤーがGUIを開いている
        if (!level.isClientSide()) {
            if (isSomeoneUsing(level, lowerPos)) {
                // 使用中なら警告メッセージを出して終了
                player.displayClientMessage(Component.translatable("message.simplekey.password_door.busy"), true);
                return InteractionResult.SUCCESS; // 処理をここで終える
            }
        }

        ItemStack heldItem = player.getItemInHand(hand);

        // 鍵用工具を持っている
        if (heldItem.is(ItemInit.KEY_TOOL)) {
            if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                // player.displayClientMessage(Component.literal("No GUI"), false);
                BlockEntity be = level.getBlockEntity(lowerPos);
                boolean isEditable = true;
                if (be != null) {
                    serverPlayer.openMenu(new SimpleMenuProvider(
                            (containerId, playerInventory, p) -> new PasswordDoorMenu(containerId, playerInventory, level, lowerPos, isEditable),
                            Component.translatable("gui.simplekey.password_door.setting.title")
                    ), friendlyByteBuf -> {
                        friendlyByteBuf.writeBlockPos(lowerPos);
                        friendlyByteBuf.writeUtf(be.components().getOrDefault(DataComponentInit.PASSWORD, "0000"));
                        friendlyByteBuf.writeBoolean(isEditable);
                    });
                }
            }
            return InteractionResult.SUCCESS;
        }

        // 鍵用工具を持っていない
        else {
            if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                BlockEntity be = level.getBlockEntity(lowerPos);
                boolean isEditable = false;
                if (be != null) {
                    serverPlayer.openMenu(new SimpleMenuProvider(
                            (containerId, playerInventory, p) -> new PasswordDoorMenu(containerId, playerInventory, level, lowerPos, isEditable),
                            Component.translatable("gui.simplekey.password_door.input.title")
                    ), friendlyByteBuf -> {
                        friendlyByteBuf.writeBlockPos(lowerPos);
                        friendlyByteBuf.writeUtf(be.components().getOrDefault(DataComponentInit.PASSWORD, "0000"));
                        friendlyByteBuf.writeBoolean(isEditable);
                    });
                }
            }
            return InteractionResult.SUCCESS;
        }

    }

    private void playSound(@Nullable Entity source, Level level, BlockPos pos, boolean isOpening) {
        level.playSound(
                source,
                pos,
                isOpening ? this.type().doorOpen() : this.type().doorClose(),
                SoundSource.BLOCKS,
                1.0F,
                level.getRandom().nextFloat() * 0.1F + 0.9F
        );
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation fromPos, boolean isMoving) {
        // 何もしない
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // 親クラスの処理をほぼコピーしつつ、信号によって開いた状態にならないように修正する
        BlockPos blockpos = context.getClickedPos();
        Level level = context.getLevel();
        if (blockpos.getY() < level.getMaxY() - 1 && level.getBlockState(blockpos.above()).canBeReplaced(context)) {
            return this.defaultBlockState()
                    .setValue(FACING, context.getHorizontalDirection())
                    .setValue(HINGE, this.getHinge(context)) // このメソッドはprivateなので、次のステップでコピー
                    .setValue(POWERED, false) // 常にfalse
                    .setValue(OPEN, false)    // 常にfalse
                    .setValue(HALF, DoubleBlockHalf.LOWER);
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityTypeInit.PASSWORD_DOOR_BLOCK_ENTITY.get().create(pos, state);
    }

    private DoorHingeSide getHinge(BlockPlaceContext context) {
        BlockGetter blockgetter = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        Direction direction = context.getHorizontalDirection();
        BlockPos blockpos1 = blockpos.above();
        Direction direction1 = direction.getCounterClockWise();
        BlockPos blockpos2 = blockpos.relative(direction1);
        BlockState blockstate = blockgetter.getBlockState(blockpos2);
        BlockPos blockpos3 = blockpos1.relative(direction1);
        BlockState blockstate1 = blockgetter.getBlockState(blockpos3);
        Direction direction2 = direction.getClockWise();
        BlockPos blockpos4 = blockpos.relative(direction2);
        BlockState blockstate2 = blockgetter.getBlockState(blockpos4);
        BlockPos blockpos5 = blockpos1.relative(direction2);
        BlockState blockstate3 = blockgetter.getBlockState(blockpos5);
        int i = (blockstate.isCollisionShapeFullBlock(blockgetter, blockpos2) ? -1 : 0)
                + (blockstate1.isCollisionShapeFullBlock(blockgetter, blockpos3) ? -1 : 0)
                + (blockstate2.isCollisionShapeFullBlock(blockgetter, blockpos4) ? 1 : 0)
                + (blockstate3.isCollisionShapeFullBlock(blockgetter, blockpos5) ? 1 : 0);
        boolean flag = blockstate.getBlock() instanceof DoorBlock && blockstate.getValue(HALF) == DoubleBlockHalf.LOWER;
        boolean flag1 = blockstate2.getBlock() instanceof DoorBlock && blockstate2.getValue(HALF) == DoubleBlockHalf.LOWER;
        if ((!flag || flag1) && i <= 0) {
            if ((!flag1 || flag) && i >= 0) {
                int j = direction.getStepX();
                int k = direction.getStepZ();
                Vec3 vec3 = context.getClickLocation();
                double d0 = vec3.x - blockpos.getX();
                double d1 = vec3.z - blockpos.getZ();
                return (j >= 0 || !(d1 < 0.5)) && (j <= 0 || !(d1 > 0.5)) && (k >= 0 || !(d0 > 0.5)) && (k <= 0 || !(d0 < 0.5))
                        ? DoorHingeSide.LEFT
                        : DoorHingeSide.RIGHT;
            } else {
                return DoorHingeSide.LEFT;
            }
        } else {
            return DoorHingeSide.RIGHT;
        }
    }

    private boolean isSomeoneUsing(Level level, BlockPos pos) {
        // サーバー側の全プレイヤーをループして確認
        for (Player player : level.players()) {
            // プレイヤーが開いているメニューが PasswordDoorMenu かどうか
            if (player.containerMenu instanceof PasswordDoorMenu menu) {
                // そのメニューが紐づいている座標が、今のドアの座標と同じか
                if (menu.blockPos.equals(pos)) {
                    return true; // 誰かが使っている
                }
            }
        }
        return false; // 誰も使っていない
    }
}
