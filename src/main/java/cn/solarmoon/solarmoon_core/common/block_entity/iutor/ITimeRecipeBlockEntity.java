package cn.solarmoon.solarmoon_core.common.block_entity.iutor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.wrapper.RecipeWrapper;

/**
 * 标识该方块实体是具有含时配方的，且能获取配方所需时间和当前进行到的时间（自己实现）<br/>
 * 能够自动保存time，就如同熔炉一样<br/>
 * 如果是类似营火那样一个方块多个时间的用IIndividualTimeRecipeBlockEntity
 */
public interface ITimeRecipeBlockEntity<R extends Recipe<?>> {

    int getTime();

    /**
     * 一般是记录配方所需时间（最大时间），一般是渲染用
     */
    int getRecipeTime();

    void setTime(int time);

    void setRecipeTime(int recipeTime);

    /**
     * 作为配方标识当然需要实现获取匹配配方的方法
     */
    R getCheckedRecipe();

}
