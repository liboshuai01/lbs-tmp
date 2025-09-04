package com.liboshuai.demo;

public class Calculator {
    public int add(int a, int b) {
        return a + b;
    }

    public double divide(double dividend, double divisor) {
        if (divisor == 0) {
            throw new IllegalArgumentException("Divisor cannot be zero");
        }
        return dividend / divisor;
    }
}