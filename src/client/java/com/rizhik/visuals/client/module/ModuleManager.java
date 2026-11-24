package com.rizhik.visuals.client.module;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private static final List<Module> MODULES = new ArrayList<>();

    public static void register(Module module) { MODULES.add(module); }

    public static List<Module> all() { return MODULES; }

    public static List<Module> byCategory(Category category) {
        return MODULES.stream().filter(m -> m.category == category).toList();
    }
}
