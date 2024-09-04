package cn.solarmoon.solarmoon_core.api.data;

import com.google.gson.annotations.SerializedName;


/**
 * 食物属性
 */
public class FoodValue {

    public static FoodValue EMPTY = new FoodValue(0, 0);

    public FoodValue(int nutrition, float saturation) {
        this.nutrition = nutrition;
        this.saturation = saturation;
    }

    @SerializedName("hunger")
    public int nutrition;

    @SerializedName("saturation")
    public float saturation;

    /**
     * 检测至少有一项不为0（对饥饿值有帮助）
     */
    public boolean isValid() {
        return nutrition > 0 || saturation > 0;
    }

    @Override
    public String toString() {
        return "FoodValue{" +
                "nutrition=" + nutrition +
                ", saturation=" + saturation +
                '}';
    }

}
