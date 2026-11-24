package com.rizhik.visuals.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.rizhik.visuals.client.gui.ClickGui;
import com.rizhik.visuals.client.gui.HudEditor;
import com.rizhik.visuals.client.gui.MarkersScreen;
import com.rizhik.visuals.client.module.ClickPearl;
import com.rizhik.visuals.client.module.Frame;
import com.rizhik.visuals.client.module.Module;
import com.rizhik.visuals.client.module.ModuleList;
import com.rizhik.visuals.client.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class RizhikVisualsClient implements ClientModInitializer {
	private boolean wasOurs;

	@Override
	public void onInitializeClient() {
		ModuleList.init();
		Config.load();

		KeyMapping.Category category = KeyMapping.Category.register(
				Identifier.fromNamespaceAndPath("rizhikvisuals", "main"));

		KeyMapping menuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.rizhikvisuals.menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));
		KeyMapping editorKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.rizhikvisuals.hudeditor", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_CONTROL, category));
		KeyMapping markersKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.rizhikvisuals.markers", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_ALT, category));
		ClickPearl.key = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.rizhikvisuals.clickpearl", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, category));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (menuKey.consumeClick()) client.setScreen(new ClickGui());
			while (editorKey.consumeClick()) client.setScreen(new HudEditor());
			while (markersKey.consumeClick()) client.setScreen(new MarkersScreen());
			for (Module m : ModuleManager.all()) {
				if (m.isEnabled()) m.onTick(client);
			}

			boolean ours = client.screen instanceof ClickGui || client.screen instanceof HudEditor;
			if (wasOurs && !ours) Config.save();
			wasOurs = ours;
		});

		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> Config.save());

		HudElementRegistry.addLast(
				Identifier.fromNamespaceAndPath("rizhikvisuals", "hud"),
				(graphics, tracker) -> {
					Frame.partial = tracker.getGameTimeDeltaPartialTick(false);
					for (Module m : ModuleManager.all()) {
						if (m.isEnabled()) m.renderHud(graphics);
					}
				});
	}
}
