package cn.solarmoon.solarmoon_core.api.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Random;

public class DropUtil {

    /**
     * 生成基础掉落物
     * 坐标中心位置
     * 固定概率掉落
     */
    public static void summonDrop(Item item, Level level, BlockPos pos, int min, int max) {
        Random rand = new Random();
        ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, new ItemStack(item, rand.nextInt(min,max)));
        level.addFreshEntity(drop);
    }

    /**
     * 生成基础掉落物
     * 更为精确的位置
     * 固定概率掉落
     */
    public static void summonDrop(Item item, Level level, Vec3 vec3, int origin, int bound) {
        Random rand = new Random();
        ItemEntity drop = new ItemEntity(level, vec3.x, vec3.y, vec3.z, new ItemStack(item, rand.nextInt(origin,bound)));
        level.addFreshEntity(drop);
    }

    /**
     * 生成基础掉落物
     * 坐标中心位置（不包括y）
     */
    public static void summonDrop(Item item, Level level, BlockPos pos, int amount) {
        ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, new ItemStack(item, amount));
        level.addFreshEntity(drop);
    }

    /**
     * 生成基础掉落物
     * 坐标中心位置（不包括y）
     * 附带一个自定义初速度
     */
    public static void summonDrop(ItemStack item, Level level, BlockPos pos, Vec3 movement) {
        ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, item);
        drop.setDeltaMovement(movement);
        level.addFreshEntity(drop);
    }

    /**
     * 生成基础掉落物
     * 坐标中心位置（不包括y）
     * 附带一个自定义初速度
     */
    public static void summonDrop(Item item, Level level, BlockPos pos, Vec3 movement, int amount) {
        ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, new ItemStack(item, amount));
        drop.setDeltaMovement(movement);
        level.addFreshEntity(drop);
    }

    /**
     * 生成基础掉落物
     * 坐标中心位置（不包括y）
     * 附带一个自定义初速度
     * 直接生成一整个列表的掉落物
     */
    public static void summonDrop(List<Item> items, Level level, BlockPos pos, Vec3 movement, int amount) {
        for (Item item : items) {
            ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, new ItemStack(item, amount));
            drop.setDeltaMovement(movement);
            level.addFreshEntity(drop);
        }
    }

    /**
     * 生成基础掉落物
     */
    public static void summonDrop(Item item, Level level, Vec3 vec3, int amount) {
        ItemEntity drop = new ItemEntity(level, vec3.x, vec3.y, vec3.z, new ItemStack(item, amount));
        level.addFreshEntity(drop);
    }

    /**
     * 生成基础掉落物
     * 以坐标中心为生成点
     */
    public static void summonDrop(ItemStack stack, Level level, BlockPos pos) {
        ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, stack);
        level.addFreshEntity(drop);
    }

    /**
     * 生成基础掉落物
     * 以坐标中心（不包括y）为生成点
     * 附带一个初速度
     */
    public static void summonDrop(List<ItemStack> stacks, Level level, BlockPos pos, Vec3 movement) {
        for (ItemStack stack : stacks) {
            ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, stack);
            drop.setDeltaMovement(movement);
            level.addFreshEntity(drop);
        }
    }

    /**
     * 生成基础掉落物
     * 以三维坐标为生成点（更为精细）
     */
    public static void summonDrop(ItemStack item, Level level, Vec3 vec3) {
        ItemEntity drop = new ItemEntity(level, vec3.x, vec3.y, vec3.z, item);
        level.addFreshEntity(drop);
    }

    /**
     * 生成基础掉落物
     * 以三维坐标为生成点（更为精细）
     * 直接生成一整个列表的掉落物
     */
    public static void summonDrop(List<ItemStack> stacks, Level level, Vec3 vec3) {
        for (ItemStack stack : stacks) {
            summonDrop(stack, level, vec3);
        }
    }

    /**
     * 像give一样给玩家添加物品，如果没能成功，则将物品以掉落物的形式给到玩家身边
     * Forge：ItemHandlerHelper方法一致
     */
    public static void addItemToInventory(Player player, ItemStack stack) {
        boolean result = player.addItem(stack);
        if (!result) {
            player.drop(stack, false);
        }
    }

    /**
     * 像give一样给玩家添加物品，如果没能成功，则将物品以掉落物的形式给到指定坐标（坐标中心）
     */
    public static void addItemToInventory(Player player, ItemStack stack, BlockPos pos) {
        boolean result = player.addItem(stack);
        if (!result) {
            Vec3 vec3 = new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            summonDrop(stack, player.level(), vec3);
        }
    }

    /**
     * 像give一样给玩家添加物品，如果没能成功，则将物品以掉落物的形式给到指定坐标（坐标中心）
     * 完美模式：当还剩1个物品时能够把返还的物品准确回到点击的手上（仅限于物品交互，空手交互不可用此方法）
     */
    public static void addItemToInventoryPerfectly(Player player, ItemStack add, BlockPos pos, ItemStack heldItem, InteractionHand hand) {
        if (heldItem.getCount() == 1) player.setItemInHand(hand, add);
        else addItemToInventory(player, add, pos);
    }

    /**
     * 像give一样给玩家添加物品，如果没能成功，则将物品以掉落物的形式给到玩家身边
     * 完美模式：当还剩1个物品时能够把返还的物品准确回到点击的手上（仅限于物品交互，空手交互不可用此方法）
     */
    public static void addItemToInventoryPerfectly(Player player, ItemStack add, ItemStack heldItem, InteractionHand hand) {
        if (heldItem.getCount() == 1) player.setItemInHand(hand, add);
        else addItemToInventory(player, add);
    }

}
