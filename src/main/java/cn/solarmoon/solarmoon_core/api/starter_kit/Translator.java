package cn.solarmoon.solarmoon_core.api.starter_kit;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * 使用public static来固定在mod中
 */
public class Translator {

    private final String modId;

    public Translator(String modId) {
        this.modId = modId;
    }

    public Component set(String string1, String string2, Object... objects) {
        return Component.translatable(string1 + "." + modId + "." + string2, objects);
    }

    public Component set(String string1, String string2, ChatFormatting format, Object... objects) {
        return Component.translatable(string1 + "." + modId + "." + string2, objects)
                .withStyle(format);
    }

    public static Translator create(String modId) {
        return new Translator(modId);
    }

}
