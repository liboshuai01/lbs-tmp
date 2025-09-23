package com.liboshuai.demo;

public class JvmDemo {
    public static void main(String[] args) {
        final String s1 = "a";
        final String s2 = "b";
        String s3 = "a" + "b";
        String s4 = s1 + s2;
        System.out.println(s3 == s4); // true
    }
}