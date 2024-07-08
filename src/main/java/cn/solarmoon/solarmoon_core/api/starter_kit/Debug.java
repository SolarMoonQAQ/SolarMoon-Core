package cn.solarmoon.solarmoon_core.api.starter_kit;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 使用public static来固定在mod中
 */
public class Debug {

    private final String prefix;
    private final ForgeConfigSpec.ConfigValue<Boolean> debugConfig;
    private final Logger logger;

    public Debug(String prefix, ForgeConfigSpec.ConfigValue<Boolean> debugConfig) {
        this.prefix = prefix;
        this.debugConfig = debugConfig;
        this.logger = LoggerFactory.getLogger(prefix);
    }

    /**
     * 客户端侧debug
     */
    public void send(String string) {
        if (FMLEnvironment.dist.isClient()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && debugConfig.get()) {
                mc.player.sendSystemMessage(Component.literal(prefix + string));
            }
        }
    }

    /**
     * 服务端侧debug
     */
    public void send(String string, Player player) {
        Level level = player.level();
        if(!level.isClientSide && debugConfig.get()) {
            player.sendSystemMessage(Component.literal(prefix  + string));
        }
    }

    public Logger getLogger() {
        return logger;
    }

    public static Debug create(String prefix, ForgeConfigSpec.ConfigValue<Boolean> debugConfig) {
        return new Debug(prefix, debugConfig);
    }

}
