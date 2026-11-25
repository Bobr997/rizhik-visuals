package com.rizhik.visuals.client.module;

import com.rizhik.visuals.client.render.WorldFx;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    public final String name;
    public final String description;
    public final Category category;
    public final List<Setting<?>> settings = new ArrayList<>();
    public final List<Setting<?>> hidden = new ArrayList<>();
    private boolean enabled;

    protected Module(String name, Category category) {
        this(name, category, "");
    }

    protected Module(String name, Category category, String description) {
        this.name = name;
        this.category = category;
        this.description = description;
    }

    protected <S extends Setting<?>> S add(S setting) {
        settings.add(setting);
        return setting;
    }

    protected <S extends Setting<?>> S addHidden(S setting) {
        hidden.add(setting);
        return setting;
    }

    public boolean isEnabled() { return enabled; }

    public void toggle() { setEnabled(!enabled); }

    public void setEnabled(boolean value) {
        if (enabled == value) return;
        enabled = value;
        if (enabled) onEnable(); else onDisable();
    }

    protected void onEnable() {}
    protected void onDisable() {}

    public void onTick(Minecraft mc) {}
    public void renderHud(GuiGraphics graphics) {}
    public void renderWorld(WorldFx.Quads quads) {}
}
