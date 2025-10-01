package com.liboshuai.demo.facade.subsystem;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AudioSystem {
    private boolean musicPlaying = false;

    public void playMusic() {
        log.info("音响开始播放音乐");
        musicPlaying = true;
    }

    public void stopMusic() {
        log.info("音响停止播放音乐");
        musicPlaying = false;
    }

    public boolean getMusicPlaying() {
        return musicPlaying;
    }
}
