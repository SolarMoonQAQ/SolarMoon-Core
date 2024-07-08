package cn.solarmoon.solarmoon_core.api.tile.inventory;

import cn.solarmoon.solarmoon_core.api.util.DropUtil;
import cn.solarmoon.solarmoon_core.network.NETList;
import cn.solarmoon.solarmoon_core.registry.common.SolarNetPacks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

public class ItemHandlerUtil {

    public static final String INVENTORY = "Inventory";

    /**
     * 插入容纳的物品（按物品栈插入）<br/>
     * 逻辑为从第一格开始尝试插入直到插入成功<br/>
     * 会返回计算消耗后的物品栈，并不会消耗物品，因此不要再用shrink！用setItem！<br/>
     * <b>别忘了setChanged！</b>
     */
    public static ItemStack insertItem(IItemHandler inv, ItemStack itemStack) {
        return insertItem(inv, itemStack, 0, inv.getSlots() - 1);
    }

    /**
     * 插入容纳的物品（按物品栈插入）<br/>
     * 逻辑为从第一格开始尝试插入直到插入成功<br/>
     * 会返回计算消耗后的物品栈，并不会消耗物品，因此不要再用shrink！用setItem！<br/>
     * <b>别忘了setChanged！</b>
     */
    public static ItemStack insertItem(IItemHandler inv, ItemStack itemStack, int slotMin, int slotMax) {
        ItemStack result = itemStack;
        for (int i = slotMin; i <= slotMax; i++) {
            result = inv.insertItem(i, itemStack, false);
            if (!result.equals(itemStack, false)) break;
        }
        return result;
    }

    /**
     * 从中提取物品<br/>
     * 默认逻辑从最后一栏开始提取，按物品栈提取，没提取会返回空栈<br/>
     */
    public static ItemStack extractItem(IItemHandler inv, int count, int slotMin, int slotMax) {
        ItemStack stack = ItemStack.EMPTY;
        for(int i = slotMax; i >= slotMin; --i) {
            stack = inv.extractItem(i, count, false);
            if (!stack.isEmpty()) {
                break;
            }
        }
        return stack;
    }

    /**
     * 从中提取物品<br/>
     * 默认逻辑从最后一栏开始提取，按物品栈提取，没提取会返回空栈<br/>
     */
    public static ItemStack extractItem(IItemHandler inv, int count) {
        return extractItem(inv, count, 0, inv.getSlots() - 1);
    }

    /**
     * 从tag中读取inventory信息
     */
    public static void setInventory(INBTSerializable<CompoundTag> invToBeSet, CompoundTag set) {
        invToBeSet.deserializeNBT(set.getCompound(INVENTORY));
    }

    /**
     * 获取容器内的所有物品
     */
    public static List<ItemStack> getStacks(IItemHandler inv) {
        List<ItemStack> stacks = new ArrayList<>();
        int maxSlots = inv.getSlots();
        for (int i = 0; i < maxSlots; i++) {
            ItemStack stack = inv.getStackInSlot(i);
            //这里不能让stack为空，因为会插入EMPTY的stack，这样会妨碍List.isEmpty的检查
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
        return stacks;
    }

    /**
     * 获取容器内物品总数
     */
    public static int getStacksAmount(IItemHandler inv) {
        int amount = 0;
        int maxSlots = inv.getSlots();
        for (int i = 0; i < maxSlots; i++) {
            amount += inv.getStackInSlot(i).getCount();
        }
        return amount;
    }

    /**
     * 获取容器的最大可容纳物品量
     */
    public static int getItemCapability(IItemHandler inv) {
        int total = 0;
        for (int i = 0; i < inv.getSlots(); i++) {
            total += inv.getSlotLimit(i);
        }
        return total;
    }

    /**
     * @return 获取容器当前物品和最大容积之比
     */
    public static float getScale(IItemHandler inv) {
        return (float) ItemHandlerUtil.getStacksAmount(inv) / ItemHandlerUtil.getItemCapability(inv);
    }

    /**
     * 单独放入玩家手中物品
     * @return 成功返回true
     */
    public static boolean putItem(IItemHandler inv, Player player, InteractionHand hand, int count, int slotMin, int slotMax) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (!heldItem.isEmpty()) {
            ItemStack simulativeItem = heldItem.copyWithCount(count);
            ItemStack result = insertItem(inv, simulativeItem, slotMin, slotMax);
            int countToShrink = count - result.getCount();
            if (!player.isCreative()) heldItem.shrink(countToShrink);
            return !result.equals(simulativeItem);
        }
        return false;
    }

