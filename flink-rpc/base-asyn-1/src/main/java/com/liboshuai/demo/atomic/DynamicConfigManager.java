package com.liboshuai.demo.atomic;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class DynamicConfigManager {

    private static final class AppConfig {
        private final String version;
        private final double discountRate;

        public AppConfig(String version, double discountRate) {
            this.version = version;
            this.discountRate = discountRate;
        }

        public String getVersion() {
            return version;
        }

        public double getDiscountRate() {
            return discountRate;
        }

        @Override
        public String toString() {
            return "AppConfig{" +
                    "version='" + version + '\'' +
                    ", discountRate=" + discountRate +
                    '}';
        }
    }

    private static final AtomicReference<AppConfig> currentConfig = new AtomicReference<>(new AppConfig("v1.10", 0.5));

    public void processBusiness() {
        while (!Thread.currentThread().isInterrupted()) {
            AppConfig config = currentConfig.get();
            System.out.printf("线程 [%s] 正在使用配置 %s 计算价格，折扣率为: %.2f%n",
                    Thread.currentThread().getName(), config.getVersion(), config.getDiscountRate());
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void updateConfig(String newVersion, double newDiscountRate) {
        AppConfig appConfig = new AppConfig(newVersion, newDiscountRate);
        currentConfig.set(appConfig);
        System.out.printf("-------> 配置已成功更新为: %s <-------%n", appConfig);
    }

    public static void main(String[] args) throws InterruptedException {
        DynamicConfigManager dynamicConfigManager = new DynamicConfigManager();
        int userCount = 1000;
        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        for (int i = 0; i < userCount; i++) {
            threadPool.execute(dynamicConfigManager::processBusiness);
        }
        TimeUnit.SECONDS.sleep(2);
        dynamicConfigManager.updateConfig("v1.11", 0.6);
        TimeUnit.SECONDS.sleep(2);
        dynamicConfigManager.updateConfig("v1.12", 1.1);
        TimeUnit.SECONDS.sleep(2);
        threadPool.shutdownNow();
    }
}
