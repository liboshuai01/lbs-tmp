package com.liboshuai.demo;

public class JvmDemo {
    public static void main(String[] args) {
        String s1 = "JavaeeHadoop";
        String s2 = "Javaee";
        String s3 = s2 + "hadoop";
        System.out.println(s1 == s3); // false

        final String s4 = "Javaee";
        String s5 = s4 + "Hadoop";
        System.out.println(s1 == s5); // true
    }
}