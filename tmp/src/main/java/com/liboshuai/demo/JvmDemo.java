package com.liboshuai.demo;

public class JvmDemo {
    public static void main(String[] args) {
        String s = new String("a") + new String("b");
        String s2 = s.intern();

        System.out.println(s == "ab"); // true
        System.out.println(s2 == "ab"); // true
    }
}