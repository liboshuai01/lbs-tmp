package com.liboshuai.demo.thread.juc;

import java.util.concurrent.*;

public class CyclicBarrierDemo {
    public static void main(String[] args) {
        int playerCount = 5;
        CyclicBarrier cyclicBarrier = new CyclicBarrier(playerCount, () -> {
            System.out.println("所有玩家已准备就绪，开始下一轮！");
        });
        ExecutorService threadPool = Executors.newFixedThreadPool(playerCount);
        for (int i = 0; i < playerCount; i++) {
            threadPool.execute(new Player(i + 1, cyclicBarrier));
        }
        threadPool.shutdown();
    }
}

class Player implements Runnable {

    private final int playerId;
    private final CyclicBarrier cyclicBarrier;

    public Player(int playerId, CyclicBarrier cyclicBarrier) {
        this.playerId = playerId;
        this.cyclicBarrier = cyclicBarrier;
    }

    @Override
    public void run() {
        try {
            System.out.printf("玩家 %d 正在准备......%n", playerId);
            TimeUnit.MILLISECONDS.sleep((long)(Math.random() * 2000 + 1000));
            System.out.printf("玩家 %d 已经准备就绪，等待游戏开始......%n", playerId);
            cyclicBarrier.await();
            System.out.printf("玩家 %d 开始游戏......%n", playerId);
            TimeUnit.MILLISECONDS.sleep((long)(Math.random() * 2000 + 1000));
            System.out.printf("玩家 %d 完成本轮......%n", playerId);
            cyclicBarrier.await();
            System.out.printf("玩家 %d 游戏结束......%n", playerId);
        } catch (InterruptedException | BrokenBarrierException e) {
            System.err.printf("玩家 %d 出现异常：%s%n", playerId, e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
