package com.liboshuai.demo.adapter.object_adapter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PowerAdapterTest {
    @Test
    void test() {
        PowerAdapter powerAdapter = new PowerAdapter(new AC220V());
        int japaneseOutput = powerAdapter.output100V();
        assertEquals(100, japaneseOutput);
    }
}