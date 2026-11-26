package com.rizhik.visuals.client.module;

public class ModuleList {
    public static void init() {
        ModuleManager.register(new Watermark());
        ModuleManager.register(new CustomHotbar());
        ModuleManager.register(new ArmorHud());
        ModuleManager.register(new TotemCounter());
        ModuleManager.register(new ShieldInfo());
        ModuleManager.register(new Keystrokes());
        ModuleManager.register(new Cooldowns());
        ModuleManager.register(new TargetHud());
        ModuleManager.register(new Markers());
        ModuleManager.register(new ClickPearl());
        ModuleManager.register(new AutoCart());
        ModuleManager.register(new AutoTotem());
        ModuleManager.register(new HitSounds());
        ModuleManager.register(new Fullbright());
        ModuleManager.register(new CustomHand());
        ModuleManager.register(new ChinaHat());
        ModuleManager.register(new KillEffect());
        ModuleManager.register(new HitEffect());
        ModuleManager.register(new Trails());
        ModuleManager.register(new JumpCircles());
        ModuleManager.register(new Optimization());
        ModuleManager.register(new SkyColor());
        ModuleManager.register(new Module("TimeChanger", Category.WORLD,
                "Фиксирует время суток (день, закат, ночь) ползунком, только у тебя на экране.") {});
        ModuleManager.register(new Module("StreamerMode", Category.MISC,
                "Скрывает личную информацию на экране при записи видео и стримах.") {});
    }
}
