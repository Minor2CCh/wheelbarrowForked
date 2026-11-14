package com.asasinmode.wheelbarrow.fabric;

import com.asasinmode.wheelbarrow.Wheelbarrow;
import com.asasinmode.wheelbarrow.entity.custom.WheelbarrowEntity;
import com.asasinmode.wheelbarrow.networking.InformYeetKeybindS2CPacket;
import com.asasinmode.wheelbarrow.networking.YeetC2SPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class WheelbarrowFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Wheelbarrow.init();
        PayloadTypeRegistry.playC2S().register(YeetC2SPacket.PACKET_ID, YeetC2SPacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(InformYeetKeybindS2CPacket.PACKET_ID,
                InformYeetKeybindS2CPacket.PACKET_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(YeetC2SPacket.PACKET_ID, (payload, context) -> {
            if (context.player().getVehicle() instanceof WheelbarrowEntity wheelbarrow
                    && wheelbarrow.getControllingPassenger() == context.player()) {
                wheelbarrow.yeetLastPassenger();
            }
        });
    }
}
