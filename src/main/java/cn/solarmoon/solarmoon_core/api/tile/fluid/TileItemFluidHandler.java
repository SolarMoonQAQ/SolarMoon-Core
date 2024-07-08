package cn.solarmoon.solarmoon_core.api.tile.fluid;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * 用于syncedEntityBlock，读取的液体位置变为BE专用的tag位置
 */
public class TileItemFluidHandler extends FluidHandlerItemStack {

    public TileItemFluidHandler(@NotNull ItemStack container, int capacity) {
        super(container, capacity);
    }

    @NotNull
    @Override
    public FluidStack getFluid() {
        CompoundTag tagCompound = BlockItem.getBlockEntityData(container);
        if (tagCompound == null || !tagCompound.contains(FLUID_NBT_KEY)) {
            return FluidStack.EMPTY;
        }
        return FluidStack.loadFluidStackFromNBT(tagCompound.getCompound(FLUID_NBT_KEY));
    }

    @Override
    public void setFluid(FluidStack fluid) {
        CompoundTag fluidTag = new CompoundTag();
        fluid.writeToNBT(fluidTag);
        container.getOrCreateTagElement(BlockItem.BLOCK_ENTITY_TAG).put(FLUID_NBT_KEY, fluidTag);
    }

    @Override
    public void setContainerToEmpty() {
        container.getOrCreateTagElement(BlockItem.BLOCK_ENTITY_TAG).remove(FLUID_NBT_KEY);
    }

}
