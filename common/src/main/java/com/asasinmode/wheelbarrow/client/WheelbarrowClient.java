package com.asasinmode.wheelbarrow.client;

import com.asasinmode.wheelbarrow.client.platform.ModPlatformClient;
import com.asasinmode.wheelbarrow.entity.custom.WheelbarrowEntity;
import com.asasinmode.wheelbarrow.networking.YeetC2SPacket;
import net.minecraft.client.KeyMapping;

import java.util.List;

public final class WheelbarrowClient {
    private WheelbarrowClient(){}
    public static final List<KeyMapping> REGISTER_KEYMAPPING_LIST = List.of(Keybinds.yeet);
    public static void init(){

        ModPlatformClient.INSTANCE.registerEndTickEvents((minecraft) -> {
            while (Keybinds.yeet.consumeClick()) {
                if (minecraft.player != null && minecraft.player.getVehicle() instanceof WheelbarrowEntity wheelbarrow
                        && wheelbarrow.getControllingPassenger() == minecraft.player) {
                    ModPlatformClient.INSTANCE.sendClientToServerPacket(YeetC2SPacket.INSTANCE);
                }
            }
        });

    }
}
