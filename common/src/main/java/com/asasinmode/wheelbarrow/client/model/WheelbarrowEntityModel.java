package com.asasinmode.wheelbarrow.client.model;

import com.asasinmode.wheelbarrow.entity.custom.WheelbarrowEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.player.Player;


public class WheelbarrowEntityModel extends EntityModel<WheelbarrowEntity> {
	private final ModelPart top;
	private final ModelPart front;
	private final ModelPart back;
	private final ModelPart left;
	private final ModelPart right;
	private final ModelPart bottom;
	private final ModelPart wheel;

	public WheelbarrowEntityModel(ModelPart root) {
		this.top = root.getChild("top");
		this.front = this.top.getChild("front");
		this.back = this.top.getChild("back");
		this.left = this.top.getChild("left");
		this.right = this.top.getChild("right");
		this.bottom = this.top.getChild("bottom");
		this.wheel = root.getChild("wheel");
	}

	public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        PartDefinition top = modelPartData.addOrReplaceChild("top", CubeListBuilder.create(),
                PartPose.offset(0.0F, 20.0F, -4.0F));

        top.addOrReplaceChild("front",
                CubeListBuilder.create().texOffs(4, 43).addBox(6.0F, -4.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
						.texOffs(2, 37).addBox(6.0F, -6.0F, -4.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
						.texOffs(0, 29).addBox(6.0F, -8.0F, -6.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
						.texOffs(2, 37).addBox(-8.0F, -6.0F, -4.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
						.texOffs(4, 43).addBox(-8.0F, -4.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
						.texOffs(0, 29).addBox(-8.0F, -8.0F, -6.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
						.texOffs(0, 60).addBox(-8.0F, -2.0F, -2.0F, 16.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
						.texOffs(0, 56).addBox(-8.0F, -4.0F, -4.0F, 16.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
						.texOffs(0, 52).addBox(-8.0F, -6.0F, -6.0F, 16.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
						.texOffs(0, 48).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -2.0F, -4.0F));

        top.addOrReplaceChild("back",
                CubeListBuilder.create().texOffs(0, 19).addBox(-8.0F, -8.0F, -1.0F, 16.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
						.texOffs(44, 40).addBox(-8.0F, -8.0F, 1.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
						.texOffs(44, 40).addBox(6.0F, -8.0F, 1.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -2.0F, 11.0F));

        top.addOrReplaceChild("left",
                CubeListBuilder.create().texOffs(32, 18).addBox(-1.0F, -8.0F, -7.0F, 2.0F, 8.0F, 14.0F, new CubeDeformation(0.0F)),
                PartPose.offset(7.0F, -2.0F, 3.0F));

        top.addOrReplaceChild("right",
                CubeListBuilder.create().texOffs(32, 18).addBox(-1.0F, -8.0F, -7.0F, 2.0F, 8.0F, 14.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-7.0F, -2.0F, 3.0F));

        top.addOrReplaceChild("bottom",
                CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -2.0F, -8.0F, 16.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
						.texOffs(0, 8).addBox(5.0F, 0.0F, 5.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(0, 8).addBox(-6.0F, 0.0F, 5.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 4.0F));

        modelPartData.addOrReplaceChild("wheel",
                CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 22.0F, -4.0F));
		return LayerDefinition.create(modelData, 64, 64);
	}

	@Override
	public void setupAnim(WheelbarrowEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		// pitch it slightly forward when controlled
		// don't know why it can't be just 3.0f
		if (entity.getControllingPassenger() instanceof Player) {
			this.top.xRot = 132.0f;
		} else {
			this.top.xRot = 0.0f;
		}

		this.wheel.xRot = limbSwing;
	}

	@Override
	public void renderToBuffer(PoseStack matrices, VertexConsumer vertexConsumer, int light, int overlay, int color) {
		top.render(matrices, vertexConsumer, light, overlay, color);
		wheel.render(matrices, vertexConsumer, light, overlay, color);
	}
}
