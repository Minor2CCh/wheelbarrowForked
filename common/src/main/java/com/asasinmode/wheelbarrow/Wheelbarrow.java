package com.asasinmode.wheelbarrow;

import com.asasinmode.wheelbarrow.entity.custom.WheelbarrowEntity;
import com.asasinmode.wheelbarrow.item.ModItems;
import com.asasinmode.wheelbarrow.platform.ModPlatform;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asasinmode.wheelbarrow.entity.ModEntities;


public final class Wheelbarrow{
    private Wheelbarrow(){}
	public static final String MOD_ID = "wheelbarrow";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Config CONFIG = Config.load();

	public static void init() {
		ModItems.registerModItems();
		ModEntities.registerModEntities();
        ModPlatform.INSTANCE.allowDamageEventRegister((entity, source, amount) -> {
            if(source.is(DamageTypes.IN_WALL) && CONFIG.getImmuneWallDamage()){
                if(entity instanceof LivingEntity){
                    Entity vehicleEntity = entity.getVehicle();
                    while(vehicleEntity != null){
                        if(vehicleEntity instanceof WheelbarrowEntity){
                            return false;
                        }
                        vehicleEntity = vehicleEntity.getVehicle();
                    }
                }
            }

            return true;
        });
	}

    public static ResourceLocation of(String id){
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, id);
    }
}
