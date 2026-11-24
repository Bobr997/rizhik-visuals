package com.rizhik.visuals.client.module;

import net.minecraft.client.Minecraft;

import java.util.List;

public class Watermark extends HudModule {
    private final Setting<Boolean> showFps = add(new Setting<Boolean>("Показывать FPS", true));

    public Watermark() {
        super("Watermark",
                "Плашка с названием клиента и FPS в углу экрана. Показывает, что клиент работает.",
                6, 6);
    }

    @Override
    protected List<String> lines() {
        Minecraft mc = Minecraft.getInstance();
        return List.of(showFps.get()
                ? "Rizhik Visuals | " + mc.getFps() + " FPS"
                : "Rizhik Visuals");
    }
}
