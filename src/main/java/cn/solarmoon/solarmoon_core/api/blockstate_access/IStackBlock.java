package cn.solarmoon.solarmoon_core.api.blockstate_access;

import cn.solarmoon.solarmoon_core.api.util.DropUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.List;

/**
 * 一个基于物品栈属性的接口，本身只是注册一个属性，但也提供了快速的stack默认方法
 */
public interface IStackBlock {

    IntegerProperty STACK = IntegerProperty.create("stack", 1, 64);

    /**
     * 用于use的默认方法，可以拿着相同方块物品使得stack+1或取出
     */
    default boolean stackUse(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        int stack = state.getValue(STACK);
        if (heldItem.is(state.getBlock().asItem()) && stack < getMaxStack()) {
            if (!player.isCreative()) {
                heldItem.shrink(1);
            }
            level.setBlock(pos, state.setValue(STACK, stack + 1), 3);
            level.playSound(null, pos, state.getSoundType().getPlaceSound(), SoundSource.PLAYERS);
            return true;
        } else if (heldItem.isEmpty() && stack > 1) {
            level.setBlock(pos, state.setValue(STACK, stack - 1), 3);
            if (!player.isCreative()) {
                DropUtil.summonDrop(state.getBlock().asItem(), level, pos, 1);
            }
            return true;
        } else if (heldItem.isEmpty() && stack == 1) {
            level.removeBlock(pos, false);
            if (!player.isCreative()) {
                DropUtil.summonDrop(state.getBlock().asItem(), level, pos, 1);
            }
            return true;
        }
        return false;
    }

    /**
     * 设置掉落物为stack的数量
     */
    default void setStackDrops(BlockState state, List<ItemStack> drops) {
        int count = state.getValue(STACK);
        for (ItemStack drop : drops) {
            if (drop.is(state.getBlock().asItem())) {
                drop.setCount(count);
                break;
            }
        }
    }

    int getMaxStack();

}
