package com.asasinmode.wheelbarrow.fabric.client;

import com.asasinmode.wheelbarrow.Wheelbarrow;
import com.asasinmode.wheelbarrow.client.Keybinds;
import com.asasinmode.wheelbarrow.client.WheelbarrowClient;
import com.asasinmode.wheelbarrow.entity.ModEntities;
import com.asasinmode.wheelbarrow.networking.InformYeetKeybindS2CPacket;
import com.asasinmode.wheelbarrow.client.render.WheelbarrowEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

public class WheelbarrowClientFabric implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
        for(KeyMapping keyMapping : WheelbarrowClient.REGISTER_KEYMAPPING_LIST){
            KeyBindingHelper.registerKeyBinding(keyMapping);
        }
        WheelbarrowClient.init();
		EntityRendererRegistry.register(ModEntities.WHEELBARROW.get(), WheelbarrowEntityRenderer::new);
		registerS2CPackets();
	}

	private static void registerS2CPackets() {
		ClientPlayNetworking.registerGlobalReceiver(InformYeetKeybindS2CPacket.PACKET_ID,
				(payload, context) -> {
					// TODO this gets overriden by dismount message
					context.player().displayClientMessage(
							Component.translatable("key." + Wheelbarrow.MOD_ID + ".yeetTooltip", Keybinds.yeet.getTranslatedKeyMessage()),
							true);
				});
	}
}
