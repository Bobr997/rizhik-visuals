package com.rizhik.visuals.client.gui;

import com.rizhik.visuals.client.module.CustomHand;
import com.rizhik.visuals.client.module.Module;
import com.rizhik.visuals.client.module.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class HandEditor extends Screen {
    private boolean ctrl, shift;
    private double lastX = Double.NaN, lastY;

    public HandEditor() {
        super(Component.literal("Hand Editor"));
    }

    @Override
    protected void init() {
        for (Module m : ModuleManager.all()) {
            if (m instanceof CustomHand) m.setEnabled(true);
        }
    }

    private ItemStack targetStack() {
        var p = Minecraft.getInstance().player;
        if (p == null) return ItemStack.EMPTY;
        if (CustomHand.tweakFor(p.getMainHandItem()) != null) return p.getMainHandItem();
        if (CustomHand.tweakFor(p.getOffhandItem()) != null) return p.getOffhandItem();
        return ItemStack.EMPTY;
    }

    private CustomHand.Tweak target() {
        ItemStack st = targetStack();
        return st.isEmpty() ? null : CustomHand.tweakFor(st);
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private static String fmt(double v) {
        return String.format("%+.2f", v);
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        CustomHand.Tweak t = target();
        String[] lines;
        if (t == null) {
            lines = new String[]{"Возьми меч, щит или тотем в руку", "Esc — выйти"};
        } else {
            lines = new String[]{
                    CustomHand.labelFor(targetStack()) + ":  размер " + Math.round(t.scale.get() * 100)
                            + "%   X " + fmt(t.x.get()) + "   Y " + fmt(t.y.get()) + "   Z " + fmt(t.z.get()),
                    "Ctrl + мышь — двигать,  Ctrl + колёсико — размер",
                    "Ctrl + Shift + колёсико — глубина,  R — сброс,  Esc — выход"
            };
        }
        int w = 0;
        for (String s : lines) w = Math.max(w, font.width(s));
        int bw = w + 20, bh = lines.length * 11 + 10;
        int bx = (width - bw) / 2, by = 8;
        Render.roundedRect(g, bx, by, bw, bh, 6, 0xCC111114);
        for (int i = 0; i < lines.length; i++) {
            g.drawString(font, lines[i], bx + 10, by + 6 + i * 11, i == 0 ? 0xFFFF7B1D : 0xFFFFFFFF);
        }
        super.render(g, mouseX, mouseY, delta);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (ctrl && !Double.isNaN(lastX)) {
            CustomHand.Tweak t = target();
            if (t != null) {
                double dx = mouseX - lastX, dy = mouseY - lastY;
                t.x.set(clamp(t.x.get() + dx * 0.003, t.x.min, t.x.max));
                t.y.set(clamp(t.y.get() - dy * 0.003, t.y.min, t.y.max));
            }
        }
        lastX = mouseX;
        lastY = mouseY;
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (ctrl) {
            CustomHand.Tweak t = target();
            if (t != null) {
                if (shift) {
                    t.z.set(clamp(t.z.get() + scrollY * 0.05, t.z.min, t.z.max));
                } else {
                    double v = Math.round((t.scale.get() + scrollY * 0.05) * 100) / 100.0;
                    t.scale.set(clamp(v, t.scale.min, t.scale.max));
                }
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int k = event.key();
        if (k == GLFW.GLFW_KEY_LEFT_CONTROL || k == GLFW.GLFW_KEY_RIGHT_CONTROL) ctrl = true;
        if (k == GLFW.GLFW_KEY_LEFT_SHIFT || k == GLFW.GLFW_KEY_RIGHT_SHIFT) shift = true;
        if (k == GLFW.GLFW_KEY_R) {
            CustomHand.Tweak t = target();
            if (t != null) t.reset();
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        int k = event.key();
        if (k == GLFW.GLFW_KEY_LEFT_CONTROL || k == GLFW.GLFW_KEY_RIGHT_CONTROL) ctrl = false;
        if (k == GLFW.GLFW_KEY_LEFT_SHIFT || k == GLFW.GLFW_KEY_RIGHT_SHIFT) shift = false;
        return super.keyReleased(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
