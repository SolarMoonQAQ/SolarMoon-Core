package cn.solarmoon.solarmoon_core.api.block_util;

import cn.solarmoon.solarmoon_core.SolarMoonCore;
import cn.solarmoon.solarmoon_core.api.blockstate_access.IBedPartBlock;
import cn.solarmoon.solarmoon_core.api.util.HitResultUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.Property;

import static cn.solarmoon.solarmoon_core.api.blockstate_access.IBedPartBlock.PART;
import static cn.solarmoon.solarmoon_core.api.blockstate_access.IHorizontalFacingBlock.FACING;

public class BlockUtil {

    /**
     * 使得设置的方块具有原位方块的方向属性
     */
    public static boolean replaceBlockWithDirection(BlockState originState, BlockState state, Level level, BlockPos pos) {
        if (state.getValues().get(FACING) != null && originState.getValues().get(FACING) != null) {
            level.setBlock(pos, state.setValue(FACING, originState.getValue(FACING)), 3);
            return true;
        } else {
            level.setBlock(pos, state, 3);
            return false;
        }
    }

    /**
     * 完美继承上一个方块的所有可以继承的属性
     */
    public static BlockState inheritBlockWithAllState(BlockState stateToBeInherited, BlockState stateToSet) {
        var values = stateToBeInherited.getValues();
        for (var entry : values.entrySet()) {
            Property key = entry.getKey();
            if (stateToSet.getValues().get(key) != null) {
                Comparable value = entry.getValue();
                stateToSet = stateToSet.setValue(key, value);
            }
        }
        return stateToSet;
    }

    /**
     * 完美继承上一个方块的所有可以继承的属性并替换
     */
    // 记得测试一下双方块换为单方块还会不会掉落本体
    public static void replaceBlockWithAllState(BlockState stateToBeInherited, BlockState stateToSet, Level level, BlockPos pos) {
        var values = stateToBeInherited.getValues();
        for (var entry : values.entrySet()) {
            Property key = entry.getKey();
            if (stateToSet.getValues().get(key) != null) {
                Comparable value = entry.getValue();
                stateToSet = stateToSet.setValue(key, value);
            }
        }
        if (stateToSet.getValues().get(PART) != null && values.get(PART) != null) replaceBedPartBlock(stateToBeInherited, stateToSet, level, pos);
        else if (values.get(PART) != null && stateToSet.getValues().get(PART) == null) {
            removeDoubleBlock(level, pos);
            level.setBlock(pos, stateToSet, 3);
            SolarMoonCore.DEBUG.send("已替换双方块至单方块");
        }
        else {
            level.setBlock(pos, stateToSet, 3);
            SolarMoonCore.DEBUG.send("已替换单方块");
        }
    }

    /**
     * 快速从一个双方块替换为另一个双方块<br/>
     * 无需检测是否是双方块，如果不是的话什么也不会触发
     */
    public static boolean replaceBedPartBlock(BlockState originState, BlockState stateTo, Level level, BlockPos pos) {
        BedPart part = originState.getValue(PART);
        if (stateTo.getBlock() instanceof IBedPartBlock partBlock) {
            Direction direction = partBlock.getNeighbourDirection(part, originState.getValue(FACING));
            stateTo = stateTo.setValue(partBlock.PART, part).setValue(FACING, originState.getValue(FACING));
            BlockState stateTo2;
            if (part == BedPart.FOOT) {
                stateTo2 = stateTo.setValue(PART, BedPart.HEAD);
            } else stateTo2 = stateTo.setValue(PART, BedPart.FOOT);
            level.setBlock(pos, stateTo, 18);
            level.setBlock(pos.relative(direction), stateTo2, 18);
            level.updateNeighborsAt(pos, stateTo.getBlock());
            level.updateNeighborsAt(pos.relative(direction), stateTo.getBlock());
            SolarMoonCore.DEBUG.send("已替换双方块至双方块");
            return true;
        }
        return false;
    }

    /**
     * 移除平面双方块
     */
    public static boolean removeDoubleBlock(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof IBedPartBlock partBlock) {
            Direction direction = partBlock.getNeighbourDirection(state.getValue(PART), state.getValue(FACING));
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 18);
            level.setBlock(pos.relative(direction), Blocks.AIR.defaultBlockState(), 18);
            level.updateNeighborsAt(pos, Blocks.AIR);
            level.updateNeighborsAt(pos.relative(direction), Blocks.AIR);
            return true;
        }
        return false;
    }

    /**
     * 把方块快速拿到空手里
     */
    public boolean getThis(Player player, Level level, BlockPos pos, BlockState state, InteractionHand hand, boolean needCrouching, boolean defaultSound) {
        ItemStack heldItem = player.getItemInHand(hand);
        if(hand.equals(InteractionHand.MAIN_HAND) && heldItem.isEmpty() && (!needCrouching || player.isCrouching())) {
            ItemStack copy = state.getCloneItemStack(HitResultUtil.getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE), level, pos, player);
            boolean flag = BlockUtil.removeDoubleBlock(level, pos);
            if (!flag) level.removeBlock(pos, false);
            if (defaultSound) {
                level.playSound(player, pos, SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.PLAYERS, 1F, 1F);
            }
            player.setItemInHand(hand, copy);
            return true;
        }
        return false;
    }

}
