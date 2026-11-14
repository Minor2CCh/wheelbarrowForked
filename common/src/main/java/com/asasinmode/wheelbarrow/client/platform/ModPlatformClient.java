package com.asasinmode.wheelbarrow.client.platform;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.function.Consumer;

public interface ModPlatformClient {
    ModPlatformClient INSTANCE = Util.make(() -> {
        final ServiceLoader<ModPlatformClient> loader = ServiceLoader.load(ModPlatformClient.class);
        final Iterator<ModPlatformClient> iterator = loader.iterator();
        if (!iterator.hasNext()) {
            throw new RuntimeException("Platform instance not found!");
        } else {
            final ModPlatformClient platform = iterator.next();
            if (iterator.hasNext()) {
                throw new RuntimeException("More than one platform instance was found!");
            }
            return platform;
        }
    });
    void registerEndTickEvents(Consumer<Minecraft> minecraftConsumer);
    void sendClientToServerPacket(CustomPacketPayload payload);

}
