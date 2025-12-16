package cn.liboshuai.demo;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 数据生产者 (与 Mailbox 版本一致)
 */
public class NettyDataProducer extends Thread{
    private final MiniInputGate inputGate;
    private volatile boolean running = true;

    public NettyDataProducer(MiniInputGate inputGate) {
        super("Netty-Thread");
        this.inputGate = inputGate;
    }

    @Override
    public void run() {
        Random random = new Random();
        int seq = 0;
        while (running) {
            try {
                // 模拟高频数据输入, 给锁造成压力
                int sleep = random.nextInt(100) < 5 ? 200 : 5;
                TimeUnit.MILLISECONDS.sleep(sleep);
                String data = "Record-" + (++seq);
                inputGate.pushData(data);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void shutdown() {
        running = false;
        this.interrupt();
    }
}
