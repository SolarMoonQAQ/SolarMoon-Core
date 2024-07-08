package cn.solarmoon.solarmoon_core.api.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * 基本物品渲染，只是简化了一些操作
 */
public abstract class BaseItemRenderer extends BlockEntityWithoutLevelRenderer {

    public BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
    public ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

    public BaseItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public abstract void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay);

}
