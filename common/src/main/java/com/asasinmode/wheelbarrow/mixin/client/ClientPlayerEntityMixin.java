package com.asasinmode.wheelbarrow.mixin.client;

import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.asasinmode.wheelbarrow.entity.custom.WheelbarrowEntity;


@Mixin(LocalPlayer.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayer {
	public ClientPlayerEntityMixin(Minecraft client, ClientLevel world, ClientPacketListener networkHandler,
                                   StatsCounter stats, ClientRecipeBook recipeBook, boolean lastSneaking, boolean lastSprinting) {
		super(world, networkHandler.getLocalGameProfile());
	}
    @Shadow
    @Final
    protected Minecraft minecraft;

	@Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/Input;tick(ZF)V", shift = At.Shift.AFTER))
	private void wheelbarrowOnInputCall(CallbackInfo ci) {
		Entity riddenEntity = this.getVehicle();
		if (riddenEntity instanceof WheelbarrowEntity wheelbarrow) {
            LocalPlayer thisObject = (LocalPlayer) (Object) this;
			Input input = thisObject.input;
			wheelbarrow.setInputs(input.left, input.right, input.up, input.down,
					this.minecraft.options.keySprint.isDown(), input.jumping);
		}
	}
}
