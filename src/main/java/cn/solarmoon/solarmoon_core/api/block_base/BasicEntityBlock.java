package cn.solarmoon.solarmoon_core.api.block_base;

import cn.solarmoon.solarmoon_core.api.block_util.IBlockFunctionProvider;
import cn.solarmoon.solarmoon_core.api.event.BasicEntityBlockTickEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.Nullable;

/**
 * 有基本的设置对应方块实体功能的实体方块<br/>
 * 以及一个ticker<br/>
 * 还是 万 物 本 源
 */
public abstract class BasicEntityBlock extends BaseEntityBlock implements IBlockFunctionProvider {

    public BasicEntityBlock(Properties properties) {
        super(properties);
    }

    /**
     * 必填项 - 碰撞箱
     */
    @Override
    public abstract VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context);

    /**
     * 创建一个ticker
     */
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, getBlockEntityType(), this::tick);
    }

    /**
     * @return 设置绑定的方块实体
     */
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return getBlockEntityType().create(pos, state);
    }

    /**
     * 设置绑定的方块实体
     * 决定ticker所对应的实体类型（具体到注册类）
     */
    public abstract BlockEntityType<?> getBlockEntityType();

    /**
     * 设置实体的模型渲染类型
     */
    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    /**
     * 启用红石信号
     */
    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    /**
     * 默认进行容器的同步
     */
    public void tick(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        BasicEntityBlockTickEvent tickEvent = new BasicEntityBlockTickEvent(blockEntity);
        MinecraftForge.EVENT_BUS.post(tickEvent);
    }

}
