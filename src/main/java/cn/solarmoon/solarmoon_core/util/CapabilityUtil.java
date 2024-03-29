package cn.solarmoon.solarmoon_core.util;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;

public class CapabilityUtil {

    public static <T> T getData(ICapabilityProvider provider, Capability<T> cap) {
        return getCapability(provider, cap).orElse(null);
    }

    @Nonnull
    public static <T> LazyOptional<T> getCapability(ICapabilityProvider provider, Capability<T> cap) {
        if (provider == null || cap == null) {
            return LazyOptional.empty();
        }
        return provider.getCapability(cap);
    }

    @Nonnull
    public static <T> LazyOptional<T> getCapability(ICapabilityProvider provider, Capability<T> cap, Direction side) {
        if (provider == null || cap == null) {
            return LazyOptional.empty();
        }
        return provider.getCapability(cap, side);
    }

}
