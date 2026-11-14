package com.asasinmode.wheelbarrow;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;

import com.asasinmode.wheelbarrow.platform.ModPlatform;
import com.google.gson.GsonBuilder;

// inspired by https://github.com/newhoryzon/farmers-delight-fabric/blob/master/src/main/java/com/nhoryzon/mc/farmersdelight/Configuration.java
public class Config {
	private static final File CONFIG_FILE = new File(ModPlatform.INSTANCE.getConfigPath().toFile(),
			Wheelbarrow.MOD_ID + ".json");

	private Integer maxPassengers;
    private Boolean immuneWallDamage;
    private Boolean placeableInWater;
    public static final int DEFAULT_MAX_PASSENGERS = 1;
    public static final boolean DEFAULT_IMMUNE_WALL_DAMAGE = false;
    public static final boolean PLACEABLE_IN_WATER = false;

	private Config() {
	}
    public static Config defaultConfig(){
        Config config = new Config();
        config.maxPassengers = DEFAULT_MAX_PASSENGERS;
        config.immuneWallDamage = DEFAULT_IMMUNE_WALL_DAMAGE;
        config.placeableInWater = PLACEABLE_IN_WATER;
        return config;
    }
    private boolean nullToDefault(){
        boolean isFixed = false;
        if(maxPassengers == null){
            maxPassengers = DEFAULT_MAX_PASSENGERS;
            isFixed = true;
        }
        if(immuneWallDamage == null){
            immuneWallDamage = DEFAULT_IMMUNE_WALL_DAMAGE;
            isFixed = true;
        }
        if(placeableInWater == null){
            placeableInWater = PLACEABLE_IN_WATER;
            isFixed = true;
        }
        return isFixed;
    }
	public static Config load() {
		if (!CONFIG_FILE.exists()) {
			save(defaultConfig());
		}
        Config config;
		Reader reader;
		try {
			reader = Files.newBufferedReader(CONFIG_FILE.toPath());
			config = (new GsonBuilder().setPrettyPrinting().create()).fromJson(reader, Config.class);
            if(config.nullToDefault()){
                save(config);
            }
			reader.close();
		} catch (IOException e) {
			Wheelbarrow.LOGGER.error("Failed to load the configuration file. Default configuration used.", e);
            save(defaultConfig());
            config = defaultConfig();
		}

		return config;
	}

	public static void save(Config config) {
		try {
			Writer writer = Files.newBufferedWriter(CONFIG_FILE.toPath());
			(new GsonBuilder().setPrettyPrinting().create()).toJson(config, writer);
			writer.close();
		} catch (IOException e) {
			Wheelbarrow.LOGGER.error("Failed to save configuration file.", e);
		}
	}

	public int getMaxPassengers() {
		return this.maxPassengers;
	}

	public void setMaxPassengers(int value) {
		this.maxPassengers = Math.clamp(value, 0, Integer.MAX_VALUE);
	}

    public boolean getImmuneWallDamage() {
        return this.immuneWallDamage;
    }

    public void setImmuneWallDamage(boolean value) {
        this.immuneWallDamage = value;
    }
    public boolean getPlaceableInWater() {
        return this.placeableInWater;
    }

    public void setPlaceableInWater(boolean value) {
        this.placeableInWater = value;
    }
}
