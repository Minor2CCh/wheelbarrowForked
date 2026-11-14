package com.asasinmode.wheelbarrow.fabric.client.integration;

import com.asasinmode.wheelbarrow.client.compat.WheelbarrowConfigScreen;
import com.asasinmode.wheelbarrow.platform.ModPlatform;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

// inspired by https://github.com/newhoryzon/farmers-delight-fabric/blob/master/src/main/java/com/nhoryzon/mc/farmersdelight/integration/modmenu/FarmersDelightModMenu.java
public class WheelbarrowModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (!ModPlatform.INSTANCE.isModLoaded("cloth-config")) {
            return null;
        }
		return WheelbarrowConfigScreen::buildConfigScreen;
	}
}
