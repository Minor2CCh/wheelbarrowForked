package com.asasinmode.wheelbarrow.platform;

import net.minecraft.Util;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import org.apache.commons.lang3.function.TriFunction;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public interface ModPlatform {
    ModPlatform INSTANCE = Util.make(() -> {
        final ServiceLoader<ModPlatform> loader = ServiceLoader.load(ModPlatform.class);
        final Iterator<ModPlatform> iterator = loader.iterator();
        if (!iterator.hasNext()) {
            throw new RuntimeException("Platform instance not found!");
        } else {
            final ModPlatform platform = iterator.next();
            if (iterator.hasNext()) {
                throw new RuntimeException("More than one platform instance was found!");
            }
            return platform;
        }
    });
    enum ModLoader{
        NEOFORGE,
        FABRIC
    }
    interface QuadConsumer<A, B, C, D> {
        void accept(A a, B b, C c, D d);
    }
    interface QuintConsumer<A, B, C, D, E> {
        void accept(A a, B b, C c, D d, E e);
    }
    interface SepFunction<A, B, C, D, E, F, G, H> {
        H accept(A a, B b, C c, D d, E e, F f, G g);
    }
    ModLoader getModLoader();
    Path getConfigPath();
    <T extends Item> Supplier<T> itemRegister(String id, Supplier<T> item);
    <T extends Entity> Supplier<EntityType<T>> entityRegister(String id, EntityType.Builder<T> entity);
    boolean isModLoaded(String id);
    void sendPacket(ServerPlayer player, CustomPacketPayload payLoad);
    void delayedInit(Runnable runnable);
    void insertCreativeModeTab(ResourceKey<CreativeModeTab> tab, Supplier<Item> insertItem, Supplier<Item> extraItem);
    void allowDamageEventRegister(TriFunction<LivingEntity, DamageSource, Float, Boolean> function);
}
