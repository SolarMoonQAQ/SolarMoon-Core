package cn.solarmoon.solarmoon_core.api.blockstate_access;

import cn.solarmoon.solarmoon_core.api.util.DropUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * 全自动添加lit属性
 */
public interface ILitBlock {

    BooleanProperty LIT = BlockStateProperties.LIT;

    default boolean defaultLitValue() {
        return false;
    }

    static int getCommonLightLevel(BlockState state) {
        if (state.getValues().get(LIT) != null) {
            return state.getValue(LIT) ? 13 : 0;
        }
        return 0;
    }

    /**
     * 打火石手动点燃
     * @return 成功返回true
     */
    static boolean litByHand(BlockState state, BlockPos pos, Level level, Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        //打火石等点燃
        if (!state.getValue(LIT)) {
            if (heldItem.getItem() instanceof FlintAndSteelItem) {
                level.playSound(player, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, player.getRandom().nextFloat() * 0.4F + 0.8F);
                level.setBlock(pos, state.setValue(BlockStateProperties.LIT, Boolean.TRUE), 11);
                heldItem.hurtAndBreak(1, player, action -> action.broadcastBreakEvent(hand));
                return true;
            }
        }
        return false;
    }

    static boolean extinguishByHand(BlockState state, BlockPos pos, Level level, Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (!state.getValue(LIT)) return false;
        if (heldItem.is(Items.POTION) && PotionUtils.getPotion(heldItem) == Potions.WATER) {
            if (!player.isCreative()) {
                heldItem.shrink(1);
                DropUtil.addItemToInventory(player, new ItemStack(Items.GLASS_BOTTLE));
            }
            level.setBlock(pos, state.setValue(LIT, false), 3);
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS);
            return true;
        }
        if (heldItem.is(Items.WATER_BUCKET)) {
            if (!player.isCreative()) {
                heldItem.shrink(1);
                DropUtil.addItemToInventory(player, new ItemStack(Items.BUCKET));
            }
            level.setBlock(pos, state.setValue(LIT, false), 3);
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS);
            return true;
        }
        if (heldItem.is(ItemTags.SHOVELS)) {
            heldItem.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            level.setBlock(pos, state.setValue(LIT, false), 3);
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS);
            return true;
        }
        return false;
    }

    static boolean controlLitByHand(BlockState state, BlockPos pos, Level level, Player player, InteractionHand hand) {
        return litByHand(state, pos, level, player, hand) || extinguishByHand(state, pos, level, player, hand);
    }

}
