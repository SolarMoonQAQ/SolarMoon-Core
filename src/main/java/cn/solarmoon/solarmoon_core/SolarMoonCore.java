package cn.solarmoon.solarmoon_core;

import cn.solarmoon.solarmoon_core.api.entry.ObjectRegistry;
import cn.solarmoon.solarmoon_core.api.starter_kit.Debug;
import cn.solarmoon.solarmoon_core.api.starter_kit.Translator;
import cn.solarmoon.solarmoon_core.data.SolarConfig;
import cn.solarmoon.solarmoon_core.feature.SolarMoonBase;
import cn.solarmoon.solarmoon_core.registry.client.SolarTooltips;
import cn.solarmoon.solarmoon_core.registry.common.*;
import net.minecraftforge.fml.common.Mod;

import static cn.solarmoon.solarmoon_core.SolarMoonCore.MOD_ID;


@Mod(MOD_ID)
public class SolarMoonCore extends SolarMoonBase {

    public static final String MOD_ID = "solarmoon_core";
    public static final ObjectRegistry REGISTRY = ObjectRegistry.create(MOD_ID);
    public static final Debug DEBUG = Debug.create("[§4曦月核心§f] ", SolarConfig.deBug);
    public static final Translator TRANSLATOR = Translator.create(MOD_ID);

    @Override
    public void objectsClientOnly() {

    }

    @Override
    public void objects() {
        SolarNetPacks.register();
        SolarAttributes.register();
        SolarDamageTypes.register();
        SolarRecipes.register();
    }

    @Override
    public void eventObjectsClientOnly() {
        new SolarTooltips().register();
    }

    @Override
    public void eventObjects() {
        new SolarCommonEvents().register();
        new SolarCapabilities().register();
        new SolarCommands().register();
    }

    @Override
    public void xData() {
        SolarConfig.register();
    }

    @Override
    public void compats() {
        //JEI
    }

}