package com.liboshuai.demo.mailbox;


import java.util.Random;

/**
 * 模拟网络 I/O 线程。
 * 它的工作就是不断地往 InputGate 里塞数据。
 */
public class MockNettyThread extends Thread {

    private final MockInputGate inputGate;
    private volatile boolean running = true;

    public MockNettyThread(MockInputGate inputGate) {
        super("Netty-IO-Thread");
        this.inputGate = inputGate;
    }

    @Override
    public void run() {
        int seq = 0;
        Random random = new Random();

        while (running) {
            try {
                // 1. 模拟网络数据到达的不确定性 (时快时慢)
                // 有时很快 (100ms)，有时会卡顿 (2000ms)，从而触发 Task 挂起
                int sleepTime = random.nextInt(10) > 7 ? 2000 : 100;
                Thread.sleep(sleepTime);

                // 2. 生成数据
                String data = "Record-" + (++seq);

                // 3. 推送到 InputGate (这可能会触发 SourceTask 的 resume)
                // System.out.println("[Netty] Receiving " + data);
                inputGate.pushData(data);

            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void shutdown() {
        running = false;
        this.interrupt();
    }
}
