package com.liboshuai.demo.spi;

import java.util.ServiceLoader;

public class SpiMain {
    public static void main(String[] args) {
        System.out.println("开始通过 SPI 查找 Search 服务...");

        // 1. 使用 ServiceLoader 加载服务
        ServiceLoader<Search> loader = ServiceLoader.load(Search.class);

        // 2. 遍历并使用所有找到的服务
        int serviceCount = 0;
        for (Search search : loader) {
            serviceCount++;
            System.out.println("-------------------------");
            System.out.println("找到服务实现: " + search.getClass().getName());
            search.search("Hello SPI");
        }

        System.out.println("-------------------------");
        if(serviceCount == 0) {
            System.out.println("未找到任何 Search 服务实现。");
        } else {
            System.out.println("SPI 服务查找完毕，共找到 " + serviceCount + " 个服务。");
        }
    }
}
