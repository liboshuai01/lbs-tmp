package com.liboshuai.demo.juc;


public class SourceStreamTask extends StreamTask {

    private int counter = 0;
    private final int maxData = 20; // 模拟处理20条数据后结束

    // 模拟算子状态
    private int stateSum = 0;

    @Override
    protected void init() {
        System.out.println("[Source] 打开 InputGate...");
    }

    @Override
    protected void processInput(Controller controller) throws Exception {
        // 1. 模拟从网络/磁盘读取数据的耗时
        Thread.sleep(100);

        // 2. 处理数据 (修改状态)
        counter++;
        stateSum += counter;
        System.out.println("[Source] 处理数据 Record ID: " + counter + ", 当前状态 Sum: " + stateSum);

        // 3. 判断是否结束
        if (counter >= maxData) {
            System.out.println("[Source] 数据读取完毕.");
            controller.allFinished();
        }
    }

    @Override
    protected void performCheckpoint(long checkpointId) {
        // 在主线程中打印状态，不需要加锁
        System.out.println("   >>> [CP] Checkpoint " + checkpointId + " 保存状态: Sum=" + stateSum);
    }

    @Override
    protected void cleanup() {
        System.out.println("[Source] 清理资源.");
    }
}
