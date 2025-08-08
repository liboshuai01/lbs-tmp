package com.liboshuai.demo.thread.juc;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CyclicBarrierDemo {
    public static void main(String[] args) {
        // 玩家数量
        int playerCount = 5;

        // 创建一个CyclicBarrier，屏障动作是宣布游戏开始
        CyclicBarrier cyclicBarrier = new CyclicBarrier(playerCount, () -> {
            System.out.println("======================================");
            System.out.println("所有玩家加载完毕，游戏正式开始！");
            System.out.println("======================================");
        });
        ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(playerCount);

        for (int i = 0; i < playerCount; i++) {
            newFixedThreadPool.execute(new Player("玩家" + i, cyclicBarrier));
        }
        newFixedThreadPool.shutdown();
    }
}

class Player implements Runnable {
    private final String name;
    private final CyclicBarrier cyclicBarrier;
    public Player(String name, CyclicBarrier cyclicBarrier) {
        this.name = name;
        this.cyclicBarrier = cyclicBarrier;
    }

    @Override
    public void run() {
        // 模拟加载消耗
        long loadingTime = (long) (Math.random() * 5000 + 1000);
        System.out.printf("%s 正在加载游戏，预计需要 %.1f 秒...%n", name, loadingTime / 1000.0);
        try {
            TimeUnit.MILLISECONDS.sleep(loadingTime);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.printf("%s 加载完成，已在起跑线等待...%n", name);
        // 通知 cycliBarrier，我已经到达屏障点，开始等待
        try {
            cyclicBarrier.await();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // 所有玩家都达到后，这里的代码才会开始执行
        System.out.printf("--- %s 开始游戏 ---%n", name);
    }
}
