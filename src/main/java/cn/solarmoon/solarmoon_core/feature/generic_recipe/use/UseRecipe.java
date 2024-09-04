package cn.solarmoon.solarmoon_core.feature.generic_recipe.use;

import cn.solarmoon.solarmoon_core.api.data.SerializeHelper;
import cn.solarmoon.solarmoon_core.api.entry.common.RecipeEntry;
import cn.solarmoon.solarmoon_core.api.recipe.ChanceResult;
import cn.solarmoon.solarmoon_core.api.recipe.IConcreteRecipe;
import cn.solarmoon.solarmoon_core.registry.common.SolarRecipes;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record UseRecipe(
        ResourceLocation id,
        Ingredient ingredient,
        Block inputBlock,
        Block outputBlock,
        NonNullList<ChanceResult> chanceResults
) implements IConcreteRecipe {

    public List<ItemStack> getResults() {
        return ChanceResult.getResults(chanceResults);
    }

    public List<ItemStack> getRolledResults(Player player) {
        return ChanceResult.getRolledResults(player, chanceResults);
    }

    @Override
    public RecipeEntry<?> getRecipeEntry() {
        return SolarRecipes.USE;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    public static class Serializer implements RecipeSerializer<UseRecipe> {

        @Override
        public UseRecipe fromJson(ResourceLocation id, JsonObject json) {
            Ingredient ingredient = Ingredient.fromJson(json.get("ingredient"));
            Block inputBlock = SerializeHelper.readBlock(json, "input_block");
            Block outputBlock = SerializeHelper.readBlock(json, "output_block", inputBlock);
            NonNullList<ChanceResult> chanceResults = SerializeHelper.readChanceResults(json, "results");
            return new UseRecipe(id, ingredient, inputBlock, outputBlock, chanceResults);
        }

        @Override
        public @Nullable UseRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            Ingredient ingredient = Ingredient.fromNetwork(buf);
            Block inputBlock = SerializeHelper.readBlock(buf);
            Block outputBlock = SerializeHelper.readBlock(buf);
            NonNullList<ChanceResult> chanceResults = SerializeHelper.readChanceResults(buf);
            return new UseRecipe(id, ingredient, inputBlock, outputBlock, chanceResults);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, UseRecipe recipe) {
            recipe.ingredient().toNetwork(buf);
            SerializeHelper.writeBlock(buf, recipe.inputBlock());
            SerializeHelper.writeBlock(buf, recipe.outputBlock());
            SerializeHelper.writeChanceResults(buf, recipe.chanceResults());
        }

    }
}
