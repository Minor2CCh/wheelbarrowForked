package com.asasinmode.wheelbarrow.client.compat;

import com.asasinmode.wheelbarrow.Config;
import com.asasinmode.wheelbarrow.Wheelbarrow;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class WheelbarrowConfigScreen {
    public static Screen buildConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setSavingRunnable(() -> Config.save(Wheelbarrow.CONFIG))
                .setTitle(Component.literal("Wheelbarrow Config"));

        ConfigCategory category = builder.getOrCreateCategory(Component.literal("Default"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        category.addEntry(createEntry(
                entryBuilder,
                "maxPassengers",
                () -> Wheelbarrow.CONFIG.getMaxPassengers(),
                newValue -> Wheelbarrow.CONFIG.setMaxPassengers(newValue),
                Config.DEFAULT_MAX_PASSENGERS));
        category.addEntry(createEntry(
                entryBuilder,
                "immuneWallDamage",
                () -> Wheelbarrow.CONFIG.getImmuneWallDamage(),
                newValue -> Wheelbarrow.CONFIG.setImmuneWallDamage(newValue),
                Config.DEFAULT_IMMUNE_WALL_DAMAGE));
        category.addEntry(createEntry(
                entryBuilder,
                "placeableInWater",
                () -> Wheelbarrow.CONFIG.getPlaceableInWater(),
                newValue -> Wheelbarrow.CONFIG.setPlaceableInWater(newValue),
                Config.PLACEABLE_IN_WATER));

        return builder.build();
    }

    private static TooltipListEntry<Integer> createEntry(ConfigEntryBuilder builder, String titleTranslationKey,
                                                  Supplier<Integer> current, Consumer<Integer> saver, Integer defaultValue) {
        return builder
                .startIntField(Component.translatable("config." + Wheelbarrow.MOD_ID + "." + titleTranslationKey), current.get())
                .setSaveConsumer(saver)
                .setDefaultValue(defaultValue)
                .build();
    }
    private static TooltipListEntry<Boolean> createEntry(ConfigEntryBuilder builder, String titleTranslationKey,
                                                         Supplier<Boolean> current, Consumer<Boolean> saver, Boolean defaultValue) {
        return builder
                .startBooleanToggle(Component.translatable("config." + Wheelbarrow.MOD_ID + "." + titleTranslationKey), current.get())
                .setSaveConsumer(saver)
                .setDefaultValue(defaultValue)
                .build();
    }
}
