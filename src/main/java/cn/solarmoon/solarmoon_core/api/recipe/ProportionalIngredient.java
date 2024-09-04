package cn.solarmoon.solarmoon_core.api.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.util.RecipeMatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ProportionalIngredient(Ingredient ingredient, int count) {

    public void write(FriendlyByteBuf buf) {
        ingredient.toNetwork(buf);
        buf.writeInt(count);
    }

    public static ProportionalIngredient read(FriendlyByteBuf buf) {
        Ingredient ingredient = Ingredient.fromNetwork(buf);
        int count = buf.readInt();
        return new ProportionalIngredient(ingredient, count);
    }

    public static List<ProportionalIngredient> readProportionalIngredients(JsonObject json, String id) {
        List<ProportionalIngredient> ingredients = new ArrayList<>();
        if (json.has(id)) {
            for (JsonElement element : GsonHelper.getAsJsonArray(json, id)) {
                JsonObject j = element.getAsJsonObject();
                Ingredient ingredient = Ingredient.fromJson(GsonHelper.getNonNull(j, "ingredient"));
                int count = GsonHelper.getAsInt(j, "count", 1);
                ingredients.add(new ProportionalIngredient(ingredient, count));
            }
        }
        return ingredients;
    }

    public static List<ProportionalIngredient> readProportionalIngredients(FriendlyByteBuf buf) {
        List<ProportionalIngredient> ingredients = new ArrayList<>();
        int inCount = buf.readVarInt();

        for(int i = 0; i < inCount; ++i) {
            ingredients.add(read(buf));
        }

        return ingredients;
    }

    public static void writeProportionalIngredients(FriendlyByteBuf buf, List<ProportionalIngredient> ingredients) {
        buf.writeVarInt(ingredients.size());
        for (ProportionalIngredient proportionalIngredient : ingredients) {
            proportionalIngredient.write(buf);
        }
    }

    public static List<Ingredient> getAllIngredients(List<ProportionalIngredient> proportionalIngredients) {
        List<Ingredient> ingredients = new ArrayList<>();
        proportionalIngredients.forEach(p -> ingredients.add(p.ingredient()));
        return ingredients;
    }

    public static int sumCount(List<ProportionalIngredient> proportionalIngredients) {
        return proportionalIngredients.stream().mapToInt(p -> p.count).sum();
    }

    public static double getAccess(ProportionalIngredient in, List<ProportionalIngredient> proportionalIngredients) {
        return (double) in.count() / sumCount(proportionalIngredients);
    }

    public static Pair<Boolean, Integer> findMatch(List<ItemStack> stacks, List<ProportionalIngredient> ins) {
        Map<Item, Integer> match = new HashMap<>();
        // 把所有种类相同的物品堆到一起
        stacks.forEach(stack -> match.put(stack.getItem(), match.getOrDefault(stack.getItem(), 0) + stack.getCount()));
        List<Ingredient> ingredients = ProportionalIngredient.getAllIngredients(ins);
        List<ItemStack> stackMatch = match.keySet().stream().map(ItemStack::new).toList();
        boolean typeMatch = RecipeMatcher.findMatches(stackMatch, ingredients) != null;

        int itemSum = match.values().stream().mapToInt(Integer::intValue).sum();
        boolean proportionMatch = false;
        for (var set : match.entrySet()) {
            for (var pi : ins) {
                if (pi.ingredient().test(new ItemStack(set.getKey()))) {
                    double access = (double) set.getValue() / itemSum;
                    proportionMatch = ProportionalIngredient.getAccess(pi, ins) == access;
                    if (!proportionMatch) {
                        break;
                    }
                }
            }
            if (!proportionMatch) {
                break;
            }
        }

        boolean singleAccessMatch = itemSum % sumCount(ins) == 0;

        return Pair.of(typeMatch && proportionMatch && singleAccessMatch, itemSum / sumCount(ins));
    }

}
