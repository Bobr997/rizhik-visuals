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

    public static void rrect(GuiGraphics g, double x, double y, double w, double h, double r, int argb) {
        if ((argb >>> 24) == 0 || w <= 0 || h <= 0) return;
        double k = scaleOf(g);
        int x0 = (int) Math.round(x * k), y0 = (int) Math.round(y * k);
        int x1 = (int) Math.round((x + w) * k), y1 = (int) Math.round((y + h) * k);
        int pw = x1 - x0, ph = y1 - y0;
        if (pw <= 0 || ph <= 0) return;
        int pr = (int) Math.round(Math.max(0, Math.min(r * k, Math.min(pw, ph) / 2.0)));

        var pose = g.pose();
        pose.pushMatrix();
        pose.scale((float) (1.0 / k), (float) (1.0 / k));
        if (pr <= 0) {
            g.fill(x0, y0, x1, y1, argb);
        } else {
            if (ph > 2 * pr) g.fill(x0, y0 + pr, x1, y1 - pr, argb);
            int[] full = new int[pr];
            int[] q = new int[pr];
            for (int i = 0; i < pr; i++) {
                double dy = pr - (i + 0.5);
                double dx = Math.sqrt(Math.max(0, (double) pr * pr - dy * dy));
                double edge = pr - dx;
                int f = (int) Math.ceil(edge - 1e-6);
                int qq = (int) Math.round((f - edge) * 4);
                if (qq >= 4) {
                    f -= 1;
                    qq = 0;
                }
                full[i] = Math.max(0, f);
                q[i] = qq;
            }
            int i = 0;
            while (i < pr) {
                int j = i;
                while (j + 1 < pr && full[j + 1] == full[i] && q[j + 1] == q[i]) j++;
                span(g, x0, x1, y0 + i, y0 + j + 1, full[i], q[i], argb);
                span(g, x0, x1, y1 - 1 - j, y1 - i, full[i], q[i], argb);
                i = j + 1;
            }
        }
        pose.popMatrix();
    }

    private static void span(GuiGraphics g, int x0, int x1, int ya, int yb, int f, int q, int argb) {
        if (x1 - f > x0 + f) g.fill(x0 + f, ya, x1 - f, yb, argb);
        if (q > 0 && f > 0) {
            int c = alpha(argb, q / 4.0);
            g.fill(x0 + f - 1, ya, x0 + f, yb, c);
            g.fill(x1 - f, ya, x1 - f + 1, yb, c);
        }
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

    public static void glow(GuiGraphics g, double x, double y, double w, double h, double r,
                            int color, int layers, double step) {
        for (int i = layers; i >= 1; i--) {
            rrect(g, x - i * step, y - i * step, w + 2 * i * step, h + 2 * i * step,
                    r + i * step, alpha(color, 0.07));
        }
    }

    public static void toggle(GuiGraphics g, double x, double y, double w, double h, float t) {
        rrect(g, x, y, w, h, h / 2, mix(OFF, ACCENT, t));
        double d = h - 4;
        double kx = x + 2 + (w - 4 - d) * t;
        rrect(g, kx, y + 2, d, d, d / 2, 0xFFFFFFFF);
    }

    public static void hueBar(GuiGraphics g, double x, double y, double w, double h) {
        double k = scaleOf(g);
        int x0 = (int) Math.round(x * k), y0 = (int) Math.round(y * k);
        int x1 = (int) Math.round((x + w) * k), y1 = (int) Math.round((y + h) * k);
        int pw = x1 - x0, ph = y1 - y0;
        if (pw <= 0 || ph <= 0) return;
        int pr = ph / 2;
        var pose = g.pose();
        pose.pushMatrix();
        pose.scale((float) (1.0 / k), (float) (1.0 / k));
        for (int i = 0; i < pw; i++) {
            int inset = 0;
            double d = -1;
            if (i < pr) d = pr - i - 0.5;
            else if (i >= pw - pr) d = i - (pw - pr) + 0.5;
            if (d >= 0) inset = (int) Math.round(pr - Math.sqrt(Math.max(0, (double) pr * pr - d * d)));
            g.fill(x0 + i, y0 + inset, x0 + i + 1, y1 - inset, ColorSetting.hsv(i / (float) pw, 0.8f, 1f));
        }
        pose.popMatrix();
    }

    /** Галочка-стрелка: open=0 смотрит вниз, open=1 вверх. */
    public static void chevron(GuiGraphics g, double cx, double cy, double half, float open, int color) {
        double hy = half * 0.55 * (1 - 2 * open);
        segment(g, cx - half, cy - hy, cx, cy + hy, color);
        segment(g, cx, cy + hy, cx + half, cy - hy, color);
    }

    private static void segment(GuiGraphics g, double ax, double ay, double bx, double by, int color) {
        double k = scaleOf(g);
        double X0 = ax * k, Y0 = ay * k, X1 = bx * k, Y1 = by * k;
        int n = (int) Math.ceil(Math.max(Math.abs(X1 - X0), Math.abs(Y1 - Y0))) + 1;
        int th = Math.max(2, (int) Math.round(k * 0.75));
        var pose = g.pose();
        pose.pushMatrix();
        pose.scale((float) (1.0 / k), (float) (1.0 / k));
        for (int i = 0; i <= n; i++) {
            double t = i / (double) n;
            int px = (int) Math.round(X0 + (X1 - X0) * t);
            int py = (int) Math.round(Y0 + (Y1 - Y0) * t);
            g.fill(px, py, px + th, py + th, color);
        }
        pose.popMatrix();
    }
}
