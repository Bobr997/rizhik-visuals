package com.rizhik.visuals.client.module;

public class Setting<T> {
    public final String name;
    public final double min, max;
    private T value;

    public Setting(String name, T value) {
        this(name, value, 0, 0);
    }

    public Setting(String name, T value, double min, double max) {
        this.name = name;
        this.value = value;
        this.min = min;
        this.max = max;
    }

    public T get() { return value; }
    public void set(T value) { this.value = value; }
}
