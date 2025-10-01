package com.liboshuai.demo.adapter.class_adapter;

public class PowerAdapter extends AC220V implements Target{
    @Override
    public int output100V() {
        int chineseOutput = super.output220V();
        return convert(chineseOutput);
    }

    private int convert(int chineseOutput) {
        return (chineseOutput / 2) - 10;
    }
}
