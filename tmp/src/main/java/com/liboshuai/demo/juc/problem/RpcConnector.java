package com.liboshuai.demo.juc.problem;


import java.util.concurrent.TimeUnit;

public class RpcConnector {

    private final String serverIp;
    private final int port;
    // 模拟一个很重的资源配置
    private final byte[] heavyResource;

    private RpcConnector(String ip, int p) {
        // 1. 开始初始化基础数据
        this.serverIp = ip;

        // 2. 启动心跳检测线程 (这里埋下了巨大的隐患！)
        // 思考：当线程启动时，RpcConnector 这个对象初始化完成了吗？
//        new Thread(new HeartbeatTask(), "Heartbeat-Thread").start();

        // 模拟一些耗时的初始化操作 (比如读取配置文件、分配内存)
        try { TimeUnit.SECONDS.sleep(1); } catch (InterruptedException e) {}

        this.heavyResource = new byte[1024];
        this.port = p;

        System.out.println("【主线程】构造函数执行完毕，对象初始化完成。");
    }

    public static RpcConnector create(String ip, int p) {
        RpcConnector rpcConnector = new RpcConnector(ip, p);
        new Thread(new HeartbeatTask(rpcConnector), "Heartbeat-Thread").start();
        return rpcConnector;
    }


    // 内部类：心跳任务
    static class HeartbeatTask implements Runnable {

        private final RpcConnector rpcConnector;

        HeartbeatTask(RpcConnector rpcConnector) {
            this.rpcConnector = rpcConnector;
        }

        @Override
        public void run() {
            System.out.println("【心跳线程】开始工作...");
            // 模拟心跳检测，需要用到外部类的成员变量
            try {
                while (true) {
                    // ⚠️ 风险点：在这里读取 serverIp, port 和 heavyResource
                    // 它们可能还没被赋值吗？或者 heavyResource 可能是 null 吗？
                    System.out.println("【心跳线程】Pinging " + rpcConnector.serverIp + ":" + rpcConnector.port);

                    if (rpcConnector.heavyResource == null) {
                        System.err.println("【心跳线程】🚨 严重报警！读取到 heavyResource 为 null！");
                    } else {
                        System.out.println("【心跳线程】资源状态正常。");
                    }

                    TimeUnit.MILLISECONDS.sleep(500);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) {
        // 启动 RPC 连接器
//        new RpcConnector("192.168.1.100", 8080);
        RpcConnector.create("192.168.1.100", 8080);
    }
}
