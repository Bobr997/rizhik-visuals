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
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClickGui extends Screen {
    private static final int SIDE = 88, PAD = 8, HEAD = 30, CARD_H = 36;
    private static final Set<Module> EXPANDED = new HashSet<>();
    private static final Map<Object, Float> OPEN = new IdentityHashMap<>();
    private static final Map<Object, Float> TOG = new IdentityHashMap<>();
    private static final Map<Object, Float> HOV = new IdentityHashMap<>();
    private static final Map<Module, List<FormattedCharSequence>> DESC = new IdentityHashMap<>();
    private static final Map<Module, Integer> DESC_W = new IdentityHashMap<>();

    private record Card(Module module, int x, int y, int w, int h) {}

    private Category selected = Category.PVP;
    private double scroll = 0, scrollAnim = 0;
    private float appear = 0f, pillY = -1f;
    private long last = System.nanoTime();
    private float dt = 0.016f;
    private Setting<?> dragSetting;
    private double dragBarX, dragBarW;

    public ClickGui() {
        super(Component.literal("Rizhik Visuals"));
    }

    private static float get(Map<Object, Float> map, Object key, float def) {
        Float v = map.get(key);
        return v == null ? def : v;
    }

    private static float step(Map<Object, Float> map, Object key, float target, float dt, float speed) {
        float cur = get(map, key, target);
        cur = Ui.approach(cur, target, dt, speed);
        if (Math.abs(cur - target) < 0.003f) cur = target;
        map.put(key, cur);
        return cur;
    }

    private float sc() {
        float e = 1f - (1f - appear) * (1f - appear) * (1f - appear);
        return 0.94f + 0.06f * e;
    }

    private double lx(double mx) { return (mx - width / 2.0) / sc() + width / 2.0; }
    private double ly(double my) { return (my - height / 2.0) / sc() + height / 2.0; }

    private int pw() { return Math.min(400, width - 12); }
    private int ph() { return Math.min(236, height - 12); }
    private int px() { return (width - pw()) / 2; }
    private int py() { return (height - ph()) / 2; }
    private int cardW() { return (pw() - SIDE - PAD * 3) / 2; }
    private int cardX(int col) { return px() + SIDE + PAD + col * (cardW() + PAD); }
    private int sideY(int i) { return py() + 38 + i * 24; }

    private List<FormattedCharSequence> lines(Module m, int cw) {
        int wdt = cw - 20;
        Integer cached = DESC_W.get(m);
        if (cached == null || cached != wdt) {
            DESC.put(m, font.split(Component.literal(m.description), wdt));
            DESC_W.put(m, wdt);
        }
        return DESC.get(m);
    }

    private static boolean isBar(Setting<?> s) {
        return s instanceof ColorSetting || s.get() instanceof Double;
    }

    private static int rowH(Setting<?> s) {
        return isBar(s) ? 30 : 20;
    }

    private int settingsHeight(Module m) {
        int h = 0;
        for (Setting<?> s : m.settings) h += rowH(s);
        return h;
    }

    private int cardHeight(Module m, int cw) {
        float o = get(OPEN, m, EXPANDED.contains(m) ? 1f : 0f);
        int exp = 20 + lines(m, cw).size() * 10 + 4 + settingsHeight(m) + 4;
        exp = Math.max(exp, CARD_H);
        return Math.round(CARD_H + (exp - CARD_H) * o);
    }

    private int settingsTop(Card c) {
        return c.y() + 20 + lines(c.module(), c.w()).size() * 10 + 4;
    }

    private List<Card> layout() {
        List<Card> cards = new ArrayList<>();
        int startY = py() + HEAD + PAD - (int) Math.round(scrollAnim);
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

    private double maxScroll(List<Card> cards) {
        int bottom = 0;
        for (Card c : cards) bottom = Math.max(bottom, c.y() + c.h());
        double contentBottom = bottom + Math.round(scrollAnim) + PAD;
        double viewBottom = py() + ph() - 6;
        return Math.max(0, contentBottom - viewBottom);
    }

    @SuppressWarnings("unchecked")
    private void setFromBar(Setting<?> s, double mx, double barX, double barW) {
        double f = Math.max(0, Math.min(1, (mx - barX) / barW));
        if (s instanceof ColorSetting cs) {
            cs.set((float) f);
        } else if (s.get() instanceof Double) {
            ((Setting<Double>) s).set(s.min + (s.max - s.min) * f);
        }
    }

    private void drawSetting(GuiGraphics g, Setting<?> s, Card c, int ry) {
        int lx = c.x() + 10, rw = c.w() - 20;
        Object v = s.get();
        if (s instanceof ModeSetting ms) {
            g.drawString(font, s.name, lx, ry + 6, Ui.SUBTEXT);
            int bw = 56, bx = c.x() + c.w() - 10 - bw;
            Ui.rrect(g, bx, ry + 2, bw, 15, 7, Ui.OFF);
            String t = font.plainSubstrByWidth(ms.current(), bw - 8);
            g.drawString(font, t, bx + (bw - font.width(t)) / 2, ry + 6, Ui.ACCENT);
        } else if (v instanceof Boolean b) {
            g.drawString(font, s.name, lx, ry + 6, Ui.SUBTEXT);
            float t = step(TOG, s, b ? 1f : 0f, dt, 16f);
            Ui.toggle(g, c.x() + c.w() - 10 - 26, ry + 3, 26, 14, t);
        } else if (s instanceof ColorSetting cs) {
            g.drawString(font, s.name, lx, ry + 4, Ui.SUBTEXT);
            int col = ColorSetting.hsv(cs.get(), 0.85f, 1f);
            Ui.circle(g, c.x() + c.w() - 14, ry + 8, 4, col);
            Ui.hueBar(g, lx, ry + 18, rw, 4);
            double kx = lx + cs.get() * rw;
            Ui.circle(g, kx, ry + 20, 5, 0xFFFFFFFF);
            Ui.circle(g, kx, ry + 20, 3, col);
        } else if (v instanceof Double d) {
            g.drawString(font, s.name, lx, ry + 4, Ui.SUBTEXT);
            String val = (s.max - s.min) >= 50 ? String.format("%.0f", d) : String.format("%.1f", d);
            g.drawString(font, val, lx + rw - font.width(val), ry + 4, Ui.MUTED);
            double f = Math.max(0, Math.min(1, (d - s.min) / (s.max - s.min)));
            Ui.rrect(g, lx, ry + 18, rw, 4, 2, Ui.OFF);
            Ui.rrect(g, lx, ry + 18, Math.max(4, rw * f), 4, 2, Ui.ACCENT);
            Ui.circle(g, lx + rw * f, ry + 20, 5, 0xFFFFFFFF);
        }
    }

    @SuppressWarnings("unchecked")
    private void clickSetting(Setting<?> s, double mx, Card c) {
        double barX = c.x() + 10, barW = c.w() - 20;
        if (s instanceof ModeSetting ms) {
            ms.next();
        } else if (isBar(s)) {
            dragSetting = s;
            dragBarX = barX;
            dragBarW = barW;
            setFromBar(s, mx, barX, barW);
        } else if (s.get() instanceof Boolean b) {
            ((Setting<Boolean>) s).set(!b);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        long now = System.nanoTime();
        dt = Math.min(0.05f, (now - last) / 1e9f);
        last = now;
        appear = Ui.approach(appear, 1f, dt, 12f);
        if (appear > 0.995f) appear = 1f;
        scrollAnim += (scroll - scrollAnim) * (1 - Math.exp(-dt * 16));
        if (Math.abs(scroll - scrollAnim) < 0.3) scrollAnim = scroll;

        for (Module m : ModuleManager.byCategory(selected)) {
            step(OPEN, m, EXPANDED.contains(m) ? 1f : 0f, dt, 14f);
        }
        List<Card> cards = layout();

        g.fill(0, 0, width, height, ((int) (0x88 * appear)) << 24);

        float s = sc();
        g.pose().pushMatrix();
        g.pose().translate(width / 2f, height / 2f);
        g.pose().scale(s, s);
        g.pose().translate(-width / 2f, -height / 2f);

        double mx = lx(mouseX), my = ly(mouseY);
        int x = px(), y = py(), w = pw(), h = ph();

        Ui.panel(g, x, y, w, h, 11, Ui.BG, Ui.BORDER);
        Ui.rrect(g, x + 5, y + 5, SIDE - 6, h - 10, 8, Ui.SIDEBAR);
        g.drawString(font, "Rizhik", x + 14, y + 14, Ui.ACCENT);
        g.drawString(font, "Visuals", x + 14 + font.width("Rizhik "), y + 14, Ui.TEXT);

        Category[] cats = Category.values();
        float tgt = sideY(selected.ordinal());
        pillY = pillY < 0 ? tgt : Ui.approach(pillY, tgt, dt, 18f);
        Ui.rrect(g, x + 9, pillY, SIDE - 14, 20, 7, Ui.alpha(Ui.ACCENT, 0.22));
        Ui.rrect(g, x + 9, pillY + 5, 2.2, 10, 1.1, Ui.ACCENT);
        for (int i = 0; i < cats.length; i++) {
            boolean active = cats[i] == selected;
            int sy = sideY(i);
            g.drawString(font, cats[i].title, x + 18, sy + 6, active ? Ui.TEXT : Ui.MUTED);
            String cnt = String.valueOf(ModuleManager.byCategory(cats[i]).size());
            g.drawString(font, cnt, x + SIDE - 14 - font.width(cnt), sy + 6, Ui.MUTED);
        }
        long on = ModuleManager.all().stream().filter(Module::isEnabled).count();
        Ui.circle(g, x + 16, y + h - 15, 2.5, Ui.ACCENT);
        g.drawString(font, on + " активно", x + 23, y + h - 19, Ui.MUTED);

        g.drawString(font, selected.title, x + SIDE + PAD, y + 13, Ui.TEXT);
        String right = ModuleManager.byCategory(selected).size() + " модулей";
        g.drawString(font, right, x + w - 12 - font.width(right), y + 13, Ui.MUTED);

        int viewTop = y + HEAD, viewBottom = y + h - 6;
        g.enableScissor(x + SIDE, viewTop, x + w - 4, viewBottom);
        for (Card c : cards) {
            if (c.y() + c.h() < viewTop || c.y() > viewBottom) continue;
            Module m = c.module();
            boolean hover = mx >= c.x() && mx <= c.x() + c.w() && my >= Math.max(c.y(), viewTop)
                    && my <= Math.min(c.y() + c.h(), viewBottom);
            float hv = step(HOV, m, hover ? 1f : 0f, dt, 16f);
            float tg = step(TOG, m, m.isEnabled() ? 1f : 0f, dt, 14f);
            float op = get(OPEN, m, 0f);

            int bg = Ui.mix(Ui.mix(Ui.CARD, Ui.CARD_HOVER, hv), Ui.ACCENT, 0.10 * tg);
            Ui.rrect(g, c.x(), c.y(), c.w(), c.h(), 8, bg);

            g.enableScissor(c.x(), Math.max(c.y(), viewTop), c.x() + c.w(), Math.min(c.y() + c.h(), viewBottom));
            if (tg > 0.02f) Ui.rrect(g, c.x() + 4, c.y() + 8, 2.2, 20, 1.1, Ui.alpha(Ui.ACCENT, tg));
            g.drawString(font, m.name, c.x() + 12, c.y() + 8, Ui.TEXT);

            if (op > 0.3f) {
                int ly = c.y() + 20;
                for (var line : lines(m, c.w())) {
                    g.drawString(font, line, c.x() + 10, ly, Ui.MUTED);
                    ly += 10;
                }
            } else {
                g.drawString(font, font.plainSubstrByWidth(m.description, c.w() - 52),
                        c.x() + 12, c.y() + 21, Ui.MUTED);
            }
            if (op > 0.02f) {
                int ry = settingsTop(c);
                for (Setting<?> st : m.settings) {
                    drawSetting(g, st, c, ry);
                    ry += rowH(st);
                }
            }

            Ui.toggle(g, c.x() + c.w() - 44, c.y() + 8, 26, 14, tg);
            Ui.chevron(g, c.x() + c.w() - 11, c.y() + 15, 3.2, op, Ui.MUTED);
            g.disableScissor();
        }
        g.disableScissor();

        double max = maxScroll(cards);
        if (max > 0) {
            double track = viewBottom - viewTop - 4;
            double thumb = Math.max(18, track * track / (track + max));
            double ty = viewTop + 2 + (scrollAnim / max) * (track - thumb);
            Ui.rrect(g, x + w - 7, ty, 2.2, thumb, 1.1, Ui.alpha(Ui.ACCENT, 0.55));
        }

        g.pose().popMatrix();
        super.render(g, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = lx(event.x()), my = ly(event.y());
        int x = px(), y = py(), h = ph();

        if (event.button() == 0) {
            Category[] cats = Category.values();
            for (int i = 0; i < cats.length; i++) {
                int sy = sideY(i);
                if (mx >= x + 8 && mx <= x + SIDE - 2 && my >= sy && my <= sy + 20) {
                    selected = cats[i];
                    scroll = 0;
                    scrollAnim = 0;
                    return true;
                }
            }
        }

        if (mx >= x + SIDE && my >= y + HEAD && my <= y + h - 6) {
            for (Card c : layout()) {
                if (mx < c.x() || mx > c.x() + c.w() || my < c.y() || my > c.y() + c.h()) continue;
                Module m = c.module();
                if (my <= c.y() + CARD_H) {
                    boolean chevron = mx >= c.x() + c.w() - 20;
                    if (event.button() == 1 || (event.button() == 0 && chevron)) {
                        if (!EXPANDED.remove(m)) EXPANDED.add(m);
                    } else if (event.button() == 0) {
                        m.toggle();
                    }
                } else if (event.button() == 0 && EXPANDED.contains(m)) {
                    int ry = settingsTop(c);
                    for (Setting<?> st : m.settings) {
                        int rh = rowH(st);
                        if (my >= ry && my <= ry + rh) clickSetting(st, mx, c);
                        ry += rh;
                    }
                }
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (dragSetting != null) {
            setFromBar(dragSetting, lx(event.x()), dragBarX, dragBarW);
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        dragSetting = null;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scroll = Math.max(0, Math.min(maxScroll(layout()), scroll - scrollY * 22));
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
