package com.rizhik.visuals.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class SkyColor extends Module {
    public static SkyColor INSTANCE;
    public static boolean hooked;

    private final ColorSetting hue = add(new ColorSetting("Цвет", 0.78f));
    private final Setting<Double> brightness = add(new Setting<Double>("Яркость", 80.0, 20, 100));
    private final Setting<Boolean> rainbow = add(new Setting<Boolean>("Радужное", false));
    private final Setting<Double> speed = add(new Setting<Double>("Скорость радуги", 4.0, 1, 20));

    private int ticks;
    private boolean warned;

    public SkyColor() {
        super("SkyColor", Category.WORLD,
                "Красит небо в свой цвет (или в плавно меняющуюся радугу). Видно только тебе, в Незере и Энде не работает.");
        INSTANCE = this;
    }

    public int rgb() {
        float h = rainbow.get()
                ? (float) ((System.currentTimeMillis() * speed.get() / 20000.0) % 1.0)
                : hue.get();
        return ColorSetting.hsv(h, 0.75f, (float) (brightness.get() / 100.0)) & 0xFFFFFF;
    }

    @Override
    protected void onEnable() {
        ticks = 0;
        warned = false;
        hooked = false;
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || hooked || warned) return;
        if (++ticks > 80) {
            warned = true;
            mc.player.displayClientMessage(Component.literal(
                    "§c[Rizhik] SkyColor: хук неба не сработал (или ты в Незере/Энде). Если в Верхнем мире тоже так, скажи мне."), false);
        }
    }
}
