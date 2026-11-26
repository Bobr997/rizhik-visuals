package com.rizhik.visuals.client.module;

import com.rizhik.visuals.client.render.WorldFx;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class HitEffect extends Module {
    private static final int SEG = 36;
    private static final double LIFE = 480.0;

    private final ModeSetting mode = add(new ModeSetting("Режим", 0, "Искры", "Волна", "Звёзды", "Сердца"));
    private final ColorSetting color = add(new ColorSetting("Цвет", 0.78f));
    private final Setting<Boolean> rainbow = add(new Setting<Boolean>("Радужный", false));
    private final Setting<Double> amount = add(new Setting<Double>("Количество", 14.0, 4, 40));
    private final Setting<Boolean> onlyPlayers = add(new Setting<Boolean>("Только по игрокам", false));

    private final List<double[]> rings = new ArrayList<>();

    public HitEffect() {
        super("HitEffect", Category.VISUALS,
                "Эффект в месте каждого твоего удара: цветные искры, расходящаяся волна, звёзды или сердечки. Режим, цвет и количество настраиваются.");
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            Minecraft mc = Minecraft.getInstance();
            if (isEnabled() && player == mc.player && mc.level != null
                    && entity instanceof LivingEntity le
                    && (!onlyPlayers.get() || le instanceof Player)) {
                spawn(mc, le.getX(), le.getY() + le.getBbHeight() * 0.65, le.getZ());
            }
            return InteractionResult.PASS;
        });
    }

    @Override
    protected void onDisable() {
        rings.clear();
    }

    private static double rnd(ThreadLocalRandom r) {
        return (r.nextDouble() - 0.5) * 2.0;
    }

    private void spawn(Minecraft mc, double x, double y, double z) {
        int m = mode.get();
        int rgb = (rainbow.get() ? Fx.rainbow() : color.rgb()) & 0xFFFFFF;
        int n = (int) Math.round(amount.get());
        ThreadLocalRandom r = ThreadLocalRandom.current();

        if (m == 0) {
            for (int i = 0; i < n; i++) {
                Fx.dust(mc, x + rnd(r) * 0.4, y + rnd(r) * 0.4, z + rnd(r) * 0.4, rgb, 1.0f);
            }
            for (int i = 0; i < n / 3; i++) {
                mc.level.addParticle(ParticleTypes.CRIT, x, y, z, rnd(r) * 0.5, r.nextDouble() * 0.4, rnd(r) * 0.5);
            }
        } else if (m == 1) {
            rings.add(new double[]{x, y, z, System.currentTimeMillis(), rgb});
            for (int i = 0; i < n / 2; i++) {
                Fx.dust(mc, x + rnd(r) * 0.3, y + rnd(r) * 0.3, z + rnd(r) * 0.3, rgb, 0.9f);
            }
        } else if (m == 2) {
            for (int i = 0; i < n; i++) {
                mc.level.addParticle(ParticleTypes.END_ROD, x, y, z,
                        rnd(r) * 0.25, rnd(r) * 0.25, rnd(r) * 0.25);
            }
        } else {
            for (int i = 0; i < n / 3 + 1; i++) {
                mc.level.addParticle(ParticleTypes.HEART,
                        x + rnd(r) * 0.4, y + rnd(r) * 0.3, z + rnd(r) * 0.4, 0, 0.05, 0);
            }
        }
    }

    private void ring(WorldFx.Quads q, double x, double y, double z, double rad, double w, int rgb, double alpha) {
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
        rings.removeIf(e -> now - e[3] > LIFE);
        for (double[] e : rings) {
            double t = (now - e[3]) / LIFE;
            double rad = 0.25 + 1.4 * (1 - (1 - t) * (1 - t));
            ring(q, e[0], e[1], e[2], rad, 0.28, (int) e[4], Math.pow(1 - t, 1.2) * 0.9);
        }
    }
}
