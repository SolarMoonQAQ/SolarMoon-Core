package cn.solarmoon.solarmoon_core.api.fluid_base;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class BaseFluidType extends FluidType {

    protected final int color;
    protected final ResourceLocation spriteStill;
    protected final ResourceLocation spriteFlowing;
    protected final ResourceLocation spriteOverlay;
    protected final ResourceLocation spriteUnder;

    public BaseFluidType(String modId, String id, int color, Properties properties) {
        super(properties);
        this.color = color;
        this.spriteStill = new ResourceLocation(modId + ":block/fluid/" + id + "_still");
        this.spriteFlowing = new ResourceLocation(modId + ":block/fluid/" + id + "_flow");
        this.spriteOverlay = new ResourceLocation(modId + ":block/fluid/" + id + "_overlay.png");
        this.spriteUnder = new ResourceLocation(modId + ":textures/misc/" + id + "_under.png");
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new IClientFluidTypeExtensions() {

            @Override
            public ResourceLocation getStillTexture()
            {
                return spriteStill;
            }

            @Override
            public ResourceLocation getFlowingTexture()
            {
                return spriteFlowing;
            }

            @Nullable
            @Override
            public ResourceLocation getOverlayTexture()
            {
                return spriteOverlay;
            }

            @Override
            public ResourceLocation getRenderOverlayTexture(Minecraft mc)
            {
                return spriteUnder;
            }

            @Override
            public int getTintColor()
            {
                return color;
            }

        });
    }

}
