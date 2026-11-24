package com.rizhik.visuals.client.module;

public class ModeSetting extends Setting<Integer> {
    public final String[] options;

    public ModeSetting(String name, int index, String... options) {
        super(name, index, 0, options.length - 1);
        this.options = options;
    }

    public String current() {
        return options[Math.floorMod(get(), options.length)];
    }

    public void next() {
        set((get() + 1) % options.length);
    }
}
