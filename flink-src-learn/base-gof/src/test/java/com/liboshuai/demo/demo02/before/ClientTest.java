package com.liboshuai.demo.demo02.before;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ClientTest {

    @Test
    void testRectangle() {
        Rectangle rectangle = new Rectangle();
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            Client.resize(rectangle);
        });
        assertTrue(rectangle.getWidth() > rectangle.getLength());
    }

    @Test
    void testSquare() {
        Rectangle rectangle = new Square();
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            Client.resize(rectangle);
        });
        assertTrue(rectangle.getWidth() > rectangle.getLength());
    }

}