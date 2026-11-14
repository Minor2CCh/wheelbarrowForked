package com.asasinmode.wheelbarrow.neoforge.client.platform;

import com.asasinmode.wheelbarrow.Wheelbarrow;
import com.asasinmode.wheelbarrow.client.platform.ModPlatformClient;
import com.google.auto.service.AutoService;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@EventBusSubscriber(modid = Wheelbarrow.MOD_ID, value = Dist.CLIENT)
@AutoService(ModPlatformClient.class)
public class NeoForgePlatformClient implements ModPlatformClient{
    private static final List<Consumer<Minecraft>> END_TICK_EVENTS = new ArrayList<>();
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        for(Consumer<Minecraft> tickEvent : END_TICK_EVENTS){
            tickEvent.accept(client);
        }
        // 処理
    }

    @Override
    public void registerEndTickEvents(Consumer<Minecraft> minecraftConsumer) {
        END_TICK_EVENTS.add(minecraftConsumer);
    }

    @Override
    public void sendClientToServerPacket(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }

}
