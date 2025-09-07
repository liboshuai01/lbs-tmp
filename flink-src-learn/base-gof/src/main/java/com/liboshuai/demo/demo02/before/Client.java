package com.liboshuai.demo.demo02.before;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Client {
    public static void resize(Rectangle rectangle) {
        while (rectangle.getWidth() <= rectangle.getLength()) {
            rectangle.setWidth(rectangle.getWidth() + 1);
        }
    }
}
