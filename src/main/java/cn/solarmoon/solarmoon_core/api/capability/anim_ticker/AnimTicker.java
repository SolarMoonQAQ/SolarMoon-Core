package cn.solarmoon.solarmoon_core.api.capability.anim_ticker;

import cn.solarmoon.solarmoon_core.network.NETList;
import cn.solarmoon.solarmoon_core.registry.common.SolarNetPacks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;

public class AnimTicker implements INBTSerializable<CompoundTag> {

    private int index;
    private boolean startOnChanged = true;
    private final BlockEntity blockEntity;
    private boolean enabled;
    private int ticks;
    private int factor;
    private int maxTick;
    private float fixedValue;
    private FluidStack fixedFluid;

    public AnimTicker(int index, BlockEntity blockEntity) {
        this.index = index;
        this.blockEntity = blockEntity;
        factor = 1;
        maxTick = 100;
        fixedFluid = FluidStack.EMPTY;
    }

    public void setStartOnChanged(boolean startOnChanged) {
        Level level = blockEntity.getLevel();
        if (level != null && level.isClientSide) {
            SolarNetPacks.SERVER.getSender()
                    .pos(blockEntity.getBlockPos())
                    .flag(startOnChanged)
                    .i(index)
                    .send(NETList.SYNC_ANIM_CHANGED);
        }
        this.startOnChanged = startOnChanged;
    }

    public int getTicks() {
        return ticks;
    }

    public void setTicks(int ticks) {
        Level level = blockEntity.getLevel();
        if (level != null && level.isClientSide) {
            SolarNetPacks.SERVER.getSender()
                    .pos(blockEntity.getBlockPos())
                    .f(ticks)
                    .i(index)
                    .send(NETList.SYNC_ANIM_TICK);
        }
        this.ticks = ticks;
    }

    public void setFactor(int factor) {
        Level level = blockEntity.getLevel();
        if (level != null && level.isClientSide) {
            SolarNetPacks.SERVER.getSender()
                    .pos(blockEntity.getBlockPos())
                    .f(factor)
                    .i(index)
                    .send(NETList.SYNC_ANIM_FACTOR);
        }
        this.factor = factor;
    }

    public int getFactor() {
        return factor;
    }

    public void setMaxTick(int maxTick) {
        Level level = blockEntity.getLevel();
        if (level != null && level.isClientSide) {
            SolarNetPacks.SERVER.getSender()
                    .pos(blockEntity.getBlockPos())
                    .f(maxTick)
                    .i(index)
                    .send(NETList.SYNC_ANIM_MAX_TICK);
        }
        this.maxTick = maxTick;
    }

    public int getMaxTick() {
        return maxTick;
    }

    public float getFixedValue() {
        return fixedValue;
    }

    public void setFixedValue(float fixedValue) {
        Level level = blockEntity.getLevel();
        if (level != null && level.isClientSide) {
            SolarNetPacks.SERVER.getSender()
                    .pos(blockEntity.getBlockPos())
                    .f(fixedValue)
                    .i(index)
                    .send(NETList.SYNC_ANIM_FIXED_TICK);
        }
        this.fixedValue = fixedValue;
    }

    public FluidStack getFixedFluid() {
        return fixedFluid;
    }

    public void setFixedFluid(FluidStack fixedFluid) {
        Level level = blockEntity.getLevel();
        if (level != null && level.isClientSide) {
            SolarNetPacks.SERVER.getSender()
                    .pos(blockEntity.getBlockPos())
                    .fluidStack(fixedFluid)
                    .i(index)
                    .send(NETList.SYNC_ANIM_FLUID);
        }
        this.fixedFluid = fixedFluid;
    }

    public void tick() {
        if (enabled) {
            if (ticks < maxTick) ticks++;
            else {
                ticks = 0;
                enabled = false;
            }
            if (!startOnChanged) blockEntity.setChanged();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        Level level = blockEntity.getLevel();
        if (level != null && level.isClientSide) {
            SolarNetPacks.SERVER.getSender()
                    .pos(blockEntity.getBlockPos())
                    .flag(enabled)
                    .i(index)
                    .send(NETList.SYNC_ANIM_ENABLED);
        }
        this.enabled = enabled;
    }

    /**
     * 开启后，将从0tick到maxTick，然后关闭，回到0tick
     */
    public void start() {
        setEnabled(true);
        setTicks(0);
    }

    public void stop() {
        setEnabled(false);
        setTicks(0);
    }

    public boolean canStartOnChanged() {
        return startOnChanged;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Index", index);
        tag.putBoolean("StartOnChanged", startOnChanged);
        tag.putBoolean("Enabled", enabled);
        tag.putInt("Ticks", ticks);
        tag.putInt("MaxTick", maxTick);
        tag.putFloat("FixedValue", fixedValue);
        tag.put("FixedFluid", fixedFluid.writeToNBT(new CompoundTag()));
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        index = nbt.getInt("Index");
        startOnChanged = nbt.getBoolean("StartOnChanged");
        enabled = nbt.getBoolean("Enabled");
        ticks = nbt.getInt("Ticks");
        maxTick = nbt.getInt("MaxTick");
        fixedValue = nbt.getFloat("FixedValue");
        fixedFluid = FluidStack.loadFluidStackFromNBT(nbt.getCompound("FixedFluid"));
    }

}
