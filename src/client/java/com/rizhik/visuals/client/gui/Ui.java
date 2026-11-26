package com.rizhik.visuals.client.gui;

import com.rizhik.visuals.client.module.ColorSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class Ui {
    public static final int BG = 0xFF0C0914;
    public static final int SIDEBAR = 0xFF110C1D;
    public static final int CARD = 0xFF181228;
    public static final int CARD_HOVER = 0xFF211838;
    public static final int BORDER = 0xFF3B2B6E;
    public static final int ACCENT = 0xFF9D5CFF;
    public static final int TEXT = 0xFFEFE9FF;
    public static final int SUBTEXT = 0xFFCFC6EA;
    public static final int MUTED = 0xFF8C82AB;
    public static final int OFF = 0xFF2B2343;

    private Ui() {}

    public static double gs() {
        return Minecraft.getInstance().getWindow().getGuiScale();
    }

    /** n физических пикселей экрана в единицах интерфейса */
    public static double px(double n) {
        return n / gs();
    }

    public static int alpha(int argb, double f) {
        int a = (int) Math.round((argb >>> 24) * Math.max(0, Math.min(1, f)));
        return (a << 24) | (argb & 0xFFFFFF);
    }

    public static int mix(int a, int b, double t) {
        t = Math.max(0, Math.min(1, t));
        int ar = (int) Math.round(((a >>> 24) & 255) * (1 - t) + ((b >>> 24) & 255) * t);
        int rr = (int) Math.round(((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
        int gg = (int) Math.round(((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
        int bb = (int) Math.round((a & 255) * (1 - t) + (b & 255) * t);
        return (ar << 24) | (rr << 16) | (gg << 8) | bb;
    }

    public static float approach(float cur, float target, float dt, float speed) {
        float f = 1f - (float) Math.exp(-dt * speed);
        return cur + (target - cur) * f;
    }

    private static double scaleOf(GuiGraphics g) {
        float s = g.pose().m00();
        if (s <= 0f) s = 1f;
        return gs() * s;
    }

    /** Скруглённый прямоугольник в физических пикселях: угол из нескольких ступенек + мягкий край. */
    public static void rrect(GuiGraphics g, double x, double y, double w, double h, double r, int argb) {
        if ((argb >>> 24) == 0 || w <= 0 || h <= 0) return;
        double k = scaleOf(g);
        int x0 = (int) Math.round(x * k), y0 = (int) Math.round(y * k);
        int x1 = (int) Math.round((x + w) * k), y1 = (int) Math.round((y + h) * k);
        int pw = x1 - x0, ph = y1 - y0;
        if (pw <= 0 || ph <= 0) return;
        int pr = (int) Math.min(Math.round(Math.max(0, r * k)), Math.min(pw, ph) / 2);

        var pose = g.pose();
        pose.pushMatrix();
        pose.scale((float) (1.0 / k), (float) (1.0 / k));
        if (pr <= 1) {
            g.fill(x0, y0, x1, y1, argb);
        } else {
            int bands = Math.max(2, Math.min(5, pr / 3));
            boolean soft = (argb >>> 24) == 255;
            int edge = alpha(argb, 0.5);
            if (ph > 2 * pr) g.fill(x0, y0 + pr, x1, y1 - pr, argb);
            for (int b = 0; b < bands; b++) {
                int ra = pr * b / bands, rb = pr * (b + 1) / bands;
                if (rb <= ra) continue;
                double dy = pr - (ra + rb) / 2.0;
                int inset = (int) Math.round(pr - Math.sqrt(Math.max(0, (double) pr * pr - dy * dy)));
                inset = Math.max(0, Math.min(inset, pr));
                if (soft && inset > 0) {
                    g.fill(x0 + inset - 1, y0 + ra, x1 - inset + 1, y0 + rb, edge);
                    g.fill(x0 + inset - 1, y1 - rb, x1 - inset + 1, y1 - ra, edge);
                }
                g.fill(x0 + inset, y0 + ra, x1 - inset, y0 + rb, argb);
                g.fill(x0 + inset, y1 - rb, x1 - inset, y1 - ra, argb);
            }
        }
        pose.popMatrix();
    }

    public static void circle(GuiGraphics g, double cx, double cy, double r, int argb) {
        rrect(g, cx - r, cy - r, r * 2, r * 2, r, argb);
    }

    /** Панель с тонкой рамкой (обе краски непрозрачные). */
    public static void panel(GuiGraphics g, double x, double y, double w, double h, double r,
                             int fill, int border) {
        double b = px(1.5);
        rrect(g, x, y, w, h, r, border);
        rrect(g, x + b, y + b, w - 2 * b, h - 2 * b, Math.max(0, r - b), fill);
    }

    public static void toggle(GuiGraphics g, double x, double y, double w, double h, float t) {
        rrect(g, x, y, w, h, h / 2, mix(OFF, ACCENT, t));
        double d = h - 4;
        double kx = x + 2 + (w - 4 - d) * t;
        rrect(g, kx, y + 2, d, d, d / 2, 0xFFFFFFFF);
    }

    public static void hueBar(GuiGraphics g, double x, double y, double w, double h) {
        double k = scaleOf(g);
        int y0 = (int) Math.round(y * k), y1 = (int) Math.round((y + h) * k);
        int n = 28;
        var pose = g.pose();
        pose.pushMatrix();
        pose.scale((float) (1.0 / k), (float) (1.0 / k));
        for (int i = 0; i < n; i++) {
            int xa = (int) Math.round((x + w * i / n) * k);
            int xb = (int) Math.round((x + w * (i + 1) / n) * k);
            g.fill(xa, y0, xb, y1, ColorSetting.hsv(i / (float) (n - 1), 0.8f, 1f));
        }
        pose.popMatrix();
    }

    /** Стрелка из двух повёрнутых палочек, плавно переворачивается: open 0 = вниз, 1 = вверх. */
    public static void chevron(GuiGraphics g, double cx, double cy, double half, float open, int color) {
        double k = scaleOf(g);
        float ang = (float) ((0.5 - open) * (Math.PI / 2));
        int len = (int) Math.round(half * 1.5 * k);
        int th = Math.max(2, (int) Math.round(k * 0.9));
        var pose = g.pose();
        for (int side = -1; side <= 1; side += 2) {
            pose.pushMatrix();
            pose.translate((float) (cx + side * half * 0.5), (float) cy);
            pose.scale((float) (1.0 / k), (float) (1.0 / k));
            pose.rotate(-side * ang);
            g.fill(-len / 2, -th / 2, len - len / 2, th - th / 2, color);
            pose.popMatrix();
        }
    }
}
