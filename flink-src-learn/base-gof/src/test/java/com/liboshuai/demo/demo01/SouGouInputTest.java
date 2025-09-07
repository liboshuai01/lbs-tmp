package com.liboshuai.demo.demo01;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class SouGouInputTest {

    @Test
    void testDefault() {
        AbstractSkin sink = new DefaultSpecificSkin();
        SouGouInput souGouInput = new SouGouInput(sink);
        String result = souGouInput.display();
        assertEquals("默认皮肤", result);
    }

    @Test
    void testHeima() {
        AbstractSkin sink = new HeimaSpecificSkin();
        SouGouInput souGouInput = new SouGouInput(sink);
        String result = souGouInput.display();
        assertEquals("黑马皮肤", result);
    }
}