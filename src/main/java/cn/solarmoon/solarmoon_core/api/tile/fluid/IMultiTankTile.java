package cn.solarmoon.solarmoon_core.api.tile.fluid;

import net.minecraft.core.Direction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.List;

public interface IMultiTankTile<T extends FluidTank> {

    String TANKS = "FluidTanks";

    default T getTank(int index) {
        return getTanks().get(index);
    }

    List<T> getTanks();

    FluidTank tankCapabilityProvider(Direction side);

}
