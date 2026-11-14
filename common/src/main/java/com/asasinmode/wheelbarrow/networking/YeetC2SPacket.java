package com.asasinmode.wheelbarrow.networking;

import com.asasinmode.wheelbarrow.Wheelbarrow;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;


public class YeetC2SPacket implements CustomPacketPayload {
	public static final YeetC2SPacket INSTANCE = new YeetC2SPacket();
	public static final CustomPacketPayload.Type<YeetC2SPacket> PACKET_ID = new CustomPacketPayload.Type<>(
			Wheelbarrow.of("yeet"));
	public static final StreamCodec<FriendlyByteBuf, YeetC2SPacket> PACKET_CODEC = StreamCodec
            .unit(INSTANCE);

	public YeetC2SPacket() {
	}

	public CustomPacketPayload.@NotNull Type<YeetC2SPacket> type() {
		return PACKET_ID;
	}
}
