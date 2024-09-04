package cn.solarmoon.solarmoon_core.api.block_base;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 基本的作物类，只能种在田上（基本同小麦）
 * 继承这个和直接继承CropBlock无异，只是这个告诉你一个作物最基本需要修改的内容，相当于简化了思考步骤
 */
public abstract class BaseCropBlock extends CropBlock {

    public BaseCropBlock() {
        super(BlockBehaviour.Properties.copy(Blocks.WHEAT));
    }

    /**
     * 自定属性
     */
    public BaseCropBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected abstract ItemLike getBaseSeedId();

    @Override
    public abstract VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context);

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_52286_) {
        super.createBlockStateDefinition(p_52286_);
    }
}
