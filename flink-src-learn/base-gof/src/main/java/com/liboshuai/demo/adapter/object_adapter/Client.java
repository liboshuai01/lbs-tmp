package com.liboshuai.demo.adapter.object_adapter;

// Client: 客户端
public class Client {
    public static void main(String[] args) {
        // 1. 准备一个被适配者实例（220V电源）
        AC220V ac220vSource = new AC220V();

        // 2. 创建适配器，并将被适配者实例传递进去
        DC100V adapter = new PowerAdapter(ac220vSource);

        // 3. 客户端像之前一样使用目标接口
        int voltage = adapter.output100V();

        System.out.println("\n客户端：成功获取到电压 " + voltage + "V");

        if (voltage == 100) {
            System.out.println("客户端：电压符合标准，电器开始工作。");
        } else {
            System.out.println("客户端：警告！电压异常！");
        }
    }
}