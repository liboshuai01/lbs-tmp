package com.liboshuai.demo.completableFuture;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ComparePriceDemo {
    public static void main(String[] args) {
        Instant start = Instant.now();
        List<String> products = Arrays.asList("小米耳机", "红米耳机", "苹果耳机", "华为耳机");
        List<String> resultList = products.stream()
                .map(product ->
                        CompletableFuture.supplyAsync(() -> getPrice(product))
                ).collect(Collectors.toList())
                .stream().map(CompletableFuture::join).collect(Collectors.toList());
        System.out.println("结果: " + resultList);
        Instant end = Instant.now();
        System.out.println("耗时: " + Duration.between(start, end).toMillis());
    }

    static String getPrice(String product) {
        // 为了更好地观察到并发效果，模拟1秒的耗时操作（如网络请求）
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 计算一个随机价格
        double price = 20 + Math.random() * (200 - 20);
        return String.format("线程 [%s] 获取到商品 [%s] 的价格为: [%.2f]%n",
                Thread.currentThread().getName(),
                product,
                price);
    }
}
