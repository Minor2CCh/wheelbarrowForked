package com.asasinmode.wheelbarrow.neoforge;

import com.asasinmode.wheelbarrow.Wheelbarrow;
import com.asasinmode.wheelbarrow.client.compat.WheelbarrowConfigScreen;
import com.asasinmode.wheelbarrow.neoforge.platform.NeoForgePlatform;
import com.asasinmode.wheelbarrow.platform.ModPlatform;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(Wheelbarrow.MOD_ID)
public class WheelbarrowNeoForge {
    public WheelbarrowNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(NeoForgePlatform::init);
        // Run our common setup.
        Wheelbarrow.init();
        NeoForgePlatform.registryInit(modEventBus);

        if (ModPlatform.INSTANCE.isModLoaded("cloth_config")) {
            modContainer.registerExtensionPoint(
                    IConfigScreenFactory.class,
                    (minecraft, parent) -> WheelbarrowConfigScreen.buildConfigScreen(parent));
        }
    }
}
