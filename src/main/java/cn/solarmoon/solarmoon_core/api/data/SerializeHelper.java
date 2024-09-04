package cn.solarmoon.solarmoon_core.api.data;

import cn.solarmoon.solarmoon_core.api.recipe.ChanceResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * 超便捷快速json配方解码，这里所有的内容都是写成可选的（也就是输错主条目不报错）
 */
public class SerializeHelper {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * @return 机会物品源列表
     */
    public static NonNullList<ChanceResult> readChanceResults(JsonObject json, String id) {
        NonNullList<ChanceResult> results = NonNullList.create();
        if (json.has(id)) {
            for (JsonElement result : GsonHelper.getAsJsonArray(json, id)) {
                results.add(ChanceResult.deserialize(result));
            }
        }
        return results;
    }

    /**
     * @return 从buf中读取机会物品源列表
     */
    public static NonNullList<ChanceResult> readChanceResults(FriendlyByteBuf buffer) {
        int i = buffer.readVarInt();
        NonNullList<ChanceResult> resultsIn = NonNullList.withSize(i, ChanceResult.EMPTY);
        resultsIn.replaceAll(ignored -> ChanceResult.read(buffer));
        return resultsIn;
    }

    /**
     * 写入机会物品源列表
     */
    public static void writeChanceResults(FriendlyByteBuf buffer, NonNullList<ChanceResult> results) {
        buffer.writeVarInt(results.size());
        for (ChanceResult result : results) {
            result.write(buffer);
        }
    }

