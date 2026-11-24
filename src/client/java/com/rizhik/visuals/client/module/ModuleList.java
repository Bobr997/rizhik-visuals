package com.rizhik.visuals.client.module;

public class ModuleList {
    public static void init() {
        ModuleManager.register(new Watermark());
        ModuleManager.register(new Module("TargetHUD", Category.HUD, "Карточка противника с ХП и бронёй") {});
        ModuleManager.register(new Module("ClickPearl", Category.PVP, "Бросок перла по одной кнопке") {});
        ModuleManager.register(new Module("HitSounds", Category.PVP, "Свои звуки попадания по цели") {});
        ModuleManager.register(new Module("Fullbright", Category.VISUALS, "Максимальная яркость в темноте") {});
        ModuleManager.register(new Module("TimeChanger", Category.WORLD, "Фиксирует время суток") {});
        ModuleManager.register(new Module("Trails", Category.WORLD, "Светящийся шлейф при беге") {});
        ModuleManager.register(new Module("StreamerMode", Category.MISC, "Скрывает личные данные на экране") {});
    }
}
