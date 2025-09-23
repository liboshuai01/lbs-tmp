package com.liboshuai.demo;

public class JvmDemo {
    public static void main(String[] args) {
        String s3 = new String("1") + new String("1");
        String s5 = "11";
        String s4 = s3.intern();
        System.out.println(s3 == s5); // false
        System.out.println(s4 == s5); // true
    }
}