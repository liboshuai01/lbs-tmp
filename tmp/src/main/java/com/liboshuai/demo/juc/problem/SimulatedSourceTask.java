package com.liboshuai.demo.juc.problem;


/**
 * 这是一个简化的 Source 任务模拟
 * 考察点：JMM (Java Memory Model) 可见性
 */
public class SimulatedSourceTask implements Runnable {

    // -------------------------------------------------------
    // TODO: 请分析这里变量定义的并发隐患
    // -------------------------------------------------------
    private boolean isRunning = true;

    private int count = 0;

    public synchronized void setRunning(boolean running) {
        this.isRunning = running;
    }

    public synchronized boolean isRunning() {
        return isRunning;
    }

    @Override
    public void run() {
        System.out.println("Source Task Started in thread: " + Thread.currentThread().getName());

        // 模拟数据产生循环
        while (isRunning()) {
            count++;
            // 为了让现象更明显，这里不做任何耗时操作，纯粹的CPU空转或极短计算
            // 注意：如果这里加了 System.out.println，可能会因为IO操作强制刷新缓存而掩盖问题，所以这里不加打印
        }

        System.out.println("Source Task Stopped. Total records produced: " + count);
    }

    /**
     * 外部线程调用此方法来停止任务
     */
    public void cancel() {
        System.out.println("Cancel signal received in thread: " + Thread.currentThread().getName());
        setRunning(false);
    }

    // -------------------------------------------------------
    // 测试入口
    // -------------------------------------------------------
    public static void main(String[] args) throws InterruptedException {
        SimulatedSourceTask sourceTask = new SimulatedSourceTask();
        Thread taskThread = new Thread(sourceTask, "Source-Thread");

        taskThread.start();

        // 让任务运行1秒钟
        Thread.sleep(1000);

        // 主线程发起停止命令
        sourceTask.cancel();

        // 等待任务线程结束
        taskThread.join(2000); // 等待2秒

        if (taskThread.isAlive()) {
            System.err.println("严重BUG: 任务无法停止！主线程修改了标识位，但子线程仿佛没看见！");
            // 强制退出防止测试卡死
            System.exit(1);
        } else {
            System.out.println("测试通过：任务正常停止。");
        }
    }
}
