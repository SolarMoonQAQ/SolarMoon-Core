package cn.solarmoon.solarmoon_core.api.fluid_base;

import cn.solarmoon.solarmoon_core.api.entry.common.FluidEntry;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraftforge.fluids.ForgeFlowingFluid;

/**
 * 虽然forgeFlowingFluid有getBucket等方法，但是这些方法是获取的注册后的绑定物品，这里因为需要获取注册所需的类，
 * 因此重开一个simpleFluid方法来便捷操控这些内容
 */
public class SimpleFluid {

    private final FluidEntry fluidEntry;

    public SimpleFluid(FluidEntry fluidEntry) {
        this.fluidEntry = fluidEntry;
    }

    public LiquidBlock getBlock() {
        return new FluidBlock(fluidEntry);
    }

    public ForgeFlowingFluid getFlowing() {
        return new Flowing(fluidEntry);
    }

    public ForgeFlowingFluid getSource() {
        return new Source(fluidEntry);
    }

    public BucketItem getBucket() {
        return new Bucket(fluidEntry);
    }

    public static class FluidBlock extends BaseFluid.FluidBlock {
        public FluidBlock(FluidEntry fluidEntry) {
            super(fluidEntry.getStillObject());
        }
    }

    public static class Flowing extends BaseFluid.Flowing {
        public Flowing(FluidEntry fluidEntry) {
            super(makeProperties(fluidEntry));
        }
    }

    public static class Source extends BaseFluid.Source {
        public Source(FluidEntry fluidEntry) {
            super(makeProperties(fluidEntry));
        }
    }

    public static class Bucket extends BaseFluid.Bucket {
        public Bucket(FluidEntry fluidEntry) {
            super(fluidEntry.getStillObject());
        }
    }

    private static ForgeFlowingFluid.Properties makeProperties(FluidEntry fluidEntry) {
        return new ForgeFlowingFluid.Properties(
                fluidEntry.getTypeObject(),
                fluidEntry.getStillObject(),
                fluidEntry.getFlowingObject()
        )
                .block(fluidEntry.getBlockObject())
                .bucket(fluidEntry.getBucketObject());
    }

}
