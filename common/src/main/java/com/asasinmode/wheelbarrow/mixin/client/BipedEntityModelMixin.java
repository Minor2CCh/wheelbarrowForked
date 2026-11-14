package com.asasinmode.wheelbarrow.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.asasinmode.wheelbarrow.entity.custom.WheelbarrowEntity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;


@Mixin(HumanoidModel.class)
public abstract class BipedEntityModelMixin<T extends LivingEntity, M extends EntityModel<T>> extends AgeableListModel<T>
		implements ArmedModel, HeadedModel {
	@NotNull
	@Shadow
	protected abstract Iterable<ModelPart> headParts();

	@NotNull
	@Shadow
	protected abstract Iterable<ModelPart> bodyParts();

	@NotNull
	@Shadow
	public abstract ModelPart getHead();

	@Shadow
	public abstract void translateToHand(HumanoidArm humanoidArm, PoseStack poseStack);

	@ModifyExpressionValue(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/HumanoidModel;riding:Z", opcode = Opcodes.GETFIELD))
	private boolean modifySetAnglesRidingCheck(boolean original, T entity, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		Entity vehicle = entity.getVehicle();

		if (entity instanceof Player && vehicle instanceof WheelbarrowEntity wheelbarrow) {
			Entity controllingPassenger = wheelbarrow.getControllingPassenger();
			return original && entity != controllingPassenger;
		}

		return original;
	}

	@Redirect(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/geom/ModelPart;xRot:F", opcode = Opcodes.PUTFIELD, ordinal = 4))
	private void updateRightArmPitch(ModelPart arm, float value, LivingEntity entity) {
		if (entity instanceof Player && entity.getVehicle() instanceof WheelbarrowEntity wheelbarrow
				&& wheelbarrow.getControllingPassenger() == entity) {
			value = 0.0f;
		}
		arm.xRot = value;
	}

	@Redirect(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/geom/ModelPart;xRot:F", opcode = Opcodes.PUTFIELD, ordinal = 5))
	private void updateLeftArmPitch(ModelPart arm, float value, LivingEntity entity) {
		if (entity instanceof Player && entity.getVehicle() instanceof WheelbarrowEntity wheelbarrow
				&& wheelbarrow.getControllingPassenger() == entity) {
			value = 0.0f;
		}
		arm.xRot = value;
	}

	@Redirect(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/geom/ModelPart;zRot:F", opcode = Opcodes.PUTFIELD, ordinal = 0))
	private void updateRightArmRoll(ModelPart arm, float value, LivingEntity entity) {
		if (entity instanceof Player && entity.getVehicle() instanceof WheelbarrowEntity wheelbarrow
				&& wheelbarrow.getControllingPassenger() == entity) {
			value = 0.15f;
		}
		arm.zRot = value;
	}

	@Redirect(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/geom/ModelPart;zRot:F", opcode = Opcodes.PUTFIELD, ordinal = 1))
	private void updateLeftArmRoll(ModelPart arm, float value, LivingEntity entity) {
		if (entity instanceof Player && entity.getVehicle() instanceof WheelbarrowEntity wheelbarrow
				&& wheelbarrow.getControllingPassenger() == entity) {
			value = -0.15f;
		}
		arm.zRot = value;
	}
}
