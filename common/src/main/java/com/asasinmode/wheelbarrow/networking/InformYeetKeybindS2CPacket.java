package com.asasinmode.wheelbarrow.networking;

import com.asasinmode.wheelbarrow.Wheelbarrow;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;


public class InformYeetKeybindS2CPacket implements CustomPacketPayload {
	public static final InformYeetKeybindS2CPacket INSTANCE = new InformYeetKeybindS2CPacket();
	public static final CustomPacketPayload.Type<InformYeetKeybindS2CPacket> PACKET_ID = new CustomPacketPayload.Type<>(
			Wheelbarrow.of("inform_yeet_keybind"));
	public static final StreamCodec<FriendlyByteBuf, InformYeetKeybindS2CPacket> PACKET_CODEC = StreamCodec
			.unit(INSTANCE);

	public InformYeetKeybindS2CPacket() {
	}

	public CustomPacketPayload.Type<InformYeetKeybindS2CPacket> type() {
		return PACKET_ID;
	}
}
