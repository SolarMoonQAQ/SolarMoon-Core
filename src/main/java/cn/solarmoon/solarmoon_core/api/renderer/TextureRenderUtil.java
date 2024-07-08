package cn.solarmoon.solarmoon_core.api.renderer;

import cn.solarmoon.solarmoon_core.api.capability.anim_ticker.AnimTicker;
import cn.solarmoon.solarmoon_core.api.phys.SMath;
import cn.solarmoon.solarmoon_core.api.tile.fluid.FluidHandlerUtil;
import cn.solarmoon.solarmoon_core.registry.common.SolarCapabilities;
import com.google.common.base.Functions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Credit: MoonLight - <a href="https://github.com/MehVahdJukaar/Moonlight">...</a>
 */
public class TextureRenderUtil {

    public static void render(ResourceLocation texture, int minU, int minV, int maxU, int maxV, float width, float height, int color, float alpha, int luminosity,
                              PoseStack poseStack, MultiBufferSource bufferIn, int light) {
        poseStack.pushPose();
        if (luminosity != 0) light = light & 15728640 | luminosity << 4;
        VertexConsumer builder = getBlockMaterial(texture).buffer(bufferIn, RenderType::entityTranslucentCull);
        poseStack.translate(0.5, 0, 0.5);
        addCube(builder, poseStack,
                minU/16f, minV/16f,
                maxU/16f, maxV/16f,
                width, height,
                light, color, alpha, true, true, true);
        poseStack.popPose();
    }

    public static void renderAnimatedFluid(float width, float height, float yOffset, BlockEntity be, PoseStack poseStack, MultiBufferSource buffer, int light) {
        be.getCapability(ForgeCapabilities.FLUID_HANDLER).ifPresent(tank -> {
            be.getCapability(SolarCapabilities.BLOCK_ENTITY_DATA).ifPresent(data -> {
                FluidStack fluidStack = tank.getFluidInTank(0);
                AnimTicker animTicker1 = data.getAnimTicker(1);
                // 渲染流体过渡动画
                int targetColor = TextureRenderUtil.getColor(fluidStack);
                IClientFluidTypeExtensions fluidAttributes = IClientFluidTypeExtensions.of(fluidStack.getFluid());
                ResourceLocation spriteLocation = fluidAttributes.getStillTexture(fluidStack);

                poseStack.pushPose();
                int ticks = animTicker1.getTicks();
                float targetScale = FluidHandlerUtil.getScale(tank);
                float h0 = animTicker1.getFixedValue(); // 当前液体高度

                if (targetScale > 0 && !animTicker1.isEnabled()) {
                    h0 = targetScale;
                } // 这一段防止放置的方块没有animTicker而不显示液体 / 同时防止液体没保存而无动画

                float dh = targetScale - h0; // 当前液体与目标液体的高度比例差
                animTicker1.setMaxTick(10);
                int maxTicks = animTicker1.getMaxTick();
                float progress = ((float) ticks / maxTicks);
                float pstScale = h0 + SMath.smoothInterpolation(progress, 0, dh, 0.1f); // 当前的渲染高度比例，使用平滑插值
                float H = pstScale * height;
                poseStack.translate(0, yOffset, 0);
                if (spriteLocation != null) {
                    TextureRenderUtil.render(spriteLocation,
                            0, 0, (int) (width * 16), (int) (width * 16), width, H,
                            targetColor, 1, 0, poseStack, buffer, light);
                }
                poseStack.popPose();
                animTicker1.setFixedValue(pstScale);
            });
        });
    }

    public static void renderStaticFluid(float width, float height, float yOffset, ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, int light) {
        net.minecraftforge.fluids.FluidUtil.getFluidHandler(stack).ifPresent(tank -> {
            poseStack.pushPose();
            poseStack.translate(0, yOffset, 0);
            FluidStack fluidStack = tank.getFluidInTank(0);
            int targetColor = TextureRenderUtil.getColor(fluidStack);
            Fluid fluid = fluidStack.getFluid();
            IClientFluidTypeExtensions fluidAttributes = IClientFluidTypeExtensions.of(fluid);
            ResourceLocation spriteLocation = fluidAttributes.getStillTexture(fluidStack);
            float H = FluidHandlerUtil.getScale(tank) * height;
            if (spriteLocation != null) {
                TextureRenderUtil.render(spriteLocation,
                        0, 0, (int) (width * 16), (int) (width * 16), width, H,
                        targetColor, 1, 0, poseStack, buffer, light);
            }
            poseStack.popPose();
        });
    }

