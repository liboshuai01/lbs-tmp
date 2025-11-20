package com.liboshuai.demo.juc.problem;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadLocalTrap {

    // 模拟一个固定大小的线程池 (Tomcat/Flink 都是这么干的)
    private static final ExecutorService pool = Executors.newFixedThreadPool(2);

    // 用于存放当前处理的 User ID
    private static final ThreadLocal<String> currentUser = new ThreadLocal<>();

    public static void main(String[] args) {
        // 模拟 3 个请求
        // 请求 1: 来自用户 "Alice"
        pool.submit(() -> handleRequest("Alice"));

        // 请求 2: 来自用户 "Bob"
        pool.submit(() -> handleRequest("Bob"));

        // 请求 3: 来自 "匿名用户" (未携带 UserID)
        // 🛑 我们预期匿名用户处理时不应该有名字
        pool.submit(() -> handleRequest(null));

        pool.shutdown();
    }

    private static void handleRequest(String userId) {
        try {
            // 1. 如果 userId 不为空，存入 ThreadLocal
            if (userId != null) {
                currentUser.set(userId);
                System.out.println(Thread.currentThread().getName() + " 保存用户: " + userId);
            }

            // ... 模拟复杂的业务逻辑调用链 ...
            processBusiness();
        } finally {
            currentUser.remove();
        }
    }

    private static void processBusiness() {
        // 2. 在业务深处读取当前用户
        String user = currentUser.get();
        System.out.println(Thread.currentThread().getName() + " 正在处理业务，当前用户: " + (user == null ? "GUEST" : user));
    }
}