    /**
     * 单独放入玩家手中物品
     * @return 成功返回true
     */
    public static boolean putItem(IItemHandler inv, Player player, InteractionHand hand, int count) {
        return putItem(inv, player, hand, count, 0, inv.getSlots() - 1);
    }

    /**
     * 玩家用手单独拿取物品（只适用空手拿取），超过的部分不会提取，放心使用
     * @return 成功返回true
     */
    public static boolean takeItem(IItemHandler inv, Player player, InteractionHand hand, int count, int slotMin, int slotMax) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.isEmpty() && !getStacks(inv).isEmpty()) {
            ItemStack result = extractItem(inv, count, slotMin, slotMax);
            if (!result.isEmpty()) {
                if (!player.isCreative()) DropUtil.addItemToInventory(player, result);
                return true;
            }
        }
        return false;
    }

    /**
     * 玩家用手单独拿取物品（只适用空手拿取），超过的部分不会提取，放心使用
     * @return 成功返回true
     */
    public static boolean takeItem(IItemHandler inv, Player player, InteractionHand hand, int count) {
        return takeItem(inv, player, hand, count, 0, inv.getSlots() - 1);
    }

    /**
     * 基本的存物逻辑，似乎可通用<br/>
     * 手不为空时存入，为空时疯狂取出<br/>
     * @return 无所谓装取，只要容器交互成功就返回true
     */
    public static boolean storage(IItemHandler inv, Player player, InteractionHand hand, int putCount, int takeCount, int slotMin, int slotMax) {
        if (putItem(inv, player, hand, putCount, slotMin, slotMax)) return true;
        return takeItem(inv, player, hand, takeCount, slotMin, slotMax);
    }

    /**
     * 基本的存物逻辑，似乎可通用<br/>
     * 手不为空时存入，为空时疯狂取出<br/>
     * @return 无所谓装取，只要容器交互成功就返回true
     */
    public static boolean storage(IItemHandler inv, Player player, InteractionHand hand, int putCount, int takeCount) {
        if (putItem(inv, player, hand, putCount, 0, inv.getSlots() - 1)) return true;
        return takeItem(inv, player, hand, takeCount, 0, inv.getSlots() - 1);
    }

    /**
     * 特殊的取物逻辑，玩家蹲下时存入手中全部物品，站立时存入一个，取出同理<br/>
     * 但要注意必须实现IBlockUseCaller接口，否则默认情况下将不调用蹲下后的逻辑
     * @return 成功返回true
     */
    public static boolean specialStorage(IItemHandler inv, Player player, InteractionHand hand, int slotMin, int slotMax) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (!heldItem.isEmpty()) {
            if (player.isCrouching()) {
                return putItem(inv, player, hand, heldItem.getCount(), slotMin, slotMax);
            } else {
                return putItem(inv, player, hand, 1, slotMin, slotMax);
            }
        } else {
            return player.isCrouching() ?
                    takeItem(inv, player, hand, 64, slotMin, slotMax) :
                    takeItem(inv, player, hand, 1, slotMin, slotMax);
        }
    }

    /**
     * 特殊的取物逻辑，玩家蹲下时存入手中全部物品，站立时存入一个，取出同理<br/>
     * 但要注意必须实现IBlockUseCaller接口，否则默认情况下将不调用蹲下后的逻辑
     * @return 成功返回true
     */
    public static boolean specialStorage(IItemHandler inv, Player player, InteractionHand hand) {
        return specialStorage(inv, player, hand, 0, inv.getSlots() - 1);
    }

    public static void clearInv(IItemHandler inv) {
        getStacks(inv).forEach(stack -> stack.setCount(0));
    }

    public static void clearInv(IItemHandler inv, BlockEntity blockEntity) {
        clearInv(inv);
        blockEntity.setChanged();
    }

    /**
     * 泵出所有物品
     */
    public static void pumpOutAllItems(IItemHandler inv, BlockEntity blockEntity, Vec3 positionAddon) {
        if (blockEntity.getLevel() != null && blockEntity.getLevel().isClientSide) {
            SolarNetPacks.SERVER.getSender()
                    .pos(blockEntity.getBlockPos())
                    .stacks(getStacks(inv))
                    .vec3List(List.of(positionAddon))
                    .send(NETList.PUMP);
        }
        clearInv(inv, blockEntity);
    }

}
