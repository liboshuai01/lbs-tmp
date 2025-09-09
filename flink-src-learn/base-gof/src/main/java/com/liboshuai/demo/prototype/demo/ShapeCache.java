package com.liboshuai.demo.prototype.demo;

import java.util.HashMap;
import java.util.Map;

public class ShapeCache {
    private static Map<String, Shape> shapeMap = new HashMap<>();

    // 从数据库或配置文件加载原型，并克隆一份放入缓存
    public static void loadCache() {
        Circle circle = new Circle();
        circle.setId("1");
        shapeMap.put(circle.getId(), circle);

        Rectangle rectangle = new Rectangle();
        rectangle.setId("2");
        shapeMap.put(rectangle.getId(), rectangle);
    }

    // 对外提供获取克隆对象的方法
    public static Shape getShape(String shapeId) throws CloneNotSupportedException {
        Shape cachedShape = shapeMap.get(shapeId);
        // 关键点：返回的是原型的克隆，而不是原型本身
        return cachedShape.clone();
    }
}
