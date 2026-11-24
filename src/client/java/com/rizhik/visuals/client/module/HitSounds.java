package com.rizhik.visuals.client.module;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;

public class HitSounds extends Module {
    private final ModeSetting sound = add(new ModeSetting("Звук", 0, "Опыт", "Крит", "Звон", "Стрела"));
    private final Setting<Double> volume = add(new Setting<Double>("Громкость", 100.0, 0, 100));
    private final Setting<Double> pitch = add(new Setting<Double>("Тон", 1.0, 0.5, 2.0));

    public HitSounds() {
        super("HitSounds", Category.PVP,
                "Проигрывает выбранный звук, когда ты бьёшь игрока или моба. Звук, громкость и тон настраиваются.");
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            if (isEnabled() && entity instanceof LivingEntity
                    && player == Minecraft.getInstance().player) {
                play();
            }
            return InteractionResult.PASS;
        });
    }

    private SoundEvent selected() {
        return switch (sound.get()) {
            case 1 -> SoundEvents.PLAYER_ATTACK_CRIT;
            case 2 -> SoundEvents.AMETHYST_BLOCK_CHIME;
            case 3 -> SoundEvents.ARROW_HIT_PLAYER;
            default -> SoundEvents.EXPERIENCE_ORB_PICKUP;
        };
    }

    private void play() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(
                selected(), pitch.get().floatValue(), (float) (volume.get() / 100.0)));
    }
}
