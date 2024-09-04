package cn.solarmoon.solarmoon_core.api.tile.fluid;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.function.Predicate;

/**
 * 自动同步并存有实体信息
 */
public class TileTank extends FluidTank {

    private final BlockEntity blockEntity;

    public TileTank(int capacity, BlockEntity blockEntity) {
        super(capacity);
        this.blockEntity = blockEntity;
    }

    public TileTank(int capacity, Predicate<FluidStack> validator, BlockEntity blockEntity) {
        super(capacity, validator);
        this.blockEntity = blockEntity;
    }

    @Override
    protected void onContentsChanged() {
        super.onContentsChanged();
        blockEntity.setChanged();
    }

    public BlockEntity getBlockEntity() {
        return blockEntity;
    }

}
