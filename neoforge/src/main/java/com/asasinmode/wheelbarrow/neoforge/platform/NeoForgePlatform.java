package com.asasinmode.wheelbarrow.neoforge.platform;

import com.asasinmode.wheelbarrow.Wheelbarrow;
import com.asasinmode.wheelbarrow.client.Keybinds;
import com.asasinmode.wheelbarrow.entity.custom.WheelbarrowEntity;
import com.asasinmode.wheelbarrow.networking.InformYeetKeybindS2CPacket;
import com.asasinmode.wheelbarrow.networking.YeetC2SPacket;
import com.asasinmode.wheelbarrow.platform.ModPlatform;
import com.google.auto.service.AutoService;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.commons.lang3.function.TriFunction;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

@AutoService(ModPlatform.class)
@EventBusSubscriber(modid = Wheelbarrow.MOD_ID)
public class NeoForgePlatform implements ModPlatform {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Wheelbarrow.MOD_ID);
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, Wheelbarrow.MOD_ID);
    private static final List<Runnable> DELAY_RUNNABLE_LIST = new ArrayList<>();
    private static final List<Map.Entry<ResourceKey<CreativeModeTab>, Map.Entry<Supplier<Item>, Supplier<Item>>>> INSERT_TAB_LIST = new ArrayList<>();
    private static final List<TriFunction<LivingEntity, DamageSource, Float, Boolean>> ALLOW_DAMAGE_FUNCTIONS = new ArrayList<>();
    @Override
    public ModLoader getModLoader() {
        return ModLoader.NEOFORGE;
    }
    @Override
    public <T extends Item> Supplier<T> itemRegister(String id, Supplier<T> item) {
        return ITEMS.register(id, item);
    }

    @Override
    public <T extends Entity> Supplier<EntityType<T>> entityRegister(String id, EntityType.Builder<T> entity) {
        return ENTITIES.register(id, () -> entity.build(id));
    }

    @Override
    public boolean isModLoaded(String id) {
        return ModList.get().isLoaded(id);
    }
    @Override
    public Path getConfigPath() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public void sendPacket(ServerPlayer player, CustomPacketPayload payLoad) {
        PacketDistributor.sendToPlayer(player, payLoad);
    }
    public static void registryInit(IEventBus modEventBus){
        ITEMS.register(modEventBus);
        ENTITIES.register(modEventBus);
    }
    public static void init(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> DELAY_RUNNABLE_LIST.forEach(Runnable::run));
    }
    @Override
    public void delayedInit(Runnable runnable){
        DELAY_RUNNABLE_LIST.add(runnable);
    }
    @Override
    public void insertCreativeModeTab(ResourceKey<CreativeModeTab> tab, Supplier<Item> insertItem, Supplier<Item> extraItem) {
        INSERT_TAB_LIST.add(Map.entry(tab, Map.entry(insertItem, extraItem)));

    }

    @SubscribeEvent
    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        for(Map.Entry<ResourceKey<CreativeModeTab>, Map.Entry<Supplier<Item>, Supplier<Item>>> map : INSERT_TAB_LIST){
            if(Objects.equals(event.getTabKey(), map.getKey())){
                event.insertAfter(
                        map.getValue().getKey().get().getDefaultInstance(),
                        map.getValue().getValue().get().getDefaultInstance(),
                        CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
                );
            }
        }
    }
    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");

        registrar.playToClient(
                InformYeetKeybindS2CPacket.PACKET_ID,
                InformYeetKeybindS2CPacket.PACKET_CODEC,
                (payload, context) -> {
                    context.player().displayClientMessage(
                            Component.translatable("key." + Wheelbarrow.MOD_ID + ".yeetTooltip", Keybinds.yeet.getTranslatedKeyMessage()),
                            true);

                    // クライアント側の処理
                }
        );
        registrar.playToServer(
                YeetC2SPacket.PACKET_ID,
                YeetC2SPacket.PACKET_CODEC,
                (payload, context) -> {
                    if (context.player().getVehicle() instanceof WheelbarrowEntity wheelbarrow
                            && wheelbarrow.getControllingPassenger() == context.player()) {
                        wheelbarrow.yeetLastPassenger();
                    }
                    // クライアント側の処理
                }
        );
    }
    @Override
    public void allowDamageEventRegister(TriFunction<LivingEntity, DamageSource, Float, Boolean> function) {
        ALLOW_DAMAGE_FUNCTIONS.add(function);
    }
    @SubscribeEvent
    public static void allowDamageEvent(LivingIncomingDamageEvent event){
        for(TriFunction<LivingEntity, DamageSource, Float, Boolean> function : ALLOW_DAMAGE_FUNCTIONS){
            boolean result = function.apply(event.getEntity(), event.getSource(), event.getAmount());
            if(!result){
                event.setAmount(0);
                event.setCanceled(true);
                break;
            }
        }
    }
}
