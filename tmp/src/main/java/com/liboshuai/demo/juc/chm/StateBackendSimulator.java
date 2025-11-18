package com.liboshuai.demo.juc.chm;

import java.util.concurrent.ConcurrentHashMap;

public class StateBackendSimulator {

    // 模拟状态缓存：Key 是状态名，Value 是状态对象
    private final ConcurrentHashMap<String, Object> stateCache = new ConcurrentHashMap<>();

    // 模拟一个“昂贵”的状态创建过程
    public interface StateFactory {
        Object create(String name);
    }

    /**
     * 获取或创建状态
     *
     * @param stateName 状态名称
     * @param factory   如果状态不存在，用于创建状态的工厂
     * @return 缓存中的状态对象
     */
    public Object getOrCreateState(String stateName, StateFactory factory) {
        // TODO: 请在这里编写代码
        // 要求：
        // 1. 线程安全
        // 2. 确保对于同一个 stateName，factory.create 只被执行一次
        // 3. 返回最终的 State 对象
        return stateCache.computeIfAbsent(stateName, factory::create);
//        return o; // 占位符
    }

    // --- 测试用的辅助代码 (你不需要修改) ---
    public static void main(String[] args) {
        StateBackendSimulator backend = new StateBackendSimulator();
        StateFactory heavyFactory = name -> {
            System.out.println("Creating state for: " + name); // 这一行对于每个名字只应该打印一次
            return new Object();
        };

        // 模拟多线程并发请求同一个状态
        Runnable task = () -> {
            Object state = backend.getOrCreateState("my-value-state", heavyFactory);
            System.out.println(Thread.currentThread().getName() + " got state: " + state.hashCode());
        };

        new Thread(task, "Thread-A").start();
        new Thread(task, "Thread-B").start();
        new Thread(task, "Thread-C").start();
    }
}
