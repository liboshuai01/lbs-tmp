package com.liboshuai.demo.atomic.atomicReferenceFieldUpdateDemo;

public class TaskResult {
    private final String resultData;

    public TaskResult(String resultData) {
        this.resultData = resultData;
        System.out.printf("线程 [%s]: 正在创建任务结果对象... (这是一个耗时操作)%n", Thread.currentThread().getName());
        // 模拟结果对象创建很耗时
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public String toString() {
        return "TaskResult{" +
                "resultData='" + resultData + '\'' +
                '}';
    }
}
