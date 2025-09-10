package com.liboshuai.demo.facade;

import com.liboshuai.demo.facade.subsystem.AudioSystem;
import com.liboshuai.demo.facade.subsystem.LightSystem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SmartHomeFacadeTest {

    @Test
    void testEnterHome() {
        AudioSystem audioSystem = new AudioSystem();
        LightSystem lightSystem = new LightSystem();
        SmartHomeFacade smartHomeFacade = new SmartHomeFacade(audioSystem, lightSystem);
        smartHomeFacade.enterHome();
        assertTrue(audioSystem.getMusicPlaying());
        assertTrue(lightSystem.getLightOn());
    }

    @Test
    void testLeaveHome() {
        AudioSystem audioSystem = new AudioSystem();
        LightSystem lightSystem = new LightSystem();
        SmartHomeFacade smartHomeFacade = new SmartHomeFacade(audioSystem, lightSystem);
        smartHomeFacade.enterHome();
        smartHomeFacade.leaveHome();
        assertFalse(audioSystem.getMusicPlaying());
        assertFalse(lightSystem.getLightOn());
    }
}