package com.asasinmode.wheelbarrow.client;

import com.asasinmode.wheelbarrow.Wheelbarrow;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class Keybinds {
    private Keybinds(){}
	public static final KeyMapping yeet = new KeyMapping(
			"key." + Wheelbarrow.MOD_ID + ".yeet",
			GLFW.GLFW_KEY_Z,
			"key.categories." + Wheelbarrow.MOD_ID);

}
