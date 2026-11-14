package com.asasinmode.wheelbarrow.fabric.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import com.asasinmode.wheelbarrow.entity.custom.WheelbarrowEntity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;


@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>>
		extends EntityRenderer<T>
		implements RenderLayerParent<T, M> {

	public LivingEntityRendererMixin(EntityRendererProvider.Context ctx, M model, float shadowRadius) {
		super(ctx);
	}

	@ModifyExpressionValue(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isPassenger()Z", ordinal = 2))
	private boolean modifyRenderLimbAnimationCheck(boolean original, T livingEntity, float yaw, float tickDelta,
                                                   PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light) {
		// make the default implementation calculate the player walking values when they
		// are controlling a wheelbarrow. Output is negated, hence false
		if (livingEntity instanceof Player && livingEntity.getVehicle() instanceof WheelbarrowEntity wheelbarrow
				&& wheelbarrow.getControllingPassenger() == livingEntity) {
			return false;
		}

		return original;
	}

	@ModifyArgs(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;setupAnim(Lnet/minecraft/world/entity/Entity;FFFFF)V"))
	private void modifyRenderSetAnglesArguments(Args args, T livingEntity, float yaw, float tickDelta,
                                                PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light) {
		if (livingEntity instanceof Player && livingEntity.getVehicle() instanceof WheelbarrowEntity wheelbarrow
				&& wheelbarrow.getControllingPassenger() == livingEntity) {
			// the default values from the above are too small
			float limbSwing = args.<Float>get(1) * 8.0f;
			float limbSwingAmount = args.<Float>get(2) * 8.0f;

			if (limbSwingAmount > 1.0f) {
				limbSwingAmount = 1.0f;
			}

			args.set(1, limbSwing);
			args.set(2, limbSwingAmount);
		}
	}

	@ModifyArgs(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;rotLerp(FFF)F", ordinal = 0))
	private void modifyRenderLerpBodyYaw(Args args, T livingEntity, float yaw, float tickDelta,
                                         PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light) {
		if (livingEntity instanceof Player && livingEntity.getVehicle() instanceof WheelbarrowEntity wheelbarrow
				&& wheelbarrow.getControllingPassenger() == livingEntity) {
			// make body yaw slightly preceed the wheelbarrow yaw when turning
			float prevVelocityModifier = wheelbarrow.getPrevYawVelocity() * 2.5f;
			float velocityModifier = wheelbarrow.getYawVelocity() * 2.5f;

			args.set(1, args.<Float>get(1) - prevVelocityModifier);
			args.set(2, args.<Float>get(2) - velocityModifier);
		}
	}
}
