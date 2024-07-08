package cn.solarmoon.solarmoon_core.mixin;


import cn.solarmoon.solarmoon_core.api.ability.CustomPlaceableItem;
import cn.solarmoon.solarmoon_core.api.tile.fluid.ITankTileItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.extensions.IForgeItem;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemMixin implements IForgeItem {

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        Item item = stack.getItem();
        if (item instanceof ITankTileItem tankItem) {
            return tankItem.initTank(stack);
        }
        return IForgeItem.super.initCapabilities(stack, nbt);
    }

    /**
     * 快速添加原版可放置方块
     */
    @Inject(method = "useOn", at = @At("HEAD"))
    public void useOn(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        if (player != null && level.getBlockState(pos).canBeReplaced()) {
            for (var bound : CustomPlaceableItem.getBoundList()) {
                if (stack.is(bound.getKey())) {
                    BlockState state =  bound.getValue().getStateForPlacement(new BlockPlaceContext(context));
                    if (state != null && canPlace(new BlockPlaceContext(context), state)) {
                        stack.shrink(1);
                        level.setBlock(pos, state, 3);
                        level.playSound(null, pos, state.getSoundType().getPlaceSound(), SoundSource.BLOCKS);
                        player.swing(context.getHand());
                    }
                    break;
                }
            }
        }
    }

    private boolean canPlace(BlockPlaceContext context, BlockState state) {
        Player player = context.getPlayer();
        CollisionContext collisioncontext = player == null ? CollisionContext.empty() : CollisionContext.of(player);
        return (state.canSurvive(context.getLevel(), context.getClickedPos())) && context.getLevel().isUnobstructed(state, context.getClickedPos(), collisioncontext);
    }

}
