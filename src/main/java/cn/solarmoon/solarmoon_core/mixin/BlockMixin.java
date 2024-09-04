package cn.solarmoon.solarmoon_core.mixin;

import cn.solarmoon.solarmoon_core.api.blockstate_access.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public abstract class BlockMixin extends BlockBehaviour {

    @Shadow public abstract BlockState defaultBlockState();

    @Shadow private BlockState defaultBlockState;

    @Shadow protected abstract void registerDefaultState(BlockState p_49960_);

    private Block block = (Block)(Object)this;

    public BlockMixin(Properties p_60452_) {
        super(p_60452_);
    }

    @Inject(method = "registerDefaultState", at = @At("TAIL"))
    public void registerDefaultState(BlockState state, CallbackInfo ci) {
        if (block instanceof IHorizontalFacingBlock facingBlock) {
            state = state.setValue(facingBlock.FACING, Direction.NORTH);
        }
        if (block instanceof IWaterLoggedBlock waterBlock) {
            state = state.setValue(waterBlock.WATERLOGGED, false);
        }
        if (block instanceof IStackBlock stackBlock) {
            state = state.setValue(stackBlock.STACK, 1);
        }
        if (block instanceof ILitBlock litBlock) {
            state = state.setValue(litBlock.LIT, litBlock.defaultLitValue());
        }
        if (block instanceof INoLimitAgeBlock nlAgeBlock) {
            state = state.setValue(nlAgeBlock.AGE, 0);
        }
        if (block instanceof IBedPartBlock bedPartBlock) {
            state = state.setValue(bedPartBlock.PART, BedPart.FOOT);
        }
        if (block instanceof IDoubleBlock doubleBlock) {
            state = state.setValue(doubleBlock.HALF, doubleBlock.getDefaultHalfValue());
        }
        if (block instanceof IMultilayerBlock layer) {
            state = state.setValue(layer.LAYER, 0);
        }
        this.defaultBlockState = state;
    }

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;createBlockStateDefinition(Lnet/minecraft/world/level/block/state/StateDefinition$Builder;)V"))
    private StateDefinition.Builder<Block, BlockState> modifyBuilder(StateDefinition.Builder<Block, BlockState> builder) {
        if (block instanceof IHorizontalFacingBlock facingBlock) {
            builder.add(facingBlock.FACING);
        }
        if (block instanceof IWaterLoggedBlock waterBlock) {
            builder.add(waterBlock.WATERLOGGED);
        }
        if (block instanceof IStackBlock stackBlock) {
            builder.add(stackBlock.STACK);
        }
        if (block instanceof ILitBlock litBlock) {
            builder.add(litBlock.LIT);
        }
        if (block instanceof INoLimitAgeBlock nlAgeBlock) {
            builder.add(nlAgeBlock.AGE);
        }
        if (block instanceof IBedPartBlock bedPartBlock) {
            builder.add(bedPartBlock.PART);
        }
        if (block instanceof IDoubleBlock doubleBlock) {
            builder.add(doubleBlock.HALF);
        }
        if (block instanceof IMultilayerBlock) {
            builder.add(IMultilayerBlock.LAYER);
        }
        return builder;
    }

    @Inject(method = "getStateForPlacement", at = @At("HEAD"), cancellable = true)
    public void getStateForPlacement(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        BlockState state = this.defaultBlockState();
        Direction direction = context.getHorizontalDirection();
        BlockPos blockpos = context.getClickedPos();
        BlockPos blockpos1 = blockpos.relative(direction);
        Level level = context.getLevel();
        if (block instanceof IHorizontalFacingBlock facingBlock) {
            state = state.setValue(facingBlock.FACING, context.getHorizontalDirection().getOpposite());
        }
        if (block instanceof IWaterLoggedBlock waterBlock) {
            FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
            state = state.setValue(waterBlock.WATERLOGGED, fluid.getType() == Fluids.WATER);
        }
        if (block instanceof IBedPartBlock) {
            state = level.getBlockState(blockpos1).canBeReplaced(context) && level.getWorldBorder().isWithinBounds(blockpos1) ? state.setValue(IHorizontalFacingBlock.FACING, direction) : null;
        }
        cir.setReturnValue(state);
    }

    /**
     * @return 更新水流、双方块两部分绑定
     */
    @Override
    public BlockState updateShape(BlockState stateIn, Direction direction, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (block instanceof IWaterLoggedBlock waterBlock) {
            if (stateIn.getValue(waterBlock.WATERLOGGED)) {
                level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
            }
        }
        if (block instanceof IBedPartBlock partBlock) {
            if (direction == partBlock.getNeighbourDirection(stateIn.getValue(IBedPartBlock.PART), stateIn.getValue(IHorizontalFacingBlock.FACING))) {
                return facingState.is(block) && facingState.getValue(IBedPartBlock.PART) != stateIn.getValue(IBedPartBlock.PART) ? stateIn : Blocks.AIR.defaultBlockState();
            }
        }
        return super.updateShape(stateIn, direction, facingState, level, currentPos, facingPos);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        if (block instanceof IWaterLoggedBlock waterBlock) {
            return state.getValue(waterBlock.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
        }
        return super.getFluidState(state);
    }

    /**
     * 设置转向
     */
    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        if (block instanceof IHorizontalFacingBlock facingBlock) {
            return super.rotate(state, rot).setValue(facingBlock.FACING, rot.rotate(state.getValue(facingBlock.FACING)));
        }
        return super.rotate(state, rot);
    }

    /**
     * 设置镜像
     */
    @Override
    public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        if (block instanceof IHorizontalFacingBlock facingBlock) {
            return super.mirror(state, mirror).rotate(mirror.getRotation(state.getValue(facingBlock.FACING)));
        }
        return super.mirror(state, mirror);
    }

    /**
     * 创造模式下一起破坏bedPartBlock的连接方块
     */
    @Inject(method = "playerWillDestroy", at = @At("HEAD"))
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player, CallbackInfo ci) {
        if (block instanceof IBedPartBlock bedPartBlock) {
            if (!level.isClientSide && player.isCreative()) {
                BedPart bedpart = state.getValue(IBedPartBlock.PART);
                if (bedpart == BedPart.FOOT) {
                    BlockPos blockpos = pos.relative(bedPartBlock.getNeighbourDirection(bedpart, state.getValue(IHorizontalFacingBlock.FACING)));
                    BlockState blockstate = level.getBlockState(blockpos);
                    if (blockstate.is(block) && blockstate.getValue(IBedPartBlock.PART) == BedPart.HEAD) {
                        level.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 35);
                        level.levelEvent(player, 2001, blockpos, Block.getId(blockstate));
                    }
                }
            }
        }
    }

    @Inject(method = "setPlacedBy", at = @At("HEAD"))
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity living, ItemStack stack, CallbackInfo ci) {
        if (block instanceof IBedPartBlock) {
            BlockPos facingPos = pos.relative(state.getValue(IBedPartBlock.FACING));
            level.setBlock(facingPos, state.setValue(IBedPartBlock.PART, BedPart.HEAD), 3);
            level.blockUpdated(pos, Blocks.AIR);
            state.updateNeighbourShapes(level, pos, 3);
        }
    }

}
