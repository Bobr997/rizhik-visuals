package com.rizhik.visuals.client.module;

public enum Category {
    PVP("PvP"), HUD("HUD"), VISUALS("Visuals"), WORLD("World"), MISC("Misc");

    public final String title;

    Category(String title) {
        this.title = title;
    }
}
