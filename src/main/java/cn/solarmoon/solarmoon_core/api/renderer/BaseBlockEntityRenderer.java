package cn.solarmoon.solarmoon_core.api.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * 基本的方块实体渲染类，提供了一些快捷的定义
 * @param <E> 填入具体的方块类型以快速使用该类型的方法
 */
public abstract class BaseBlockEntityRenderer<E extends BlockEntity> implements BlockEntityRenderer<E> {

    protected BlockEntityRendererProvider.Context context;

     public BaseBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
         this.context = context;
     }

    @Override
    public abstract void render(E blockEntity, float tickDelta, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay);
}
