package com.liboshuai.demo.demo02.after;

import com.liboshuai.demo.demo02.before.Client;
import com.liboshuai.demo.demo02.before.Rectangle;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {

    @Test
    void testResize() {
        com.liboshuai.demo.demo02.before.Rectangle rectangle = new Rectangle();
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            Client.resize(rectangle);
        });
        assertTrue(rectangle.getWidth() > rectangle.getLength());
    }
}