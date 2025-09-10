package com.liboshuai.demo.facade.subsystem;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LightSystem {
    private boolean lightsOn = false;

    public void turnOn() {
        log.info("灯光已开启");
        lightsOn = true;
    }

    public void turnOff() {
        log.info("灯光已关闭");
        lightsOn = false;
    }

    public boolean getLightOn() {
        return lightsOn;
    }
}
