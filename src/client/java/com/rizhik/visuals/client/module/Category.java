package com.rizhik.visuals.client.module;

public enum Category {
    PVP("PvP"), HUD("HUD"), VISUALS("Visuals"), WORLD("World"), MARKERS("Markers"), MISC("Misc");

    public final String title;

    Category(String title) {
        this.title = title;
    }
}
