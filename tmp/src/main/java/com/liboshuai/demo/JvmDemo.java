package com.liboshuai.demo;

public class JvmDemo {

    private byte[] bigSizeByte = new byte[1024 * 1024 * 5];

    private JvmDemo jvmDemo;

    public void setJvmDemo(JvmDemo jvmDemo) {
        this.jvmDemo = jvmDemo;
    }

    public static void main(String[] args) {
        JvmDemo jvmDemo1 = new JvmDemo();
        JvmDemo jvmDemo2 = new JvmDemo();
        jvmDemo1.setJvmDemo(jvmDemo2);
        jvmDemo2.setJvmDemo(jvmDemo1);
        jvmDemo1 = null;
        jvmDemo2 = null;
        System.gc();
    }
}