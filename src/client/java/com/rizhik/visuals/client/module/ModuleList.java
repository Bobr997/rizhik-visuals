package com.rizhik.visuals.client.module;

public class ModuleList {
    public static void init() {
        ModuleManager.register(new Watermark());
        ModuleManager.register(new ArmorHud());
        ModuleManager.register(new TotemCounter());
        ModuleManager.register(new ShieldInfo());
        ModuleManager.register(new Keystrokes());
        ModuleManager.register(new Cooldowns());
        ModuleManager.register(new TargetHud());
        ModuleManager.register(new Markers());
        ModuleManager.register(new ClickPearl());
        ModuleManager.register(new HitSounds());
        ModuleManager.register(new Fullbright());
        ModuleManager.register(new Trails());
        ModuleManager.register(new JumpCircles());
        ModuleManager.register(new Module("TimeChanger", Category.WORLD,
                "Фиксирует время суток (день, закат, ночь) ползунком, только у тебя на экране.") {});
        ModuleManager.register(new Module("StreamerMode", Category.MISC,
                "Скрывает личную информацию на экране при записи видео и стримах.") {});
    }
}
