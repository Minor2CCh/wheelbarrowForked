package com.asasinmode.wheelbarrow.fabric.client.platform;

import com.asasinmode.wheelbarrow.client.platform.ModPlatformClient;
import com.google.auto.service.AutoService;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.function.Consumer;

@AutoService(ModPlatformClient.class)
public class FabricPlatformClient implements ModPlatformClient{
    @Override
    public void registerEndTickEvents(Consumer<Minecraft> minecraftConsumer) {
        ClientTickEvents.END_CLIENT_TICK.register(minecraftConsumer::accept);
    }

    @Override
    public void sendClientToServerPacket(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }
}
