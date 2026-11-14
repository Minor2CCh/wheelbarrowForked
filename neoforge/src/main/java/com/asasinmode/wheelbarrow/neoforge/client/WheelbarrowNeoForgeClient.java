package com.asasinmode.wheelbarrow.neoforge.client;


import com.asasinmode.wheelbarrow.Wheelbarrow;
import com.asasinmode.wheelbarrow.client.WheelbarrowClient;
import com.asasinmode.wheelbarrow.client.render.WheelbarrowEntityRenderer;
import com.asasinmode.wheelbarrow.entity.ModEntities;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = Wheelbarrow.MOD_ID, value = Dist.CLIENT)
public class WheelbarrowNeoForgeClient {
    @SubscribeEvent
    public static void init(FMLCommonSetupEvent event) {
        WheelbarrowClient.init();

        event.enqueueWork(() -> {
        });
    }
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.WHEELBARROW.get(), WheelbarrowEntityRenderer::new);
    }
    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        for(KeyMapping keyMapping : WheelbarrowClient.REGISTER_KEYMAPPING_LIST){
            event.register(keyMapping);
        }
    }
}
