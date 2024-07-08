package cn.solarmoon.solarmoon_core.api.tile.fluid;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

/**
 * 具有液体储罐信息的方块实体<br/>
 * 接入后能够实现save - load容器信息、 附加forgeItemHandler能力，以及一些实用方法<br/>
 * 需手动实现tank逻辑，一般直接新建一个FluidTank即可
 */
public interface ITankTile {

    FluidTank getTank();

}
