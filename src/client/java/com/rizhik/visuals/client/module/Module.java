package com.rizhik.visuals.client.module;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    public final String name;
    public final Category category;
    public final List<Setting<?>> settings = new ArrayList<>();
    private boolean enabled;

    protected Module(String name, Category category) {
        this.name = name;
        this.category = category;
    }

    protected <S extends Setting<?>> S add(S setting) {
        settings.add(setting);
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
}
