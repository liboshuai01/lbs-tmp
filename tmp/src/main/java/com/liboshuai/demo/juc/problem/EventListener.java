package com.liboshuai.demo.juc.problem;


import java.util.concurrent.TimeUnit;

public class EventListener {

    private int id;
    private String config;
    private final EventSource source;

    private EventListener(EventSource source, int id) {
        this.id = id;

        // 🛑 致命错误在这里：在构造函数里把 'this' 传出去了！
//        source.register(this);
        this.source = source;

        // 模拟繁重的初始化工作 (比如读取数据库、解析配置)
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) { }

        this.config = "Completed";
    }

    public static EventListener createAndRegister(EventSource source, int id) {
        EventListener eventListener = new EventListener(source, id);
        eventListener.registerSource();
        return eventListener;
    }

    public void registerSource() {
        source.register(this);
    }

    public void onEvent(Object e) {
        // 只要注册了，EventSource 就会调用这个方法
        System.out.println("收到事件, ID: " + this.id + ", Config: " + this.config);
    }

    public static void main(String[] args) {
         EventListener.createAndRegister(new EventSource(), 1);
    }
}

// 模拟事件源
class EventSource {
    public void register(EventListener listener) {
        // 模拟注册后立刻回调（或者另一个线程立刻发消息）
        new Thread(() -> {
            listener.onEvent("TEST");
        }).start();
    }
}
