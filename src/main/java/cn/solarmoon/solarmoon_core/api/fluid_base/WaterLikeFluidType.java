package cn.solarmoon.solarmoon_core.api.fluid_base;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * 换色水
 */
public class WaterLikeFluidType extends BaseFluidType {

    private final boolean defaultUnderOverlay;

    public static Properties waterLikeProperties(boolean canConvertToSource) {
        return FluidType.Properties.create()
                .fallDistanceModifier(0F)
                .canExtinguish(true)
                .canConvertToSource(canConvertToSource)
                .supportsBoating(true)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH);
    }

    public WaterLikeFluidType(String modId, String id, int color, boolean defaultUnderOverlay, Properties properties) {
        super(modId, id, color, properties);
        this.defaultUnderOverlay = defaultUnderOverlay;
    }

    @Override
    public @Nullable BlockPathTypes getBlockPathType(FluidState state, BlockGetter level, BlockPos pos, @Nullable Mob mob, boolean canFluidLog) {
        return canFluidLog ? super.getBlockPathType(state, level, pos, mob, true) : null;
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new IClientFluidTypeExtensions() {
            private static final ResourceLocation UNDERWATER_LOCATION = new ResourceLocation("textures/misc/underwater.png"),
                    WATER_STILL = new ResourceLocation("block/water_still"),
                    WATER_FLOW = new ResourceLocation("block/water_flow"),
                    WATER_OVERLAY = new ResourceLocation("block/water_overlay");

            @Override
            public ResourceLocation getStillTexture()
            {
                return WATER_STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture()
            {
                return WATER_FLOW;
            }

            @Nullable
            @Override
            public ResourceLocation getOverlayTexture()
            {
                return WATER_OVERLAY;
            }

            @Override
            public ResourceLocation getRenderOverlayTexture(Minecraft mc) {
                return defaultUnderOverlay ? UNDERWATER_LOCATION : spriteUnder;
            }

            @Override
            public int getTintColor()
            {
                return color;
            }

        });
    }

}
