package cn.solarmoon.solarmoon_core.api.tile;

import cn.solarmoon.solarmoon_core.api.block_base.BasicEntityBlock;
import cn.solarmoon.solarmoon_core.api.tile.inventory.ItemHandlerUtil;
import cn.solarmoon.solarmoon_core.api.tile.fluid.FluidHandlerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 自动存储实体数据到item相关方法中
 */
public abstract class SyncedEntityBlock extends BasicEntityBlock {

    public SyncedEntityBlock(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        ItemStack origin = super.getCloneItemStack(state, target, level, pos, player);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity != null) {
            blockEntity.saveToItem(origin);
        }
        return origin;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(level, pos, state, entity, stack);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        CompoundTag tag = BlockItem.getBlockEntityData(stack);
        if (blockEntity != null && tag != null) blockEntity.deserializeNBT(tag);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        List<ItemStack> drops = super.getDrops(state, builder);
        ItemStack t = new ItemStack(this);
        if(blockEntity != null) {
            blockEntity.saveToItem(t);
            drops.add(t);
        }
        return drops;
    }

    /**
     * 设定红石信号逻辑<br/>
     * 按液体或物品比例输出信号<br/>
     * 两者皆有的情况下优先判断流体<br/>
     * 优先输出信号强的那一方
     */
    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) return 0;
        int containerSignal = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER)
                .map(ItemHandlerHelper::calcRedstoneFromInventory)
                .orElse(0);
        int tankSignal = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER)
                .map(tank -> (int) FluidHandlerUtil.getScale(tank))
                .orElse(0) * 15;
        return Math.max(containerSignal, tankSignal);
    }

}
