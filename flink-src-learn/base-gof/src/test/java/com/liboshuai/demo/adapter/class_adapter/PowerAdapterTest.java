package com.liboshuai.demo.adapter.class_adapter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PowerAdapterTest {

    @Test
    void test1() {
        PowerAdapter powerAdapter = new PowerAdapter();
        int japaneseOutput = powerAdapter.output110V();
        assertEquals(100, japaneseOutput);
    }
}