    public static void renderStaticFluid(float width, float height, float yOffset, BlockEntity blockEntity, PoseStack poseStack, MultiBufferSource buffer, int light) {
        blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER).ifPresent(tank -> {
            poseStack.pushPose();
            poseStack.translate(0, yOffset, 0);
            FluidStack fluidStack = tank.getFluidInTank(0);
            int targetColor = TextureRenderUtil.getColor(fluidStack);
            Fluid fluid = fluidStack.getFluid();
            IClientFluidTypeExtensions fluidAttributes = IClientFluidTypeExtensions.of(fluid);
            ResourceLocation spriteLocation = fluidAttributes.getStillTexture(fluidStack);
            float H = FluidHandlerUtil.getScale(tank) * height;
            if (spriteLocation != null) {
                TextureRenderUtil.render(spriteLocation,
                        0, 0, (int) (width * 16), (int) (width * 16), width, H,
                        targetColor, 1, 0, poseStack, buffer, light);
            }
            poseStack.popPose();
        });
    }

    public static void renderFluid(int color, float alpha, int luminosity, int minU, int minV, int maxU, int maxV,
                                   ResourceLocation texture, PoseStack poseStack, MultiBufferSource bufferIn, int light) {
        poseStack.pushPose();
        if (luminosity != 0) light = light & 15728640 | luminosity << 4;
        VertexConsumer builder = getBlockMaterial(texture).buffer(bufferIn, RenderType::entityTranslucentCull);
        poseStack.translate(0.5, 0, 0.5);
        addCube(builder, poseStack,
                minU/16f, minV/16f,
                maxU/16f, maxV/16f,
                1, 1,
                light, color, alpha, true, true, true);
        poseStack.popPose();
    }

    private static final Cache<ResourceLocation, Material> CACHED_MATERIALS = CacheBuilder.newBuilder()
            .expireAfterAccess(2, TimeUnit.MINUTES)
            .build();

    public static Material getBlockMaterial(ResourceLocation bockTexture) {
        try {
            return CACHED_MATERIALS.get(bockTexture, () -> new Material(TextureAtlas.LOCATION_BLOCKS, bockTexture));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取液体颜色
     */
    public static int getColor(FluidStack fluidStack) {
        Fluid fluid = fluidStack.getFluid();
        IClientFluidTypeExtensions handler = IClientFluidTypeExtensions.of(fluid);
        return handler.getTintColor(fluidStack);
    }

    /**
     * 获取液体颜色ARGB
     */
    public static float[] getColorARGB(FluidStack fluidStack) {
        int fluidColor = getColor(fluidStack);
        float[] colorArray = new float[4];
        colorArray[0] = (fluidColor >> 16 & 0xFF) / 255.0F; //红
        colorArray[1] = (fluidColor >> 8 & 0xFF) / 255.0F;  //绿
        colorArray[2] = (fluidColor & 0xFF) / 255.0F;       //蓝
        colorArray[3] = ((fluidColor >> 24) & 0xFF) / 255F; //透明度
        return colorArray;
    }

    public enum FluidFlow {
        STILL, FLOWING
    }

    public static TextureAtlasSprite getFluidTexture(FluidStack fluidStack, FluidFlow type) {
        if(fluidStack.isEmpty()) return null;
        Fluid fluid = fluidStack.getFluid();
        ResourceLocation spriteLocation;
        IClientFluidTypeExtensions fluidAttributes = IClientFluidTypeExtensions.of(fluid);
        if (type == FluidFlow.STILL) {
            spriteLocation = fluidAttributes.getStillTexture(fluidStack);
        }
        else {
            spriteLocation = fluidAttributes.getFlowingTexture(fluidStack);
        }
        return getSprite(spriteLocation);
    }

    public static TextureAtlasSprite getSprite(ResourceLocation spriteLocation) {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(spriteLocation);
    }

    public static void addCube(VertexConsumer builder, PoseStack poseStack,
                               float width, float height, int light, int color) {
        addCube(builder, poseStack, 0, 0, width, height, light, color);
    }

    public static void addCube(VertexConsumer builder, PoseStack poseStack,
                               float uOff, float vOff,
                               float width, float height, int light, int color) {
        addCube(builder, poseStack, uOff, vOff,
                width, height, light, color, 1, true, true, true);
    }

    public static void addCube(VertexConsumer builder, PoseStack poseStack,
                               float uOff, float vOff,
                               float w, float h, int combinedLightIn,
                               int color, float alpha,
                               boolean up, boolean down, boolean wrap) {
        addCube(builder, poseStack, uOff, 1 - (vOff + h), uOff + w, 1 - vOff, w, h, combinedLightIn, color, alpha, up, down, wrap);
    }

    public static final Quaternionf XN90 = Axis.XP.rotationDegrees(-90);

    private static final Map<Direction, Quaternionf> DIR2ROT = Maps.newEnumMap(Arrays.stream(Direction.values())
            .collect(Collectors.toMap(Functions.identity(), d -> d.getOpposite().getRotation().mul(XN90))));

    public static void addCube(VertexConsumer builder, PoseStack poseStack,
                               float minU, float minV,
                               float maxU, float maxV,
                               float w, float h,
                               int combinedLightIn,
                               int color,
                               float alpha,
                               boolean up, boolean down, boolean wrap) {

        int lu = combinedLightIn & '\uffff';
        int lv = combinedLightIn >> 16 & '\uffff';
        float minV2 = maxV - w;

        int r = FastColor.ARGB32.red(color);
        int g = FastColor.ARGB32.green(color);
        int b = FastColor.ARGB32.blue(color);
        int a = (int) (255 * alpha);

        float hw = w / 2f;
        float hh = h / 2f;

        float inc = 0;

        poseStack.pushPose();
        poseStack.translate(0, hh, 0);
        for (var d : Direction.values()) {
            float v0 = minV;
            float t = hw;
            float y0 = -hh;
            float y1 = hh;
            float i = inc;
            if (d.getAxis() == Direction.Axis.Y) {
                if ((!up && d == Direction.UP) || !down) continue;
                t = hh;
                y0 = -hw;
                y1 = hw;
                v0 = minV2;
            } else if (!wrap) {
                inc += w;
            }
            poseStack.pushPose();
            poseStack.mulPose(DIR2ROT.get(d));
            poseStack.translate(0, 0, -t);
            addQuad(builder, poseStack, -hw, y0, hw, y1, minU + i, v0, maxU + i, maxV, r, g, b, a, lu, lv);
            poseStack.popPose();

        }
        poseStack.popPose();
    }

    public static void addQuad(VertexConsumer builder, PoseStack poseStack,
                               float x0, float y0,
                               float x1, float y1,
                               float u0, float v0,
                               float u1, float v1,
                               int r, int g, int b, int a,
                               int lu, int lv) {
        PoseStack.Pose last = poseStack.last();
        Vector3f vector3f = last.normal().transform(new Vector3f(0, 0, -1));
        float nx = vector3f.x;
        float ny = vector3f.y;
        float nz = vector3f.z;
        vertF(builder, poseStack, x0, y1, 0, u0, v0, r, g, b, a, lu, lv, nx, ny, nz);
        vertF(builder, poseStack, x1, y1, 0, u1, v0, r, g, b, a, lu, lv, nx, ny, nz);
        vertF(builder, poseStack, x1, y0, 0, u1, v1, r, g, b, a, lu, lv, nx, ny, nz);
        vertF(builder, poseStack, x0, y0, 0, u0, v1, r, g, b, a, lu, lv, nx, ny, nz);
    }

    private static void vertF(VertexConsumer builder, PoseStack poseStack, float x, float y, float z,
                              float u, float v,
                              int r, int g, int b, int a,
                              int lu, int lv, float nx, float ny, float nz) {
        builder.vertex(poseStack.last().pose(), x, y, z);
        builder.color(r, g, b, a);
        builder.uv(u, v);
        builder.overlayCoords(0, 10);
        builder.uv2(lu, lv);
        builder.normal(nx, ny, nz);
        builder.endVertex();
    }

}
