package cn.solarmoon.solarmoon_core.api.blockstate_access;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * 像床一样的平铺方向的双格方块，此类方块必须具有FACING属性！<br/>
 * 同时需要注意，双方块本质上是两个方块，因此如果在双方块上加blockentity，实际上是加了两个blockentity，这在诸如setplace时亟需注意，因为setplace时
 * 往往只对一个pos进行操作，而容易忽略另一个pos。
 */
public interface IBedPartBlock extends IHorizontalFacingBlock {

    EnumProperty<BedPart> PART = BlockStateProperties.BED_PART;

    default Direction getNeighbourDirection(BedPart part, Direction direction) {
        return part == BedPart.FOOT ? direction : direction.getOpposite();
    }

    default BlockPos getFootPos(BlockState state, BlockPos pos) {
        Direction nb = getNeighbourDirection(state.getValue(PART), state.getValue(FACING));
        return state.getValue(PART) == BedPart.FOOT ? pos : pos.relative(nb);
    }

}
