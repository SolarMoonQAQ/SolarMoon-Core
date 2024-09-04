package cn.solarmoon.solarmoon_core.api.tile;

import cn.solarmoon.solarmoon_core.api.event.BlockEntityDataEvent;
import cn.solarmoon.solarmoon_core.api.tile.fluid.IMultiTankTile;
import cn.solarmoon.solarmoon_core.api.tile.fluid.ITankTile;
import cn.solarmoon.solarmoon_core.api.tile.inventory.IContainerTile;
import cn.solarmoon.solarmoon_core.api.tile.inventory.ItemHandlerUtil;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class BlockEntityDataHolder {

    @SubscribeEvent
    public void save(BlockEntityDataEvent.Save event) {
        BlockEntity be = event.getBlockEntity();
        CompoundTag tag = event.getTag();
        if (be instanceof IContainerTile c) {
            tag.put(ItemHandlerUtil.INVENTORY, c.getInventory().serializeNBT());
        }
        if (be instanceof ITankTile t) {
            CompoundTag fluid = new CompoundTag();
            t.getTank().writeToNBT(fluid);
            tag.put(FluidHandlerItemStack.FLUID_NBT_KEY, fluid);
        }
        if (be instanceof IMultiTankTile<?> mt) {
            ListTag listTag = new ListTag();
            mt.getTanks().forEach(tank -> {
                CompoundTag fluid = new CompoundTag();
                tank.writeToNBT(fluid);
                listTag.add(fluid);
            });
            tag.put(IMultiTankTile.TANKS, listTag);
        }
    }

    @SubscribeEvent
    public void load(BlockEntityDataEvent.Load event) {
        BlockEntity be = event.getBlockEntity();
        CompoundTag tag = event.getTag();
        if (be instanceof IContainerTile c) {
            c.getInventory().deserializeNBT(tag.getCompound(ItemHandlerUtil.INVENTORY));
        }
        if (be instanceof ITankTile t) {
            t.getTank().readFromNBT(tag.getCompound(FluidHandlerItemStack.FLUID_NBT_KEY));
        }
        if (be instanceof IMultiTankTile<?> mt) {
            ListTag listTag = tag.getList(IMultiTankTile.TANKS, ListTag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag tg = listTag.getCompound(i);
                FluidTank tank = mt.getTank(i);
                tank.readFromNBT(tg);
            }
        }
    }

    @SubscribeEvent
    public void capability(BlockEntityDataEvent.Capability event) {
        BlockEntity be = event.getBlockEntity();
        var cap = event.getCap();
        Direction side = event.getSide();
        if (be instanceof IContainerTile c) {
            if (cap == ForgeCapabilities.ITEM_HANDLER) {
                event.setReturnValue(LazyOptional.of(c::getInventory));
            }
        }
        if (be instanceof ITankTile t) {
            if (cap == ForgeCapabilities.FLUID_HANDLER) {
                event.setReturnValue(LazyOptional.of(t::getTank));
            }
        }
        if (be instanceof IMultiTankTile<?> mt) {
            if (cap == ForgeCapabilities.FLUID_HANDLER) {
                event.setReturnValue(LazyOptional.of(() -> mt.tankCapabilityProvider(side)));
            }
        }
    }

}
