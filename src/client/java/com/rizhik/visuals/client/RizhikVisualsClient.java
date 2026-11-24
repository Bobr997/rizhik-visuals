package com.rizhik.visuals.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.rizhik.visuals.client.gui.ClickGui;
import com.rizhik.visuals.client.module.ModuleList;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class RizhikVisualsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModuleList.init();

		KeyMapping.Category category = KeyMapping.Category.register(
				Identifier.fromNamespaceAndPath("rizhikvisuals", "main"));

		KeyMapping menuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.rizhikvisuals.menu",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_RIGHT_SHIFT,
				category));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (menuKey.consumeClick()) {
				client.setScreen(new ClickGui());
			}
		});
	}
}