    public static Fluid readFluid(JsonObject json, String ide) {
        Fluid fluid = Fluids.EMPTY;
        if (json.has(ide)) {
            String id = GsonHelper.getAsString(GsonHelper.getAsJsonObject(json, ide), "id");
            fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(id));
            if (fluid == null) fluid = Fluids.EMPTY;
        }
        return fluid;
    }

    public static Fluid readFluid(FriendlyByteBuf buffer) {
        return buffer.readRegistryId();
    }

    public static void writeFluid(FriendlyByteBuf buffer, Fluid fluid) {
        buffer.writeRegistryId(ForgeRegistries.FLUIDS, fluid);
    }

    /**
     * read buffer和write有直接的方法，只有json读取需要这个。
     * @return null时返回空
     */
    public static FluidStack readFluidStack(JsonObject json, String id) {
        if (json.has(id)) {
            JsonObject jsonObject = GsonHelper.getAsJsonObject(json, id);
            Fluid fluid = readFluid(json, id);
            int amount = 1000;
            if (jsonObject.has("amount")) {
                amount = GsonHelper.getAsInt(jsonObject, "amount");
            }
            FluidStack read = new FluidStack(fluid, amount);
            if (GsonHelper.isValidPrimitive(jsonObject, "nbt")) {
                try {
                    JsonElement element = jsonObject.get("nbt");
                    read.setTag(TagParser.parseTag(element.isJsonObject() ? GSON.toJson(element) : GsonHelper.convertToString(element, "nbt")));
                } catch (CommandSyntaxException var7) {
                    var7.printStackTrace();
                }
            }
            return read;
        }
        else return FluidStack.EMPTY;
    }

    /**
     * @return 默认从json读取完整的物品（原来的方法不会读取如capability的数据）
     */
    public static ItemStack readItemStack(JsonObject json, String id) {
        return CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, id), true);
    }

    /**
     * 此处有个默认值，当id不存在时返回一个默认的而不报错
     * @return 默认从json读取完整的物品（原来的方法不会读取如capability的数据）
     */
    public static ItemStack readItemStack(JsonObject json, String id, ItemStack defaultItem) {
        if (json.has(id)) {
            return CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, id), true);
        }
        return defaultItem;
    }

    /**
     * 注意，使用这个方法必须和此类中的写入方法配合，不要用外部方法，会导致问题！
     * 包括下面所有和itemstack相关的内容都是如此！
     * @return 默认读取完整的物品（原来的方法不会读取如capability的数据）
     */
    public static ItemStack readItemStack(FriendlyByteBuf buf) {
        return ItemStack.of(buf.readAnySizeNbt());
    }

    /**
     * 默认发送完整物品（真正意义上的完整）
     */
    public static void writeItemStack(FriendlyByteBuf buf, ItemStack stack) {
        buf.writeNbt(stack.save(new CompoundTag()));
    }

    public static List<ItemStack> readItemStacks(JsonObject json, String id) {
        List<ItemStack> stacks = new ArrayList<>();
        if (json.has(id)) {
            for (var element : GsonHelper.getAsJsonArray(json, id)) {
                stacks.add(CraftingHelper.getItemStack(element.getAsJsonObject(), true));
            }
        }
        return stacks;
    }

    /**
     * 必须和此类中的write配合使用
     */
    public static List<ItemStack> readItemStacks(FriendlyByteBuf buffer) {
        List<ItemStack> stacks = new ArrayList<>();
        int itemCount = buffer.readVarInt();
        for (int i = 0; i < itemCount; i++) {
            stacks.add(readItemStack(buffer));
        }
        return stacks;
    }

    /**
     * 必须和此类中的read配合使用
     */
    public static void writeItemStacks(FriendlyByteBuf buffer, List<ItemStack> stacks) {
        buffer.writeVarInt(stacks.size());
        for (ItemStack stack : stacks) {
            writeItemStack(buffer, stack);
        }
    }

    public static Ingredient readIngredient(JsonObject json, String id) {
        if (json.has(id)) {
            return Ingredient.fromJson(json.get(id));
        } else return Ingredient.EMPTY;
    }

    public static List<Ingredient> readIngredients(JsonObject json, String id) {
        List<Ingredient> ingredients = new ArrayList<>();
        if (json.has(id)) {
            for (var element : GsonHelper.getAsJsonArray(json, id)) {
                ingredients.add(Ingredient.fromJson(element));
            }
        }
        return ingredients;
    }

    public static List<Ingredient> readIngredients(FriendlyByteBuf buffer) {
        List<Ingredient> ingredients = new ArrayList<>();
        int inCount = buffer.readVarInt();
        for (int i = 0; i < inCount; i++) {
            ingredients.add(Ingredient.fromNetwork(buffer));
        }
        return ingredients;
    }

    public static void writeIngredients(FriendlyByteBuf buffer, List<Ingredient> ingredients) {
        buffer.writeVarInt(ingredients.size());
        for (var in : ingredients) {
            in.toNetwork(buffer);
        }
    }

    public static Block readBlock(JsonObject json, String id) {
        if (json.has(id)) {
            JsonObject bj = GsonHelper.getAsJsonObject(json, id);
            String blockId = GsonHelper.getAsString(bj, "id");
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId));
            if (block != null) return block;
        }
        return Blocks.AIR;
    }

    public static Block readBlock(JsonObject json, String id, Block blockDefault) {
        if (json.has(id)) {
            JsonObject bj = GsonHelper.getAsJsonObject(json, id);
            String blockId = GsonHelper.getAsString(bj, "id");
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId));
            if (block != null) return block;
        }
        return blockDefault;
    }

    public static Block readBlock(FriendlyByteBuf buf) {
        return buf.readRegistryId();
    }

    public static void writeBlock(FriendlyByteBuf buf, Block block) {
        buf.writeRegistryId(ForgeRegistries.BLOCKS, block);
    }

    public static Vec3 readVec3(FriendlyByteBuf buf) {
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        return new Vec3(x, y, z);
    }

    public static void writeVec3(FriendlyByteBuf buf, Vec3 vec3) {
        buf.writeDouble(vec3.x);
        buf.writeDouble(vec3.y);
        buf.writeDouble(vec3.z);
    }

    public static List<Vec3> readVec3List(FriendlyByteBuf buf) {
        List<Vec3> vec3List = new ArrayList<>();
        int size = buf.readVarInt();
        for (int i = 0; i < size; i++) {
            Vec3 vec3 = readVec3(buf);
            vec3List.add(vec3);
        }
        return vec3List;
    }

    public static void writeVec3List(FriendlyByteBuf buf, List<Vec3> vec3List) {
        buf.writeVarInt(vec3List.size());
        for (var vec3 : vec3List) {
            writeVec3(buf, vec3);
        }
    }

    public static MobEffectInstance readEffect(JsonObject json) {
        ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(json, "id"));
        MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(id);
        int duration = GsonHelper.getAsInt(json, "duration", 0);
        int amplifier = GsonHelper.getAsInt(json, "amplifier", 0);
        boolean ambient = GsonHelper.getAsBoolean(json, "ambient", false);
        boolean visible = GsonHelper.getAsBoolean(json, "visible", true);
        boolean showIcon = GsonHelper.getAsBoolean(json, "showIcon", true);
        return new MobEffectInstance(effect, duration, amplifier, ambient, visible, showIcon);
    }

    public static List<MobEffectInstance> readEffects(JsonObject json, String id) {
        List<MobEffectInstance> effectInstances = new ArrayList<>();
        if (json.has(id)) {
            for (var element : GsonHelper.getAsJsonArray(json, id)) {
                effectInstances.add(readEffect(element.getAsJsonObject()));
            }
        }
        return effectInstances;
    }

    public static FoodValue readFoodValue(JsonObject json, String id) {
        if (json.has(id)) {
            JsonObject j = GsonHelper.getAsJsonObject(json, id);
            int nutrition = GsonHelper.getAsInt(j, "nutrition");
            float saturation = GsonHelper.getAsFloat(j, "saturation");
            return new FoodValue(nutrition, saturation);
        }
        return FoodValue.EMPTY;
    }

}
