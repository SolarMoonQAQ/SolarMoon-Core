package cn.solarmoon.solarmoon_core.api.blockstate_access;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public interface IMultilayerBlock {

    IntegerProperty LAYER = IntegerProperty.create("layer", 0, 64);

    /**
     * @param pos 当前层的坐标
     * @param connection 检测层的坐标
     * @return 检测层是否有层数，且层数满足当前层和检测层y轴之差（也就是层数是否连续），且方块类型一致
     */
    static boolean presenceOfConnection(Level level, BlockPos pos, BlockPos connection) {
        int deltaY = connection.getY() - pos.getY();
        BlockState th = level.getBlockState(pos);
        BlockState tt = level.getBlockState(connection);
        if (tt.getValues().get(LAYER) == null) return false;
        int layerTh = th.getValue(LAYER);
        int layerTT = tt.getValue(LAYER);
        return layerTT - layerTh == deltaY && th.is(tt.getBlock());
    }

    /**
     * @param pos 当前层的坐标
     * @param connection 检测层的坐标
     * @return 检测层是否有层数，且层数满足当前层和检测层y轴之差（也就是层数是否连续）,且方块类型一致
     */
    static boolean presenceOfConnection(BlockGetter level, BlockPos pos, BlockPos connection) {
        int deltaY = connection.getY() - pos.getY();
        BlockState th = level.getBlockState(pos);
        BlockState tt = level.getBlockState(connection);
        if (tt.getValues().get(LAYER) == null) return false;
        int layerTh = th.getValue(LAYER);
        int layerTT = tt.getValue(LAYER);
        return layerTT - layerTh == deltaY && th.is(tt.getBlock());
    }

    int getMaxLayer();

}
