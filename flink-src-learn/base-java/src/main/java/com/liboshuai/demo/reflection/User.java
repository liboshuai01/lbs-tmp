package com.liboshuai.demo.reflection;


@MyAnnotation("这是一个类注解")
public class User {
    private String name;
    private int age;
    public String address;

    // 构造方法
    private User() {
        this.name = "默认用户";
        this.age = 0;
        this.address = "未知";
    }

    public User(String name, int age) {
        this.name = name;
        this.age = age;
        this.address = "北京";
    }

    // 成员方法
    public void publicMethod() {
        System.out.println("这是一个公开方法！");
    }

    private String privateMethod(String message) {
        System.out.println("这是一个私有方法，收到的消息是：" + message);
        return "私有方法返回成功";
    }

    public static void staticMethod() {
        System.out.println("这是一个静态方法！");
    }

    // 重写 toString 方法，方便打印对象信息
    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", address='" + address + '\'' +
                '}';
    }
}
