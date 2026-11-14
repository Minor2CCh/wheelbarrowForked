package com.asasinmode.wheelbarrow.fabric.platform;

import com.asasinmode.wheelbarrow.Wheelbarrow;
import com.asasinmode.wheelbarrow.networking.InformYeetKeybindS2CPacket;
import com.asasinmode.wheelbarrow.platform.ModPlatform;
import com.google.auto.service.AutoService;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import org.apache.commons.lang3.function.TriFunction;

import java.nio.file.Path;
import java.util.function.Supplier;

@AutoService(ModPlatform.class)
public class FabricPlatform implements ModPlatform {
    @Override
    public ModLoader getModLoader() {
        return ModLoader.FABRIC;
    }
    @Override
    public <T extends Item> Supplier<T> itemRegister(String id, Supplier<T> item) {
        T itemInfo = item.get();
        ResourceLocation blockID = Wheelbarrow.of(id);
        Registry.register(BuiltInRegistries.ITEM, blockID, itemInfo);
        return () -> itemInfo;
    }
    public <T extends Entity> Supplier<EntityType<T>> entityRegister(String id, EntityType.Builder<T> entity){
        ResourceLocation entityID = Wheelbarrow.of(id);
        EntityType<T> entityData = Registry.register(BuiltInRegistries.ENTITY_TYPE, entityID, entity.build(id));
        return () -> entityData;
    }
    @Override
    public boolean isModLoaded(String id) {
        return FabricLoader.getInstance().isModLoaded(id);
    }
    @Override
    public Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public void sendPacket(ServerPlayer player, CustomPacketPayload payLoad) {
        ServerPlayNetworking.send(player, InformYeetKeybindS2CPacket.INSTANCE);

    }
    @Override
    public void delayedInit(Runnable runnable){
        runnable.run();
    }

    @Override
    public void insertCreativeModeTab(ResourceKey<CreativeModeTab> tab, Supplier<Item> insertItem, Supplier<Item> extraItem) {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(content -> content.addAfter(insertItem.get(), extraItem.get()));
    }
    @Override
    public void allowDamageEventRegister(TriFunction<LivingEntity, DamageSource, Float, Boolean> function) {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(function::apply);
    }
}
