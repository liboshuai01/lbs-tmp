package com.liboshuai.demo.queue;

import java.util.UUID;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * 模拟要部署的任务的描述符。
 */
class TaskDeployment {
    private final UUID taskId;
    private final String taskName;

    public TaskDeployment(UUID taskId, String taskName) {
        this.taskId = taskId;
        this.taskName = taskName;
    }

    public UUID getTaskId() {
        return taskId;
    }

    @Override
    public String toString() {
        return "TaskDeployment{" +
                "taskId=" + taskId +
                ", taskName='" + taskName + '\'' +
                '}';
    }
}

/**
 * 模拟 JobManager，作为任务的生产者。
 * 它发出一个任务部署请求，并等待 TaskExecutor 的 Slot 接收。
 */
class JobManagerService implements Runnable {
    // SynchronousQueue 作为直接的“握手”通道
    private final SynchronousQueue<TaskDeployment> deploymentChannel;
    private final String taskName;

    public JobManagerService(SynchronousQueue<TaskDeployment> deploymentChannel, String taskName) {
        this.deploymentChannel = deploymentChannel;
        this.taskName = taskName;
    }

    @Override
    public void run() {
        try {
            TaskDeployment task = new TaskDeployment(UUID.randomUUID(), this.taskName);
            System.out.println("[JobManager] 准备部署任务: " + task + ". 等待 Slot 接收...");

            // put() 操作会一直阻塞，直到另一个线程调用 take()。
            // 这就像 JobManager 发送了一个RPC请求，并同步等待对方的确认。
            // offer(task, timeout, unit) 是一个更安全的选择，可以防止无限期等待。
            boolean handedOff = deploymentChannel.offer(task, 10, TimeUnit.SECONDS);

            if (handedOff) {
                System.out.println("[JobManager] 任务 " + task.getTaskId() + " 已成功交接给一个 Slot。");
            } else {
                System.out.println("[JobManager] 错误: 在10秒内没有 Slot 接收任务 " + task.getTaskId() + "，部署失败！");
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("[JobManager] 部署过程被中断。");
        }
    }
}

/**
 * 模拟 TaskExecutor 上的一个 Slot，作为任务的消费者。
 * 它等待接收任务，一旦接收就立即开始处理。
 */
class TaskExecutorSlot implements Runnable {
    private final SynchronousQueue<TaskDeployment> deploymentChannel;
    private final int slotId;

    public TaskExecutorSlot(SynchronousQueue<TaskDeployment> deploymentChannel, int slotId) {
        this.deploymentChannel = deploymentChannel;
        this.slotId = slotId;
    }

    @Override
    public void run() {
        try {
            System.out.println("[Slot-" + slotId + "] 空闲，正在等待任务...");

            // take() 操作会一直阻塞，直到另一个线程调用 put()。
            // 这就像 Slot 在监听一个端口，等待 JobManager 的部署命令。
            TaskDeployment taskToRun = deploymentChannel.take();

            // 一旦 take() 返回，表示“握手”成功，任务已收到。
            System.out.println("[Slot-" + slotId + "] 已接收任务: " + taskToRun + "。开始部署...");

            // 模拟任务部署和执行过程
            TimeUnit.SECONDS.sleep(3);

            System.out.println("[Slot-" + slotId + "] 成功完成任务: " + taskToRun);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("[Slot-" + slotId + "] 在等待或执行任务时被中断。");
        }
    }
}

public class SynchronousQueueDemo {

    public static void main(String[] args) throws InterruptedException {
        // 创建一个 SynchronousQueue。
        // 它没有容量，只能用于线程间的直接传递。
        // "true" 参数表示公平策略，等待时间最长的线程会优先获得配对机会。
        final SynchronousQueue<TaskDeployment> deploymentChannel = new SynchronousQueue<>(true);

        System.out.println("场景一: Slot 先启动，等待 JobManager 的任务。");
        // 启动一个 Slot 线程，它会立即调用 take() 并阻塞。
        Thread slotThread1 = new Thread(new TaskExecutorSlot(deploymentChannel, 1));
        slotThread1.start();

        // 等待片刻，确保 Slot 已经处于等待状态
        TimeUnit.SECONDS.sleep(1);

        // JobManager 线程启动，调用 put()，由于有 Slot 在等待，会立即成功交接。
        Thread jobManagerThread1 = new Thread(new JobManagerService(deploymentChannel, "WordCount"));
        jobManagerThread1.start();

        // 等待他们完成
        slotThread1.join();
        jobManagerThread1.join();

        // -- FIX: String.repeat() is not available in Java 8. Replaced with a compatible alternative. --
        System.out.println("\n" + new String(new char[60]).replace('\0', '=') + "\n");

        System.out.println("场景二: JobManager 先启动，必须等待 Slot 准备好。");
        // JobManager 线程启动，调用 put()，但没有 Slot 在等待，所以它会阻塞。
        Thread jobManagerThread2 = new Thread(new JobManagerService(deploymentChannel, "DataStream Join"));
        jobManagerThread2.start();

        // 等待片刻，观察 JobManager 的等待状态
        System.out.println("(主线程) JobManager 已发出请求，现在等待 3 秒再启动 Slot...");
        TimeUnit.SECONDS.sleep(3);
        System.out.println("(主线程) 现在启动 Slot...");

        // 启动 Slot 线程，它调用 take()，与正在等待的 JobManager 线程成功配对并交接。
        Thread slotThread2 = new Thread(new TaskExecutorSlot(deploymentChannel, 2));
        slotThread2.start();

        // 等待他们完成
        jobManagerThread2.join();
        slotThread2.join();

        System.out.println("\nDemo finished.");
    }
}