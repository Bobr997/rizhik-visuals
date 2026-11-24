package com.rizhik.visuals.client.gui;

import com.rizhik.visuals.client.module.Markers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MarkersScreen extends Screen {
    private static final int ROWS = 5, PW = 320, PH = 190;
    private EditBox nameBox, xBox, yBox, zBox;

    public MarkersScreen() {
        super(Component.literal("Метки"));
    }

    private int left() { return (width - PW) / 2; }
    private int top() { return Math.max(4, (height - PH) / 2); }

    private EditBox box(int x, int y, int w, String hint) {
        EditBox b = new EditBox(font, x, y, w, 18, Component.literal(hint));
        b.setHint(Component.literal(hint));
        addRenderableWidget(b);
        return b;
    }

    @Override
    protected void init() {
        int l = left(), t = top();
        nameBox = box(l + 10, t + 28, 120, "Название");
        xBox = box(l + 136, t + 28, 50, "X");
        yBox = box(l + 190, t + 28, 40, "Y");
        zBox = box(l + 234, t + 28, 50, "Z");

        addRenderableWidget(Button.builder(Component.literal("Добавить"), b -> add())
                .bounds(l + 10, t + 52, 90, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Моя позиция"), b -> fillPosition())
                .bounds(l + 106, t + 52, 100, 20).build());

        int n = Math.min(ROWS, Markers.LIST.size());
        for (int i = 0; i < n; i++) {
            final int idx = i;
            addRenderableWidget(Button.builder(Component.literal("X"), b -> {
                Markers.LIST.remove(idx);
                Markers.save();
                rebuildWidgets();
            }).bounds(l + 290, t + 80 + i * 20, 20, 18).build());
        }
    }

    private void fillPosition() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        xBox.setValue(String.valueOf(mc.player.getBlockX()));
        yBox.setValue(String.valueOf(mc.player.getBlockY()));
        zBox.setValue(String.valueOf(mc.player.getBlockZ()));
    }

    private void add() {
        double x, y, z;
        try {
            x = Double.parseDouble(xBox.getValue().trim().replace(',', '.'));
            y = Double.parseDouble(yBox.getValue().trim().replace(',', '.'));
            z = Double.parseDouble(zBox.getValue().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return;
        }
        String name = nameBox.getValue().trim();
        if (name.isEmpty()) name = "Метка " + (Markers.LIST.size() + 1);
        Markers.LIST.add(new Markers.Marker(name, x, y, z));
        Markers.save();
        rebuildWidgets();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        g.fill(0, 0, width, height, 0x66000000);
        int l = left(), t = top();
        Render.roundedRect(g, l, t, PW, PH, 8, 0xF0111114);
        g.drawString(font, "Метки", l + 10, t + 10, 0xFFFF7B1D);
        g.drawString(font, "Esc — закрыть", l + PW - 10 - font.width("Esc — закрыть"), t + 10, 0xFF8A8A93);

        int n = Math.min(ROWS, Markers.LIST.size());
        for (int i = 0; i < n; i++) {
            Markers.Marker m = Markers.LIST.get(i);
            int ry = t + 85 + i * 20;
            g.drawString(font, font.plainSubstrByWidth(m.name(), 120), l + 10, ry, 0xFFFFFFFF);
            g.drawString(font, (int) m.x() + "  " + (int) m.y() + "  " + (int) m.z(), l + 136, ry, 0xFF8A8A93);
        }
        if (Markers.LIST.isEmpty()) {
            g.drawString(font, "Меток пока нет. Впиши название и координаты.", l + 10, t + 85, 0xFF8A8A93);
        } else if (Markers.LIST.size() > ROWS) {
            g.drawString(font, "и ещё " + (Markers.LIST.size() - ROWS) + "...", l + 10, t + 85 + ROWS * 20, 0xFF8A8A93);
        }
        super.render(g, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
