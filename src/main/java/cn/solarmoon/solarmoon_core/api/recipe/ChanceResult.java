package cn.solarmoon.solarmoon_core.api.recipe;

import cn.solarmoon.solarmoon_core.api.data.SerializeHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record ChanceResult(ItemStack stack, float chance) {

    public static final ChanceResult EMPTY = new ChanceResult(ItemStack.EMPTY, 1);

    public ItemStack rollOutput(RandomSource rand, int fortuneLevel) {
        int outputAmount = stack.getCount();

        for (int roll = 0; roll < stack.getCount(); ++roll) {
            if ((double) rand.nextFloat() > (double) chance + (double) fortuneLevel * 0.1) {
                --outputAmount;
            }
        }

        if (outputAmount == 0) {
            return ItemStack.EMPTY;
        } else {
            ItemStack out = stack.copy();
            out.setCount(outputAmount);
            return out;
        }
    }

    public JsonElement serialize() {
        JsonObject json = new JsonObject();
        ResourceLocation resourceLocation = ForgeRegistries.ITEMS.getKey(stack.getItem());
        json.addProperty("item", Objects.requireNonNull(resourceLocation).toString());
        int count = stack.getCount();
        if (count != 1) {
            json.addProperty("count", count);
        }

        if (stack.hasTag()) {
            json.add("nbt", JsonParser.parseString(Objects.requireNonNull(stack.getTag()).toString()));
        }

        if (chance != 1.0F) {
            json.addProperty("chance", this.chance);
        }

        return json;
    }

    public static ChanceResult deserialize(JsonElement je) {
        if (!je.isJsonObject()) {
            throw new JsonSyntaxException("Must be a json object");
        } else {
            JsonObject json = je.getAsJsonObject();
            String itemId = GsonHelper.getAsString(json, "item");
            int count = GsonHelper.getAsInt(json, "count", 1);
            float chance = GsonHelper.getAsFloat(json, "chance", 1.0F);
            ItemStack itemstack = new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId))), count);
            if (GsonHelper.isValidPrimitive(json, "nbt")) {
                try {
                    JsonElement element = json.get("nbt");
                    itemstack.setTag(TagParser.parseTag(element.isJsonObject() ? SerializeHelper.GSON.toJson(element) : GsonHelper.convertToString(element, "nbt")));
                } catch (CommandSyntaxException var7) {
                    var7.printStackTrace();
                }
            }

            return new ChanceResult(itemstack, chance);
        }
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeItemStack(stack(), false);
        buf.writeFloat(chance());
    }

    public static ChanceResult read(FriendlyByteBuf buf) {
        return new ChanceResult(buf.readItem(), buf.readFloat());
    }

    /**
     * @return 获取所有可能的输出物品
     */
    public static List<ItemStack> getResults(NonNullList<ChanceResult> chanceResults) {
        return chanceResults.stream()
                .map(ChanceResult::stack)
                .collect(Collectors.toList());
    }

    /**
     * 根据幸运等级对results进行随机选取并输出最终结果
     */
    public static List<ItemStack> getRolledResults(Player player, NonNullList<ChanceResult> chanceResults) {
        int fortuneLevel = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.BLOCK_FORTUNE, player.getItemInHand(InteractionHand.MAIN_HAND));
        MobEffectInstance luckEffect = player.getEffect(MobEffects.LUCK);
        int luckPotionLevel = (luckEffect != null) ? luckEffect.getAmplifier() + 1 : 0;
        RandomSource rand = player.getRandom();
        int luck = fortuneLevel + luckPotionLevel;
        List<ItemStack> results = new ArrayList<>();
        for (ChanceResult output : chanceResults) {
            ItemStack stack = output.rollOutput(rand, luck);
            if (!stack.isEmpty()) {
                results.add(stack);
            }
        }
        return results;
    }

}