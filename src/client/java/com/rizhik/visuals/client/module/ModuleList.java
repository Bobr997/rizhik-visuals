package com.rizhik.visuals.client.module;

public class ModuleList {
    public static void init() {
        ModuleManager.register(new Watermark());
        ModuleManager.register(new Cooldowns());
        ModuleManager.register(new TargetHud());
        ModuleManager.register(new Markers());
        ModuleManager.register(new Module("ClickPearl", Category.PVP,
                "Бросает эндер-перл по одной кнопке, даже если в руке меч, и возвращает прошлый слот.") {});
        ModuleManager.register(new Module("HitSounds", Category.PVP,
                "Заменяет звук удара на выбранный звук попадания. Громкость настраивается.") {});
        ModuleManager.register(new Fullbright());
        ModuleManager.register(new Module("TimeChanger", Category.WORLD,
                "Фиксирует время суток (день, закат, ночь) ползунком, только у тебя на экране.") {});
        ModuleManager.register(new Module("Trails", Category.WORLD,
                "Светящийся шлейф за игроком при беге. Цвет и длина будут настраиваться.") {});
        ModuleManager.register(new Module("StreamerMode", Category.MISC,
                "Скрывает личную информацию на экране при записи видео и стримах.") {});
    }
}
