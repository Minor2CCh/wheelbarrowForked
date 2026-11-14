package com.asasinmode.wheelbarrow.client.render;

import java.util.Map;

import com.asasinmode.wheelbarrow.Wheelbarrow;
import com.asasinmode.wheelbarrow.client.model.WheelbarrowEntityModel;
import com.asasinmode.wheelbarrow.entity.custom.WheelbarrowEntity;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;


public class WheelbarrowEntityRenderer extends EntityRenderer<WheelbarrowEntity> {
	private static final Map<WheelbarrowEntity.Type, ResourceLocation> OXIDATION_LEVEL_TO_TEXTURE = Util
			.make(Maps.newEnumMap(WheelbarrowEntity.Type.class), (map) -> {
				map.put(WheelbarrowEntity.Type.COPPER,
						Wheelbarrow.of("textures/entity/copper_wheelbarrow.png"));
				map.put(WheelbarrowEntity.Type.EXPOSED,
                        Wheelbarrow.of("textures/entity/exposed_copper_wheelbarrow.png"));
				map.put(WheelbarrowEntity.Type.WEATHERED,
                        Wheelbarrow.of("textures/entity/weathered_copper_wheelbarrow.png"));
				map.put(WheelbarrowEntity.Type.OXIDIZED,
                        Wheelbarrow.of("textures/entity/oxidized_copper_wheelbarrow.png"));
			});

	private final WheelbarrowEntityModel model;

	public WheelbarrowEntityRenderer(EntityRendererProvider.Context context) {
		super(context);

		this.shadowRadius = 0.7f;
		this.model = new WheelbarrowEntityModel(WheelbarrowEntityModel.getTexturedModelData().bakeRoot());
	}

	@Override
	public @NotNull ResourceLocation getTextureLocation(WheelbarrowEntity entity) {
		return OXIDATION_LEVEL_TO_TEXTURE.get(entity.getOxidationLevel());
	}

	@Override
	public void render(WheelbarrowEntity entity, float yaw, float tickDelta, PoseStack matrixStack,
                       @NotNull MultiBufferSource vertexConsumers, int light) {
		matrixStack.pushPose();

		matrixStack.mulPose(Axis.YP.rotationDegrees(180.0f - yaw));

		float damageWobbleTicks = (float) entity.getHurtTime() - tickDelta;
		float damageWobbleStrength = entity.getDamage() - tickDelta;
		if (damageWobbleStrength < 0.0F) {
			damageWobbleStrength = 0.0F;
		}

		if (damageWobbleTicks > 0.0F) {
			matrixStack.mulPose(Axis.ZP
					.rotationDegrees(Mth.sin(damageWobbleTicks) * damageWobbleTicks
							* damageWobbleStrength / 15.0F * (float) entity.getHurtDir()));
		}

		matrixStack.mulPose(Axis.ZP.rotationDegrees(180));
		matrixStack.translate(0, -1.5, 0);

		float ageInTicks = this.getAnimationProgress(entity, tickDelta);
		float limbSwing = entity.limbAnimator.position(tickDelta);
		float limbSwingAmount = entity.limbAnimator.speed(tickDelta);

		if (limbSwingAmount > 1.0f) {
			limbSwingAmount = 1.0f;
		}

		this.model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, 0.0f, 0.0f);

		this.model.renderToBuffer(matrixStack, vertexConsumers.getBuffer(RenderType.entityTranslucent(getTextureLocation(entity))), light,
				OverlayTexture.NO_OVERLAY);

		matrixStack.popPose();
		super.render(entity, yaw, tickDelta, matrixStack, vertexConsumers, light);
	}

	protected float getAnimationProgress(WheelbarrowEntity entity, float tickDelta) {
		return (float) entity.tickCount + tickDelta;
	}
}
