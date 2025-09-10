package com.liboshuai.demo.flyweight;

import java.util.HashMap;
import java.util.Map;

public class ShapeFactory {
    private static final Map<String, Shape> MAP = new HashMap<>();

    public static Shape getShape(String color) {
        return MAP.computeIfAbsent(color, Circle::new);
    }

    public static int getMapSize() {
        return MAP.size();
    }
}
