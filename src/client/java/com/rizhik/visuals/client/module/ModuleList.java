package com.rizhik.visuals.client.module;

public class ModuleList {
    public static void init() {
        ModuleManager.register(new Module("Watermark", Category.HUD) {});
        ModuleManager.register(new Module("TargetHUD", Category.HUD) {});
        ModuleManager.register(new Module("ClickPearl", Category.PVP) {});
        ModuleManager.register(new Module("HitSounds", Category.PVP) {});
        ModuleManager.register(new Module("Fullbright", Category.VISUALS) {});
        ModuleManager.register(new Module("TimeChanger", Category.WORLD) {});
        ModuleManager.register(new Module("Trails", Category.WORLD) {});
        ModuleManager.register(new Module("StreamerMode", Category.MISC) {});
    }
}
