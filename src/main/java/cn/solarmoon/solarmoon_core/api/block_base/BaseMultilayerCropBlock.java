package cn.solarmoon.solarmoon_core.api.block_base;

import cn.solarmoon.solarmoon_core.api.blockstate_access.IMultilayerBlock;
import cn.solarmoon.solarmoon_core.api.blockstate_access.IWaterLoggedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;

public abstract class BaseMultilayerCropBlock extends BaseCropBlock implements IMultilayerBlock {

    public BaseMultilayerCropBlock() {
        super();
    }

    public BaseMultilayerCropBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isAreaLoaded(pos, 1)) return; // Forge: prevent loading unloaded chunks when checking neighbor's light
        if (level.getRawBrightness(pos, 0) >= 9) {
            int i = this.getAge(state);
            float f = getGrowthSpeed(this, level, pos);
            if (i < this.getMaxAge()) {
                if (ForgeHooks.onCropsGrowPre(level, pos, state, random.nextInt((int)(25.0F / f) + 1) == 0)) {
                    level.setBlock(pos, state.setValue(AGE, i + 1), 2);
                    ForgeHooks.onCropsGrowPost(level, pos, state);
                }
            } else {
                if (ForgeHooks.onCropsGrowPre(level, pos, state, random.nextInt((int)(25.0F / f) + 1) == 0)) {
                    BlockPos above = pos.above();
                    int layer = state.getValue(LAYER);
                    // 上方是空气，且不是最大层
                    if (newLayerExtraCondition(state, level, pos, random) && layer < getMaxLayer()) {
                        level.setBlock(above, calibrateState(state.setValue(LAYER, layer + 1).setValue(AGE, 0)), 2);
                        ForgeHooks.onCropsGrowPost(level, pos, state);
                    }
                }
            }
        }
    }

    /**
     * @return 生长出新层所需额外条件
     */
    public boolean newLayerExtraCondition(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.above()).isAir();
    }

    @Override
    public void growCrops(Level level, BlockPos pos, BlockState state) {
        // 先洒满阶段，阶段满了以后尝试增层
        int layer = state.getValue(LAYER);
        int age = getAge(state);
        if (age < getMaxAge()) {
            int i = this.getAge(state) + this.getBonemealAgeIncrease(level);
            int j = this.getMaxAge();
            if (i > j) {
                i = j;
            }
            level.setBlock(pos, state.setValue(AGE, i), 2);
        }
        else if (layer < getMaxLayer()) {
            level.setBlock(pos.above(), calibrateState(state.setValue(LAYER, layer + 1).setValue(AGE, 0)), 2);
        }
    }

    /**
     * @return 给需要放置结果的state处进行校准，防止在非原位放置不需要的state
     */
    public BlockState calibrateState(BlockState state) {
        if (state.getValues().get(IWaterLoggedBlock.WATERLOGGED) != null && state.getValue(IWaterLoggedBlock.WATERLOGGED)) {
            return state.setValue(IWaterLoggedBlock.WATERLOGGED, false);
        }
        return state;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos pos, BlockState state, boolean b) {
        int layer = state.getValue(LAYER);
        // 骨粉使用条件：
        // 1.不是最大层
        // 2.上层为空
        boolean canGrow = layer < getMaxLayer() && levelReader.getBlockState(pos.above()).isAir();
        return super.isValidBonemealTarget(levelReader, pos, state, b) || canGrow;
    }

    /**
     * @return 特指第一层的生存条件
     */
    public boolean canSurviveEachLayer(BlockState state, BlockGetter levelReader, BlockPos pos, int layer) {
        return true;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader levelReader, BlockPos pos) {
        int layer = state.getValue(LAYER);
        BlockState stateBelow = levelReader.getBlockState(pos.below());
        boolean base = levelReader.getRawBrightness(pos, 0) >= 8 || levelReader.canSeeSky(pos);
        // 层数不是最底层，就必须满足下一层是相同类型且连续连接
        if (layer > 0) {
            return base && IMultilayerBlock.presenceOfConnection(levelReader, pos, pos.below()) && canSurviveEachLayer(state, levelReader, pos, layer);
        }
        // 如果是最底层，那么既要能判别下一格方块的放置许可情况，也要判别自身许可条件
        return base && canSurviveEachLayer(state, levelReader, pos, layer) && mayPlaceOn(stateBelow, levelReader, pos.below());
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return super.isRandomlyTicking(state) || state.getValue(LAYER) < getMaxLayer();
    }

}
