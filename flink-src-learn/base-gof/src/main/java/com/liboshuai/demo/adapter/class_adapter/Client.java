package com.liboshuai.demo.adapter.class_adapter;

// Client: 客户端
public class Client {
    public static void main(String[] args) {
        // 客户端需要一个100V的电源
        DC100V adapter = new PowerAdapter();

        // 使用适配器获取100V电压
        int voltage = adapter.output100V();

        // 客户端可以根据返回值进行后续操作
        System.out.println("\n客户端：成功获取到电压 " + voltage + "V");

        if (voltage == 100) {
            System.out.println("客户端：电压符合标准，电器开始工作。");
        } else {
            System.out.println("客户端：警告！电压异常！");
        }
    }
}