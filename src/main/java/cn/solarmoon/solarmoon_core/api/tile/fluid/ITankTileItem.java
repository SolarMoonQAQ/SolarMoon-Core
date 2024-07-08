package cn.solarmoon.solarmoon_core.api.tile.fluid;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public interface ITankTileItem {

    /**
     * @return 液体容量
     */
    int getMaxCapacity();

    /**
     * @return 默认的tank
     */
    default ICapabilityProvider initTank(ItemStack stack) {
        return new TileItemFluidHandler(stack, getMaxCapacity());
    }

}
