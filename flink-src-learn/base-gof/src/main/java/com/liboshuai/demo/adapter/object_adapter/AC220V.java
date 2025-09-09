package com.liboshuai.demo.adapter.object_adapter;

// Adaptee: 被适配者，一个提供220V交流电的电源
public class AC220V {
    public int output220V() {
        int voltage = 220;
        System.out.println("--- [被适配者] 我是一个220V电源，输出电压: " + voltage + "V ---");
        return voltage;
    }
}
