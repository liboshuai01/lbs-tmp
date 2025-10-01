package com.liboshuai.demo.adapter.object_adapter;

public class PowerAdapter implements Target {

    private final AC220V ac220V;

    public PowerAdapter(AC220V ac220V) {
        this.ac220V = ac220V;
    }

    @Override
    public int output100V() {
        int chineseOutput = ac220V.output220V();
        return convert(chineseOutput);
    }

    private int convert(int chineseOutput) {
        return (chineseOutput / 2) - 10;
    }
}
