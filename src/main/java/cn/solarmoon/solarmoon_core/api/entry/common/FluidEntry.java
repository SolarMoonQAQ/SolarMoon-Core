package cn.solarmoon.solarmoon_core.api.entry.common;

import cn.solarmoon.solarmoon_core.api.fluid_base.BaseFluidType;
import cn.solarmoon.solarmoon_core.api.fluid_base.WaterLikeFluidType;
import cn.solarmoon.solarmoon_core.api.fluid_base.SimpleFluid;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class FluidEntry {

    private final DeferredRegister<Fluid> fluidRegister;
    private final DeferredRegister<FluidType> fluidTypeRegister;
    private final DeferredRegister<Item> itemRegister;
    private final DeferredRegister<Block> blockRegister;
    private final String modId;

    private final int defaultColor;
    private String id;
    private FluidType.Properties properties;
    private Supplier<LiquidBlock> blockSupplier;
    private Supplier<FlowingFluid> stillSupplier;
    private Supplier<FlowingFluid> flowingSupplier;
    private Supplier<Item> bucketSupplier;
    private Supplier<FluidType> fluidTypeSupplier;
    private RegistryObject<LiquidBlock> fluidBlock;
    private RegistryObject<FlowingFluid> fluidFlowing;
    private RegistryObject<FlowingFluid> fluidStill;
    private RegistryObject<Item> fluidBucket;
    private RegistryObject<FluidType> fluidType;

    public FluidEntry(DeferredRegister<Fluid> fluidRegister, DeferredRegister<FluidType> fluidTypeRegister, DeferredRegister<Item> itemRegister, DeferredRegister<Block> blockRegister, String modId) {
        this.fluidRegister = fluidRegister;
        this.fluidTypeRegister = fluidTypeRegister;
        this.itemRegister = itemRegister;
        this.blockRegister = blockRegister;
        this.modId = modId;
        this.defaultColor = 0xFFFFFFFF;
    }

    public FluidEntry id(String id) {
        this.id = id;
        return this;
    }

    public FluidEntry fluidType(Supplier<FluidType> fluidTypeSupplier) {
        this.fluidTypeSupplier = fluidTypeSupplier;
        return this;
    }

    public FluidEntry properties(FluidType.Properties properties) {
        this.properties = properties;
        return this;
    }

    public FluidEntry waterLikeProperties(boolean canConvertToSource) {
        properties = WaterLikeFluidType.waterLikeProperties(canConvertToSource);
        return this;
    }

    public FluidEntry waterLike(boolean defaultUnderOverlay) {
        fluidTypeSupplier = () -> new WaterLikeFluidType(modId, id, defaultColor, defaultUnderOverlay, properties);
        return this;
    }

    public FluidEntry waterLike(boolean defaultUnderOverlay, int color) {
        fluidTypeSupplier = () -> new WaterLikeFluidType(modId, id, color, defaultUnderOverlay, properties);
        return this;
    }

    public FluidEntry base() {
        fluidTypeSupplier = () -> new BaseFluidType(modId, id, defaultColor, properties);
        return this;
    }

    public FluidEntry base(int color) {
        fluidTypeSupplier = () -> new BaseFluidType(modId, id, color, properties);
        return this;
    }

    public FluidEntry block(Supplier<LiquidBlock> blockSupplier) {
        this.blockSupplier = blockSupplier;
        return this;
    }

    public FluidEntry still(Supplier<FlowingFluid> stillSupplier) {
        this.stillSupplier = stillSupplier;
        return this;
    }

    public FluidEntry flowing(Supplier<FlowingFluid> flowingSupplier) {
        this.flowingSupplier = flowingSupplier;
        return this;
    }

    public FluidEntry bucket(Supplier<Item> bucketSupplier) {
        this.bucketSupplier = bucketSupplier;
        return this;
    }

    public FluidEntry simple(Supplier<SimpleFluid> simpleFluidSupplier, boolean hasBucket) {
        block(() -> simpleFluidSupplier.get().getBlock());
        still(() -> simpleFluidSupplier.get().getSource());
        flowing(() -> simpleFluidSupplier.get().getFlowing());
        if (hasBucket) bucket(() -> simpleFluidSupplier.get().getBucket());
        return this;
    }

    public FluidEntry build() {
        fluidBlock = blockRegister.register(id, blockSupplier);
        fluidStill = fluidRegister.register(id, stillSupplier);
        fluidFlowing = fluidRegister.register(id + "_flowing", flowingSupplier);
        if (bucketSupplier != null) fluidBucket = itemRegister.register(id + "_bucket", bucketSupplier);
        fluidType = fluidTypeRegister.register(id, fluidTypeSupplier);
        return this;
    }

    public LiquidBlock getBlock() {
        return fluidBlock.get();
    }

    public FlowingFluid getStill() {
        return fluidStill.get();
    }

    public FlowingFluid getFlowing() {
        return fluidFlowing.get();
    }

    public Item getBucket() {
        return fluidBucket.get();
    }

    public FluidType getType() {
        return fluidType.get();
    }

    public RegistryObject<LiquidBlock> getBlockObject() {
        return fluidBlock;
    }

    public RegistryObject<FlowingFluid> getStillObject() {
        return fluidStill;
    }

    public RegistryObject<FlowingFluid> getFlowingObject() {
        return fluidFlowing;
    }

    public RegistryObject<Item> getBucketObject() {
        return fluidBucket;
    }

    public RegistryObject<FluidType> getTypeObject() {
        return fluidType;
    }

}
