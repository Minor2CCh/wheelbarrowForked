package com.asasinmode.wheelbarrow.item;

import com.asasinmode.wheelbarrow.item.custom.*;
import com.asasinmode.wheelbarrow.entity.custom.WheelbarrowEntity;
import com.asasinmode.wheelbarrow.platform.ModPlatform;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.function.Supplier;


public final class ModItems {
    private ModItems(){}
	public static final Supplier<Item> COPPER_WHEELBARROW = registerItem("copper_wheelbarrow",
            () -> new WheelbarrowItem(WheelbarrowEntity.Type.COPPER, new Item.Properties().stacksTo(1)));
	public static final Supplier<Item> EXPOSED_COPPER_WHEELBARROW = registerItem("exposed_copper_wheelbarrow",
            () -> new WheelbarrowItem(WheelbarrowEntity.Type.EXPOSED, new Item.Properties().stacksTo(1)));
	public static final Supplier<Item> WEATHERED_COPPER_WHEELBARROW = registerItem("weathered_copper_wheelbarrow",
            () -> new WheelbarrowItem(WheelbarrowEntity.Type.WEATHERED, new Item.Properties().stacksTo(1)));
	public static final Supplier<Item> OXIDIZED_COPPER_WHEELBARROW = registerItem("oxidized_copper_wheelbarrow",
            () -> new WheelbarrowItem(WheelbarrowEntity.Type.OXIDIZED, new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> WAXED_COPPER_WHEELBARROW = registerItem("waxed_copper_wheelbarrow",
            () -> new WheelbarrowItem(WheelbarrowEntity.Type.COPPER, new Item.Properties().stacksTo(1), true));
    public static final Supplier<Item> WAXED_EXPOSED_COPPER_WHEELBARROW = registerItem("waxed_exposed_copper_wheelbarrow",
            () -> new WheelbarrowItem(WheelbarrowEntity.Type.EXPOSED, new Item.Properties().stacksTo(1), true));
    public static final Supplier<Item> WAXED_WEATHERED_COPPER_WHEELBARROW = registerItem("waxed_weathered_copper_wheelbarrow",
            () -> new WheelbarrowItem(WheelbarrowEntity.Type.WEATHERED, new Item.Properties().stacksTo(1), true));
    public static final Supplier<Item> WAXED_OXIDIZED_COPPER_WHEELBARROW = registerItem("waxed_oxidized_copper_wheelbarrow",
            () -> new WheelbarrowItem(WheelbarrowEntity.Type.OXIDIZED, new Item.Properties().stacksTo(1), true));

	private static <T extends Item> Supplier<T> registerItem(String name, Supplier<T>  item) {
		return ModPlatform.INSTANCE.itemRegister(name, item);
	}

	public static void registerModItems() {
        ModPlatform.INSTANCE.insertCreativeModeTab(TOOLS_AND_UTILITIES, () -> Items.TNT_MINECART, COPPER_WHEELBARROW);
        ModPlatform.INSTANCE.insertCreativeModeTab(TOOLS_AND_UTILITIES, COPPER_WHEELBARROW, EXPOSED_COPPER_WHEELBARROW);
        ModPlatform.INSTANCE.insertCreativeModeTab(TOOLS_AND_UTILITIES, EXPOSED_COPPER_WHEELBARROW, WEATHERED_COPPER_WHEELBARROW);
        ModPlatform.INSTANCE.insertCreativeModeTab(TOOLS_AND_UTILITIES, WEATHERED_COPPER_WHEELBARROW, OXIDIZED_COPPER_WHEELBARROW);
        ModPlatform.INSTANCE.insertCreativeModeTab(TOOLS_AND_UTILITIES, OXIDIZED_COPPER_WHEELBARROW, WAXED_COPPER_WHEELBARROW);
        ModPlatform.INSTANCE.insertCreativeModeTab(TOOLS_AND_UTILITIES, WAXED_COPPER_WHEELBARROW, WAXED_EXPOSED_COPPER_WHEELBARROW);
        ModPlatform.INSTANCE.insertCreativeModeTab(TOOLS_AND_UTILITIES, WAXED_EXPOSED_COPPER_WHEELBARROW, WAXED_WEATHERED_COPPER_WHEELBARROW);
        ModPlatform.INSTANCE.insertCreativeModeTab(TOOLS_AND_UTILITIES, WAXED_WEATHERED_COPPER_WHEELBARROW, WAXED_OXIDIZED_COPPER_WHEELBARROW);
	}
    private static final ResourceKey<CreativeModeTab> TOOLS_AND_UTILITIES = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ResourceLocation.withDefaultNamespace("tools_and_utilities"));
}
