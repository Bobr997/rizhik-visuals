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
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClickGui extends Screen {
    private static final int SIDE = 112, PAD = 10, HEAD = 36, CARD_H = 42, SET_H = 22, BAR_W = 58;
    private static final Set<Module> EXPANDED = new HashSet<>();
    private static final Map<Object, Float> OPEN = new IdentityHashMap<>();
    private static final Map<Object, Float> TOG = new IdentityHashMap<>();
    private static final Map<Object, Float> HOV = new IdentityHashMap<>();

    private record Card(Module module, int x, int y, int w, int h) {}

    private Category selected = Category.PVP;
    private double scroll = 0, scrollAnim = 0;
    private float appear = 0f, pillY = -1f;
    private long last = System.nanoTime();
    private float dt = 0.016f;
    private Setting<?> dragSetting;
    private double dragBarX;

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

    private int pw() { return Math.min(480, width - 16); }
    private int ph() { return Math.min(290, height - 16); }
    private int px() { return (width - pw()) / 2; }
    private int py() { return (height - ph()) / 2; }
    private int cardW() { return (pw() - SIDE - PAD * 3) / 2; }
    private int cardX(int col) { return px() + SIDE + PAD + col * (cardW() + PAD); }
    private int sideY(int i) { return py() + 52 + i * 28; }

    private int descLines(Module m, int cw) {
        return font.split(Component.literal(m.description), cw - 20).size();
    }

    private int cardHeight(Module m, int cw) {
        float o = get(OPEN, m, EXPANDED.contains(m) ? 1f : 0f);
        int expanded = 24 + descLines(m, cw) * 10 + 6;
        if (!m.settings.isEmpty()) expanded += m.settings.size() * SET_H + 6;
        expanded = Math.max(expanded, CARD_H);
        return Math.round(CARD_H + (expanded - CARD_H) * o);
    }

    private int settingsTop(Card c) {
        return c.y() + 24 + descLines(c.module(), c.w()) * 10 + 6;
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

    private double maxScroll() {
        int bottom = 0;
        for (Card c : layout()) bottom = Math.max(bottom, c.y() + c.h());
        int contentBottom = bottom + (int) Math.round(scrollAnim) + PAD;
        int viewBottom = py() + ph() - 8;
        return Math.max(0, contentBottom - viewBottom);
    }

    private boolean isBar(Setting<?> s) {
        return s instanceof ColorSetting || s.get() instanceof Double;
    }

    @SuppressWarnings("unchecked")
    private void setFromBar(Setting<?> s, double mx, double barX) {
        double f = Math.max(0, Math.min(1, (mx - barX) / BAR_W));
        if (s instanceof ColorSetting cs) {
            cs.set((float) f);
        } else if (s.get() instanceof Double) {
            ((Setting<Double>) s).set(s.min + (s.max - s.min) * f);
        }
    }

    private void drawSetting(GuiGraphics g, Setting<?> s, Card c, int ry) {
        int nameX = c.x() + 10;
        g.drawString(font, s.name, nameX, ry + 7, Ui.SUBTEXT);
        int barX = c.x() + c.w() - 10 - BAR_W;
        Object v = s.get();

        if (s instanceof ModeSetting ms) {
            Ui.rrect(g, barX, ry + 3, BAR_W, 16, 8, Ui.OFF);
            String t = font.plainSubstrByWidth(ms.current(), BAR_W - 8);
            g.drawString(font, t, barX + (BAR_W - font.width(t)) / 2, ry + 7, Ui.ACCENT);
        } else if (s instanceof ColorSetting cs) {
            Ui.hueBar(g, barX, ry + 8, BAR_W, 5);
            double kx = barX + cs.get() * BAR_W;
            Ui.circle(g, kx, ry + 10.5, 5.2, 0xFFFFFFFF);
            Ui.circle(g, kx, ry + 10.5, 3.2, ColorSetting.hsv(cs.get(), 0.85f, 1f));
        } else if (v instanceof Boolean b) {
            float t = step(TOG, s, b ? 1f : 0f, dt, 16f);
            Ui.toggle(g, c.x() + c.w() - 10 - 26, ry + 4, 26, 14, t);
        } else if (v instanceof Double d) {
            double f = Math.max(0, Math.min(1, (d - s.min) / (s.max - s.min)));
            Ui.rrect(g, barX, ry + 9, BAR_W, 4, 2, Ui.OFF);
            Ui.rrect(g, barX, ry + 9, Math.max(4, BAR_W * f), 4, 2, Ui.ACCENT);
            Ui.circle(g, barX + BAR_W * f, ry + 11, 5, 0xFFFFFFFF);
            String val = (s.max - s.min) >= 50 ? String.format("%.0f", d) : String.format("%.1f", d);
            int avail = barX - nameX - 8;
            if (font.width(s.name) + 6 + font.width(val) <= avail) {
                g.drawString(font, val, barX - 8 - font.width(val), ry + 7, Ui.MUTED);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void clickSetting(Setting<?> s, double mx, Card c) {
        double barX = c.x() + c.w() - 10 - BAR_W;
        if (s instanceof ModeSetting ms) {
            ms.next();
        } else if (isBar(s)) {
            if (mx >= barX - 6) {
                dragSetting = s;
                dragBarX = barX;
                setFromBar(s, mx, barX);
            }
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

        g.fill(0, 0, width, height, ((int) (0x99 * appear)) << 24);

        float s = sc();
        g.pose().pushMatrix();
        g.pose().translate(width / 2f, height / 2f);
        g.pose().scale(s, s);
        g.pose().translate(-width / 2f, -height / 2f);

        double mx = lx(mouseX), my = ly(mouseY);
        int x = px(), y = py(), w = pw(), h = ph();

        Ui.glow(g, x, y, w, h, 14, Ui.ACCENT, 3, 2.2);
        Ui.panel(g, x, y, w, h, 14, Ui.BG, Ui.BORDER);

        // сайдбар
        Ui.rrect(g, x + 6, y + 6, SIDE - 8, h - 12, 10, Ui.SIDEBAR);
        g.drawString(font, "Rizhik", x + 18, y + 18, Ui.ACCENT);
        g.drawString(font, "Visuals", x + 18 + font.width("Rizhik "), y + 18, Ui.TEXT);

        Category[] cats = Category.values();
        float tgt = sideY(selected.ordinal());
        pillY = pillY < 0 ? tgt : Ui.approach(pillY, tgt, dt, 18f);
        Ui.rrect(g, x + 12, pillY, SIDE - 20, 22, 8, Ui.alpha(Ui.ACCENT, 0.22));
        Ui.rrect(g, x + 12, pillY + 5, 2.4, 12, 1.2, Ui.ACCENT);
        for (int i = 0; i < cats.length; i++) {
            boolean active = cats[i] == selected;
            int sy = sideY(i);
            g.drawString(font, cats[i].title, x + 22, sy + 7, active ? Ui.TEXT : Ui.MUTED);
            String cnt = String.valueOf(ModuleManager.byCategory(cats[i]).size());
            g.drawString(font, cnt, x + SIDE - 20 - font.width(cnt), sy + 7, Ui.MUTED);
        }
        long on = ModuleManager.all().stream().filter(Module::isEnabled).count();
        Ui.circle(g, x + 21, y + h - 19, 3, Ui.ACCENT);
        g.drawString(font, on + " активно", x + 30, y + h - 23, Ui.MUTED);

        // заголовок
        g.drawString(font, selected.title, x + SIDE + PAD, y + 15, Ui.TEXT);
        String right = ModuleManager.byCategory(selected).size() + " модулей";
        g.drawString(font, right, x + w - 14 - font.width(right), y + 15, Ui.MUTED);

        int viewTop = y + HEAD, viewBottom = y + h - 8;
        g.enableScissor(x + SIDE, viewTop, x + w - 6, viewBottom);
        for (Card c : layout()) {
            if (c.y() + c.h() < viewTop || c.y() > viewBottom) continue;
            Module m = c.module();
            boolean hover = mx >= c.x() && mx <= c.x() + c.w() && my >= Math.max(c.y(), viewTop)
                    && my <= Math.min(c.y() + c.h(), viewBottom);
            float hv = step(HOV, m, hover ? 1f : 0f, dt, 16f);
            float tg = step(TOG, m, m.isEnabled() ? 1f : 0f, dt, 14f);
            float op = get(OPEN, m, 0f);

            int bg = Ui.mix(Ui.CARD, Ui.CARD_HOVER, hv);
            double bw = Ui.px(1.4);
            Ui.rrect(g, c.x(), c.y(), c.w(), c.h(), 9, Ui.mix(bg, Ui.ACCENT, 0.55 * tg));
            Ui.rrect(g, c.x() + bw, c.y() + bw, c.w() - 2 * bw, c.h() - 2 * bw, 9 - bw, bg);

            g.enableScissor(c.x(), Math.max(c.y(), viewTop), c.x() + c.w(), Math.min(c.y() + c.h(), viewBottom));
            g.drawString(font, m.name, c.x() + 10, c.y() + 10, Ui.TEXT);

            if (op > 0.3f) {
                int ly = c.y() + 24;
                for (var line : font.split(Component.literal(m.description), c.w() - 20)) {
                    g.drawString(font, line, c.x() + 10, ly, Ui.MUTED);
                    ly += 10;
                }
            } else {
                g.drawString(font, font.plainSubstrByWidth(m.description, c.w() - 70),
                        c.x() + 10, c.y() + 25, Ui.MUTED);
            }
            if (op > 0.02f) {
                int sy0 = settingsTop(c);
                for (int k = 0; k < m.settings.size(); k++) {
                    drawSetting(g, m.settings.get(k), c, sy0 + k * SET_H);
                }
            }

            Ui.toggle(g, c.x() + c.w() - 50, c.y() + 10, 28, 15, tg);
            Ui.chevron(g, c.x() + c.w() - 14, c.y() + 17.5, 3.5, op, Ui.MUTED);
            g.disableScissor();
        }
        g.disableScissor();

        double max = maxScroll();
        if (max > 0) {
            double track = viewBottom - viewTop - 4;
            double thumb = Math.max(20, track * track / (track + max));
            double ty = viewTop + 2 + (scrollAnim / max) * (track - thumb);
            Ui.rrect(g, x + w - 8, ty, 2.4, thumb, 1.2, Ui.alpha(Ui.ACCENT, 0.55));
        }

        g.pose().popMatrix();
        super.render(g, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = lx(event.x()), my = ly(event.y());
        int x = px(), y = py(), w = pw(), h = ph();

        if (event.button() == 0) {
            Category[] cats = Category.values();
            for (int i = 0; i < cats.length; i++) {
                int sy = sideY(i);
                if (mx >= x + 10 && mx <= x + SIDE - 4 && my >= sy && my <= sy + 22) {
                    selected = cats[i];
                    scroll = 0;
                    scrollAnim = 0;
                    return true;
                }
            }
        }

        if (mx >= x + SIDE && my >= y + HEAD && my <= y + h - 8) {
            for (Card c : layout()) {
                if (mx < c.x() || mx > c.x() + c.w() || my < c.y() || my > c.y() + c.h()) continue;
                Module m = c.module();
                if (my <= c.y() + CARD_H) {
                    boolean chevron = mx >= c.x() + c.w() - 26;
                    if (event.button() == 1 || (event.button() == 0 && chevron)) {
                        if (!EXPANDED.remove(m)) EXPANDED.add(m);
                    } else if (event.button() == 0) {
                        m.toggle();
                    }
                } else if (event.button() == 0 && EXPANDED.contains(m)) {
                    int sy0 = settingsTop(c);
                    List<Setting<?>> list = m.settings;
                    for (int k = 0; k < list.size(); k++) {
                        int ry = sy0 + k * SET_H;
                        if (my >= ry && my <= ry + SET_H) clickSetting(list.get(k), mx, c);
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
            setFromBar(dragSetting, lx(event.x()), dragBarX);
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
        scroll = Math.max(0, Math.min(maxScroll(), scroll - scrollY * 22));
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
