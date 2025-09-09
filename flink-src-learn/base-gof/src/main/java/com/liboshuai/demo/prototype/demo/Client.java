package com.liboshuai.demo.prototype.demo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Client {
    public static void main(String[] args) throws CloneNotSupportedException {
        // 1. 加载原型到缓存中
        ShapeCache.loadCache();

        // 2. 客户端根据需要从缓存获取对象的克隆
        Shape clonedShape1 = ShapeCache.getShape("1");
        log.info("Shape : {}@{}", clonedShape1.getType(), clonedShape1.hashCode());
        clonedShape1.draw();

        Shape clonedShape2 = ShapeCache.getShape("2");
        log.info("Shape : {}@{}", clonedShape2.getType(), clonedShape2.hashCode());
        clonedShape2.draw();

        // 3. 再次获取圆形，验证是否为新的克隆对象
        Shape clonedShape3 = ShapeCache.getShape("1");
        log.info("Shape : {}@{}", clonedShape3.getType(), clonedShape3.hashCode());
        clonedShape3.draw();
    }
}
