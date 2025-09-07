package com.liboshuai.demo;

public enum Planet {

    // 1. 定义枚举实例，并传入构造方法所需的参数
    MERCURY(3.303e+23, 2.4397e6),
    VENUS(4.869e+24, 6.0518e6),
    EARTH(5.976e+24, 6.37814e6),
    MARS(6.421e+23, 3.3972e6);

    // 2. 定义成员变量（必须是 final，保证不可变性）
    private final double mass; // 质量
    private final double radius; // 半径

    // 3. 构造方法必须是 private 或 package-private
    // 因为枚举实例只能在内部定义，不能从外部 new
    private Planet(double mass, double radius) {
        this.mass = mass;
        this.radius = radius;
    }

    // 公共的引力常数
    public static final double G = 6.67300E-11;

    // 4. 定义成员方法，封装行为
    public double surfaceGravity() {
        return G * mass / (radius * radius);
    }

    public double getMass() {
        return mass;
    }

    public double getRadius() {
        return radius;
    }

}
