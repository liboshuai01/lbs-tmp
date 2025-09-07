package com.liboshuai.demo;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class PlanetTest {

    @Test
    void test() {
        double earthWeight = 185.0; // 假设一个人的体重
        double mass = earthWeight / Planet.EARTH.surfaceGravity();

        // 遍历所有枚举实例并打印他们在不同行星上的体重
        for (Planet p : Planet.values()) {
            log.info("你的体重在[{}]上为[{}]", p.name(), mass * p.surfaceGravity());
        }
    }

}