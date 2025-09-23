package com.liboshuai.demo;

public class JvmDemo {
    public static void main(String[] args) {
        String s = new String("1");
        String s1 = s.intern();
        String s2 = "1";
        System.out.println(s == s2); // false
        System.out.println(s1 == s2); // true

        String s3 = new String("1") + new String("1");
        String s4 = s3.intern();
        String s5 = "11";
        System.out.println(s3 == s5); // true
        System.out.println(s4 == s5); // true
    }
}