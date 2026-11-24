package com.rizhik.visuals.client.gui;

import com.rizhik.visuals.client.module.Category;
import com.rizhik.visuals.client.module.ColorSetting;
import com.rizhik.visuals.client.module.ModeSetting;
import com.rizhik.visuals.client.module.Module;
import com.rizhik.visuals.client.module.ModuleManager;
import com.rizhik.visuals.client.module.Setting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClickGui extends Screen {
    private static final int PW = 440, PH = 260, SIDE = 100, PAD = 10, CARD_H = 38, HEAD = 34, SET_H = 18;
    private static final int BAR_W = 56;
    private static final int ORANGE = 0xFFFF7B1D;
    private static final Set<Module> EXPANDED = new HashSet<>();

    private record Card(Module module, int x, int y, int w, int h) {}

    private Category selected = Category.PVP;
    private double scroll = 0;

    public ClickGui() {
        super(Component.literal("Rizhik Visuals"));
    }

    private float scale() {
        return Math.min(1f, Math.min(width * 0.5f / PW, height * 0.6f / PH));
    }

    private double lx(double mx) { return (mx - width / 2.0) / scale() + width / 2.0; }
    private double ly(double my) { return (my - height / 2.0) / scale() + height / 2.0; }

    private int px() { return (width - PW) / 2; }
    private int py() { return (height - PH) / 2; }
    private int cardW() { return (PW - SIDE - PAD * 3) / 2; }
    private int cardX(int col) { return px() + SIDE + PAD + col * (cardW() + PAD); }
    private int sideY(int i) { return py() + 44 + i * 26; }

    private int descLines(Module m, int cw) {
        return font.split(Component.literal(m.description), cw - 16).size();
    }

    private int cardHeight(Module m, int cw) {
        if (!EXPANDED.contains(m)) return CARD_H;
        int h = 22 + descLines(m, cw) * 10 + 6;
        if (!m.settings.isEmpty()) h += m.settings.size() * SET_H + 4;
        return Math.max(CARD_H, h);
    }

    private int settingsTop(Card c) {
        return c.y() + 22 + descLines(c.module(), c.w()) * 10 + 6;
    }

    private List<Card> layout() {
        List<Card> cards = new ArrayList<>();
        int startY = py() + HEAD + PAD - (int) scroll;
        int[] colY = {startY, startY};
        List<Module> mods = ModuleManager.byCategory(selected);
        for (int i = 0; i < mods.size(); i++) {
            int col = i % 2;
            int h = cardHeight(mods.get(i), cardW());
            cards.add(new Card(mods.get(i), cardX(col), colY[col], cardW(), h));
            colY[col] += h + PAD;
        }
        return cards;
    }

    private double maxScroll() {
        int bottom = 0;
        for (Card c : layout()) bottom = Math.max(bottom, c.y() + c.h());
        int contentBottom = bottom + (int) scroll + PAD;
        int viewBottom = py() + PH - 4;
        return Math.max(0, contentBottom - viewBottom);
    }

    private void chevron(GuiGraphics g, int cx, int cy, boolean up, int color) {
        for (int i = 0; i < 4; i++) {
            int dy = up ? (3 - i) : i;
            g.fill(cx - 3 + i, cy + dy, cx - 2 + i, cy + dy + 1, color);
            g.fill(cx + 3 - i, cy + dy, cx + 4 - i, cy + dy + 1, color);
        }
    }

    private void drawSetting(GuiGraphics g, Setting<?> s, Card c, int ry) {
        g.drawString(font, s.name, c.x() + 8, ry + 5, 0xFFCCCCCC);
        int barX = c.x() + c.w() - 8 - BAR_W;
        Object v = s.get();
        if (s instanceof ModeSetting ms) {
            int bx = c.x() + c.w() - 8 - BAR_W;
            Render.roundedRect(g, bx, ry + 2, BAR_W, 14, 5, 0xFF2A2A31);
            String t = font.plainSubstrByWidth(ms.current(), BAR_W - 6);
            g.drawString(font, t, bx + (BAR_W - font.width(t)) / 2, ry + 5, ORANGE);
        } else if (s instanceof ColorSetting cs) {
            for (int k = 0; k < BAR_W; k++) {
                g.fill(barX + k, ry + 6, barX + k + 1, ry + 12, ColorSetting.hsv(k / (float) BAR_W, 0.85f, 1f));
            }
            int kx = barX + (int) (cs.get() * (BAR_W - 2));
            g.fill(kx, ry + 4, kx + 2, ry + 14, 0xFFFFFFFF);
        } else if (v instanceof Boolean b) {
            int tx = c.x() + c.w() - 8 - 24;
            Render.roundedRect(g, tx, ry + 3, 24, 12, 6, b ? ORANGE : 0xFF3A3A42);
            Render.roundedRect(g, b ? tx + 13 : tx + 1, ry + 4, 10, 10, 5, 0xFFFFFFFF);
        } else if (v instanceof Double d) {
            double f = (d - s.min) / (s.max - s.min);
            Render.roundedRect(g, barX, ry + 7, BAR_W, 4, 2, 0xFF3A3A42);
            Render.roundedRect(g, barX, ry + 7, Math.max(4, (int) (BAR_W * f)), 4, 2, ORANGE);
        }
    }

    @SuppressWarnings("unchecked")
    private void clickSetting(Setting<?> s, double mx, Card c) {
        if (s instanceof ModeSetting ms) {
            ms.next();
            return;
        }
        int barX = c.x() + c.w() - 8 - BAR_W;
        double f = Math.max(0, Math.min(1, (mx - barX) / BAR_W));
        Object v = s.get();
        if (s instanceof ColorSetting cs) {
            if (mx >= barX - 4) cs.set((float) f);
        } else if (v instanceof Boolean b) {
            ((Setting<Boolean>) s).set(!b);
        } else if (v instanceof Double) {
            if (mx >= barX - 4) ((Setting<Double>) s).set(s.min + (s.max - s.min) * f);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        g.fill(0, 0, width, height, 0x66000000);

        float s = scale();
        g.pose().pushMatrix();
        g.pose().translate(width / 2f, height / 2f);
        g.pose().scale(s, s);
        g.pose().translate(-width / 2f, -height / 2f);

        int x = px(), y = py();
        Render.roundedRect(g, x, y, PW, PH, 10, 0xF0111114);
        Render.roundedRect(g, x, y, SIDE, PH, 10, 0xF00C0C0F);

        g.drawString(font, "Rizhik", x + 14, y + 14, ORANGE);
        g.drawString(font, "Visuals", x + 14 + font.width("Rizhik "), y + 14, 0xFFFFFFFF);

        Category[] cats = Category.values();
        for (int i = 0; i < cats.length; i++) {
            int sy = sideY(i);
            boolean active = cats[i] == selected;
            if (active) Render.roundedRect(g, x + 8, sy, SIDE - 16, 20, 6, ORANGE);
            g.drawString(font, cats[i].title, x + 18, sy + 6, active ? 0xFFFFFFFF : 0xFF8A8A93);
        }

        g.drawString(font, selected.title, x + SIDE + PAD, y + 14, 0xFFFFFFFF);

        int viewTop = y + HEAD, viewBottom = y + PH - 4;
        for (Card c : layout()) {
            if (c.y() < viewTop || c.y() + c.h() > viewBottom) continue;
            Module m = c.module();
            boolean open = EXPANDED.contains(m);

            Render.roundedRect(g, c.x(), c.y(), c.w(), c.h(), 6, 0xFF1A1A1F);
            g.drawString(font, m.name, c.x() + 8, c.y() + 8, 0xFFFFFFFF);

            if (open) {
                int ly = c.y() + 22;
                for (var line : font.split(Component.literal(m.description), c.w() - 16)) {
                    g.drawString(font, line, c.x() + 8, ly, 0xFF8A8A93);
                    ly += 10;
                }
                int sy0 = settingsTop(c);
                for (int k = 0; k < m.settings.size(); k++) {
                    drawSetting(g, m.settings.get(k), c, sy0 + k * SET_H);
                }
            } else {
                g.drawString(font, font.plainSubstrByWidth(m.description, c.w() - 60),
                        c.x() + 8, c.y() + 22, 0xFF8A8A93);
            }

            int tx = c.x() + c.w() - 52, ty = c.y() + 13;
            Render.roundedRect(g, tx, ty, 24, 12, 6, m.isEnabled() ? ORANGE : 0xFF3A3A42);
            int kx = m.isEnabled() ? tx + 13 : tx + 1;
            Render.roundedRect(g, kx, ty + 1, 10, 10, 5, 0xFFFFFFFF);

            chevron(g, c.x() + c.w() - 14, c.y() + 17, open, 0xFF8A8A93);
        }

        g.pose().popMatrix();
        super.render(g, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            double mx = lx(event.x()), my = ly(event.y());
            int x = px(), y = py();

            Category[] cats = Category.values();
            for (int i = 0; i < cats.length; i++) {
                int sy = sideY(i);
                if (mx >= x + 8 && mx <= x + SIDE - 8 && my >= sy && my <= sy + 20) {
                    selected = cats[i];
                    scroll = 0;
                    return true;
                }
            }

            if (my >= y + HEAD && my <= y + PH - 4) {
                for (Card c : layout()) {
                    if (mx < c.x() || mx > c.x() + c.w() || my < c.y() || my > c.y() + c.h()) continue;
                    if (my <= c.y() + CARD_H) {
                        if (mx >= c.x() + c.w() - 28) {
                            if (!EXPANDED.remove(c.module())) EXPANDED.add(c.module());
                        } else {
                            c.module().toggle();
                        }
                    } else if (EXPANDED.contains(c.module())) {
                        int sy0 = settingsTop(c);
                        List<Setting<?>> list = c.module().settings;
                        for (int k = 0; k < list.size(); k++) {
                            int ry = sy0 + k * SET_H;
                            if (my >= ry && my <= ry + SET_H) clickSetting(list.get(k), mx, c);
                        }
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scroll = Math.max(0, Math.min(maxScroll(), scroll - scrollY * 14));
        return true;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float delta) {
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
