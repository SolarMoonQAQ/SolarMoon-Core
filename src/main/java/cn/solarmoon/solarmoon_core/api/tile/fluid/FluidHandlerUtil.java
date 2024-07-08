package cn.solarmoon.solarmoon_core.api.tile.fluid;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

/**
 * 主要用于对流体tank类进行处理
 */
public class FluidHandlerUtil {

    public static final String FLUID = "Fluid";

    /**
     * 从手中物品放入液体
     * @return 成功会返回true
     */
    public static boolean putFluid(IFluidHandler tank, Player player, InteractionHand hand, boolean playSound) {
        ItemStack heldItem = player.getItemInHand(hand);
        FluidActionResult result = FluidUtil.tryEmptyContainer(heldItem, tank, Integer.MAX_VALUE, playSound ? player : null, true);
        if (result.isSuccess()) {
            if (!player.isCreative()) player.setItemInHand(hand, result.getResult());
            return true;
        }
        return false;
    }

    /**
     * 从手中物品拿取液体
     * @return 成功会返回true
     */
    public static boolean takeFluid(IFluidHandler tank, Player player, InteractionHand hand, boolean playSound) {
        ItemStack heldItem = player.getItemInHand(hand);
        FluidActionResult result = FluidUtil.tryFillContainer(heldItem, tank, Integer.MAX_VALUE, playSound ? player : null, true);
        if (result.isSuccess()) {
            if (!player.isCreative()) player.setItemInHand(hand, result.getResult());
            return true;
        }
        return false;
    }

    /**
     * @return 无所谓装取，只要液体交互成功就返回true
     */
    public static boolean loadFluid(IFluidHandler tank, Player player, InteractionHand hand, boolean playSound) {
        return putFluid(tank, player, hand, playSound) || takeFluid(tank, player, hand, playSound);
    }

    public static void clearTank(IFluidHandler tank) {
        if (tank instanceof FluidTank t) t.setFluid(FluidStack.EMPTY);
        else setTank(tank, FluidStack.EMPTY);
    }

    /**
     * 根据液体容量获取大小百分比
     */
    public static float getScale(IFluidHandler tank) {
        int stored = tank.getFluidInTank(0).getAmount();
        int capacity = tank.getTankCapacity(0);
        return (float) stored / capacity;
    }

    /**
     * 用于强制设置物品里的液体（前者是被设置的，后者是设置的内容）
     */
    public static void setTank(IFluidHandler tank, FluidStack fluidStack) {
        if (tank instanceof FluidTank t) t.setFluid(fluidStack);
        else {
            tank.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
            tank.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
        }
    }

    /**
     * 一个根据tag强制设置储罐内容物的方法
     */
    public static void setFluid(FluidTank tank, CompoundTag tag) {
        tank.readFromNBT(tag.getCompound(FluidHandlerItemStack.FLUID_NBT_KEY));
    }

    /**
     * 检查两个流体栈是否完全匹配（包括数量）
     */
    public static boolean isMatch(FluidStack fluid1, FluidStack fluid2, boolean compareAmount, boolean compareNBT) {
        boolean amountMatch = !compareAmount || fluid1.getAmount() == fluid2.getAmount();
        boolean NBTMatch = !compareNBT || fluid1.getTag().equals(fluid2.getTag());
        boolean typeMatch = fluid1.getFluid() == fluid2.getFluid();
        return typeMatch && amountMatch && NBTMatch;
    }

    /**
     * 检查是否还能放入液体
     * 规则为已有液体必须相匹配，且剩余空间大于等于要放入的液体（或者为空）
     * 相反的检查可以用contains
     */
    public static boolean canStillPut(IFluidHandler tankL, FluidStack fluidStack) {
        int remain = tankL.getTankCapacity(0) - tankL.getFluidInTank(0).getAmount();
        int put = fluidStack.getAmount();
        boolean match = tankL.getFluidInTank(0).equals(fluidStack);
        return remain >= put && (match || tankL.getFluidInTank(0).isEmpty());
    }

}
