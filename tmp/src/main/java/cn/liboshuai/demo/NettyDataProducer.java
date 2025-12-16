package cn.liboshuai.demo;


import java.util.Random;
import java.util.concurrent.TimeUnit;

public class NettyDataProducer extends Thread {

    private final MiniInputGate inputGate;
    private volatile boolean running = true;

    public NettyDataProducer(MiniInputGate inputGate) {
        super("Netty-Thread");
        this.inputGate = inputGate;
    }

    @Override
    public void run() {
        Random random = new Random();
        long seq = 0;
        while (running) {
            int sleep = random.nextInt(100) < 5 ? 200 : 5;
            try {
                TimeUnit.MILLISECONDS.sleep(sleep);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            inputGate.pushData("record-" + seq);
        }
    }

    public void shutdown() {
        running = false;
        this.interrupt();
    }
}
