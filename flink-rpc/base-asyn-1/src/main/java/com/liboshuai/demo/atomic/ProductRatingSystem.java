package com.liboshuai.demo.atomic;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class ProductRatingSystem {

    /**
     * 评分的维度数量
     */
    private static final int RATING_DIMENSIONS = 4;

    /**
     * 存储各个评分维度的评分
     */
    private static final AtomicIntegerArray ratings = new AtomicIntegerArray(RATING_DIMENSIONS);

    public void rate(int dimension, int score) {
        if (dimension >= 0 && dimension < RATING_DIMENSIONS) {
            ratings.addAndGet(dimension, score);
        }
    }

    public int getRatingForDimension(int dimension) {
        if (dimension >= 0 && dimension < RATING_DIMENSIONS) {
            return ratings.get(dimension);
        }
        return -1;
    }

    public static void main(String[] args) throws InterruptedException {
        final int userCount = 1000;
        final CountDownLatch countDownLatch = new CountDownLatch(userCount);

        ProductRatingSystem productRatingSystem = new ProductRatingSystem();
        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        for (int i = 0; i < userCount; i++) {
            threadPool.execute(() -> {
                int dimension = (int) (Math.random() * RATING_DIMENSIONS);
                int score = (int) (Math.random() * 5 + 1);
                productRatingSystem.rate(dimension, score);
                countDownLatch.countDown();
            });
        }

        System.out.println(">>> 等待所有用户完成评分 <<<");
        countDownLatch.await();

        threadPool.shutdown();

        System.out.println("最终各维度总评分如下：");
        System.out.printf("- 质量 (索引0) 总分: %d%n", productRatingSystem.getRatingForDimension(0));
        System.out.printf("- 外观 (索引1) 总分: %d%n", productRatingSystem.getRatingForDimension(1));
        System.out.printf("- 价格 (索引2) 总分: %d%n", productRatingSystem.getRatingForDimension(2));
        System.out.printf("- 服务 (索引3) 总分: %d%n", productRatingSystem.getRatingForDimension(3));
    }
}
