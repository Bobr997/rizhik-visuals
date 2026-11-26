package com.rizhik.visuals.client.module;

import com.rizhik.visuals.client.render.WorldFx;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class KillEffect extends Module {
    private static final int SEG = 40;

    private final ModeSetting mode = add(new ModeSetting("Режим", 0, "Столб", "Кольца", "Взрыв", "Сгорание"));
    private final ColorSetting color = add(new ColorSetting("Цвет", 0.55f));
    private final Setting<Boolean> rainbow = add(new Setting<Boolean>("Радужный", false));
    private final Setting<Double> volume = add(new Setting<Double>("Громкость", 60.0, 0, 100));
    private final Setting<Double> time = add(new Setting<Double>("Время", 1.5, 0.6, 3.0));
    private final Setting<Boolean> particles = add(new Setting<Boolean>("Частицы", true));
    private final Setting<Boolean> sound = add(new Setting<Boolean>("Звук эффекта", true));
    private final Setting<Boolean> onlyPlayers = add(new Setting<Boolean>("Только по игрокам", false));

    private final List<double[]> effects = new ArrayList<>();
    private LivingEntity target;
    private long lastHit;
    private double lx, ly, lz;

    public KillEffect() {
        super("KillEffect", Category.VISUALS,
                "Эффект на месте, где ты убил игрока или моба: столб света, кольца, взрыв или сгорание. Режим, цвет, громкость, частицы, звук и фильтр «только игроки» настраиваются.");
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            if (isEnabled() && player == Minecraft.getInstance().player
                    && entity instanceof LivingEntity le) {
                target = le;
                lastHit = System.currentTimeMillis();
                lx = le.getX();
                ly = le.getY();
                lz = le.getZ();
            }
            return InteractionResult.PASS;
        });
    }

    @Override
    protected void onDisable() {
        effects.clear();
        target = null;
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.level == null || mc.player == null) {
            target = null;
            effects.clear();
            return;
        }
        if (target == null) return;
        if (System.currentTimeMillis() - lastHit > 6000) {
            target = null;
            return;
        }
        if (target.isDeadOrDying() || target.getHealth() <= 0f) {
            if (!onlyPlayers.get() || target instanceof Player) {
                trigger(mc, lx, ly, lz);
            }
            target = null;
            return;
        }
        if (target.isRemoved()) {
            target = null;
            return;
        }
        lx = target.getX();
        ly = target.getY();
        lz = target.getZ();
    }

    private void trigger(Minecraft mc, double x, double y, double z) {
        int m = mode.get();
        int rgb = (rainbow.get() ? Fx.rainbow() : color.rgb()) & 0xFFFFFF;
        effects.add(new double[]{x, y, z, System.currentTimeMillis(), m, rgb});

        ThreadLocalRandom r = ThreadLocalRandom.current();
        if (m == 3) {
            for (int i = 0; i < 45; i++) {
                mc.level.addParticle(ParticleTypes.FLAME,
                        x + (r.nextDouble() - 0.5) * 0.8, y + r.nextDouble() * 1.8, z + (r.nextDouble() - 0.5) * 0.8,
                        0, 0.04 + r.nextDouble() * 0.06, 0);
            }
            for (int i = 0; i < 12; i++) {
                mc.level.addParticle(ParticleTypes.LARGE_SMOKE,
                        x + (r.nextDouble() - 0.5) * 0.6, y + 0.5 + r.nextDouble() * 1.5, z + (r.nextDouble() - 0.5) * 0.6,
                        0, 0.03, 0);
            }
        } else if (particles.get()) {
            for (int i = 0; i < 40; i++) {
                Fx.dust(mc, x + (r.nextDouble() - 0.5) * 1.2, y + r.nextDouble() * 2.0,
                        z + (r.nextDouble() - 0.5) * 1.2, rgb, 1.3f);
            }
            if (m == 2) {
                for (int i = 0; i < 40; i++) {
                    double a = r.nextDouble() * Math.PI * 2;
                    mc.level.addParticle(ParticleTypes.END_ROD, x, y + 1.0, z,
                            Math.cos(a) * 0.25, r.nextDouble() * 0.3, Math.sin(a) * 0.25);
                }
            }
        }

        if (sound.get()) {
            SoundEvent s = switch (m) {
                case 0 -> SoundEvents.BEACON_ACTIVATE;
                case 1 -> SoundEvents.AMETHYST_BLOCK_CHIME;
                case 2 -> SoundEvents.LIGHTNING_BOLT_THUNDER;
                default -> SoundEvents.FIRECHARGE_USE;
            };
            mc.getSoundManager().play(SimpleSoundInstance.forUI(s, 1.0f, (float) (volume.get() / 100.0)));
        }
    }

    private void ring(WorldFx.Quads q, double x, double y, double z,
                      double rad, double w, int rgb, double alpha) {
        double ri = Math.max(0.03, rad - w);
        int outer = WorldFx.argb(rgb, alpha);
        int inner = WorldFx.argb(rgb, alpha * 0.25);
        for (int i = 0; i < SEG; i++) {
            double a0 = Math.PI * 2 * i / SEG;
            double a1 = Math.PI * 2 * (i + 1) / SEG;
            q.vertex(x + Math.cos(a0) * rad, y, z + Math.sin(a0) * rad, outer);
            q.vertex(x + Math.cos(a1) * rad, y, z + Math.sin(a1) * rad, outer);
            q.vertex(x + Math.cos(a1) * ri, y, z + Math.sin(a1) * ri, inner);
            q.vertex(x + Math.cos(a0) * ri, y, z + Math.sin(a0) * ri, inner);
        }
    }

    @Override
    public void renderWorld(WorldFx.Quads q) {
        long now = System.currentTimeMillis();
        double lifeMs = time.get() * 1000.0;
        effects.removeIf(e -> now - e[3] > lifeMs);

        for (double[] e : effects) {
            double t = (now - e[3]) / lifeMs;
            int m = (int) e[4];
            int rgb = (int) e[5];
            double x = e[0], y = e[1], z = e[2];

            if (m == 0) {
                double r = 0.7 * (1 - t * 0.6);
                double h = 0.5 + 5.5 * Math.min(1.0, t * 3.0);
                double fade = Math.pow(1 - t, 0.8) * 0.9;
                int low = WorldFx.argb(rgb, fade);
                int top = WorldFx.argb(rgb, fade * 0.1);
                for (int i = 0; i < SEG; i++) {
                    double a0 = Math.PI * 2 * i / SEG;
                    double a1 = Math.PI * 2 * (i + 1) / SEG;
                    double x0 = x + Math.cos(a0) * r, z0 = z + Math.sin(a0) * r;
                    double x1 = x + Math.cos(a1) * r, z1 = z + Math.sin(a1) * r;
                    q.vertex(x0, y, z0, low);
                    q.vertex(x1, y, z1, low);
                    q.vertex(x1, y + h, z1, top);
                    q.vertex(x0, y + h, z0, top);
                }
                ring(q, x, y + 0.03, z, 1.0 + t * 0.8, 0.3, rgb, fade);
            } else if (m == 1) {
                for (int k = 0; k < 3; k++) {
                    double lt = (t - k * 0.18) / 0.55;
                    if (lt < 0 || lt > 1) continue;
                    ring(q, x, y + 0.1 + lt * 2.2, z, 0.5 + lt * 1.1, 0.22, rgb, Math.pow(1 - lt, 0.7) * 0.9);
                }
            } else if (m == 2) {
                double tt = Math.min(1.0, t * 2.0);
                double rad = 0.3 + 4.0 * (1 - (1 - tt) * (1 - tt));
                ring(q, x, y + 0.05, z, rad, 0.5, rgb, Math.pow(1 - t, 1.2));
            }
        }
    }
}
