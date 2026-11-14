package com.asasinmode.wheelbarrow.entity;

import com.asasinmode.wheelbarrow.entity.custom.*;
import com.asasinmode.wheelbarrow.platform.ModPlatform;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.function.Supplier;


public final class ModEntities {
    private ModEntities(){}
	public static final Supplier<EntityType<WheelbarrowEntity>> WHEELBARROW = registerEntity(
            "wheelbarrow",
			EntityType.Builder
                    .of(WheelbarrowEntity::new, MobCategory.MISC)
					.sized(0.98f, 0.875f));

    private static <T extends Entity> Supplier<EntityType<T>> registerEntity(String name, EntityType.Builder<T> entity){
        return ModPlatform.INSTANCE.entityRegister(name, entity);
    }
	public static void registerModEntities() {
	}
}
