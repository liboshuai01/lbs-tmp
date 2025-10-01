package com.liboshuai.demo.facade;

import com.liboshuai.demo.facade.subsystem.AudioSystem;
import com.liboshuai.demo.facade.subsystem.LightSystem;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SmartHomeFacade {
    private final AudioSystem audioSystem;
    private final LightSystem lightSystem;

    public SmartHomeFacade(AudioSystem audioSystem, LightSystem lightSystem) {
        this.audioSystem = audioSystem;
        this.lightSystem = lightSystem;
    }

    public void enterHome() {
        log.info("欢迎回家！启动回家模式...");
        audioSystem.playMusic();
        lightSystem.turnOn();
    }

    public void leaveHome() {
        log.info("再见！启动离家模式...");
        audioSystem.stopMusic();
        lightSystem.turnOff();
    }
}
