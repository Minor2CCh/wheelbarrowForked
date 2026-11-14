package com.asasinmode.wheelbarrow.item.custom;

import com.asasinmode.wheelbarrow.Wheelbarrow;
import com.asasinmode.wheelbarrow.entity.custom.WheelbarrowEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;


public class WheelbarrowItem extends Item {
	private final WheelbarrowEntity.Type type;
    private final boolean isWaxed;

	public WheelbarrowItem(WheelbarrowEntity.Type type, Item.Properties settings) {
		super(settings);
		this.type = type;
        this.isWaxed = false;
	}
    public WheelbarrowItem(WheelbarrowEntity.Type type, Item.Properties settings, boolean isWaxed) {
        super(settings);
        this.type = type;
        this.isWaxed = isWaxed;
    }

	public @NotNull InteractionResult useOn(UseOnContext context) {
		Level world = context.getLevel();
		BlockPos blockPos = context.getClickedPos();
		Vec3 hitPos = context.getClickLocation();
		ItemStack itemStack = context.getItemInHand();
		Direction side = context.getClickedFace();
		float userYaw = context.getRotation();

		if (side != Direction.UP) {
			return InteractionResult.PASS;
		}

		if (world instanceof ServerLevel serverWorld) {

            WheelbarrowEntity wheelbarrowEntity = WheelbarrowEntity.create(this.type, this.isWaxed, serverWorld,
					hitPos.x(), hitPos.y(), hitPos.z(),
					itemStack, context.getPlayer());

			if (!world.noCollision(wheelbarrowEntity, wheelbarrowEntity.getBoundingBox())
					|| !world.getBlockState(blockPos.above()).isAir()
                    && (!Wheelbarrow.CONFIG.getPlaceableInWater() || !world.isWaterAt(blockPos.above()))) {
				return InteractionResult.PASS;
			}

			wheelbarrowEntity.setYRot(userYaw);
			serverWorld.addFreshEntity(wheelbarrowEntity);
			serverWorld.gameEvent(GameEvent.ENTITY_PLACE, blockPos,
					GameEvent.Context.of(context.getPlayer(), serverWorld.getBlockState(blockPos)));
            itemStack.consume(1, context.getPlayer());
		}

		return InteractionResult.sidedSuccess(world.isClientSide);
	}
}
