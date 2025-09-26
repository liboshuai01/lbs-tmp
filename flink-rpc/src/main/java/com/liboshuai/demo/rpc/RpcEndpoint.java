package com.liboshuai.demo.rpc;

import lombok.Getter;

import java.util.concurrent.*;

public class RpcEndpoint implements RpcGateway {

    public RpcService rpcService;
    @Getter
    private final String endpointId;
    RpcServer rpcServer;
    @Getter
    private final MainThreadExecutor mainThreadExecutor;

    protected RpcEndpoint(RpcService rpcService, String endpointId) {
        this.endpointId = endpointId;
        this.rpcService = rpcService;

        // 做各类rpc组件准备工作
        rpcServer = rpcService.startServer(this);

        mainThreadExecutor = new MainThreadExecutor(rpcServer);

    }


    // endpoint内，用于提交各类异步任务的工具
    public static class MainThreadExecutor implements Executor {

        MainThreadExecutable rpcServer;
        ScheduledExecutorService mainScheduledExecutor;

        public MainThreadExecutor(MainThreadExecutable rpcServer) {
            this.rpcServer = rpcServer;
            this.mainScheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        }


        @Override
        public void execute(Runnable runnable) {

            mainScheduledExecutor.execute(() -> rpcServer.runAsync(runnable));

            //rpcServer.runAsync(runnable);

        }


        public void schedule(Runnable command, long delay, TimeUnit unit) {

            mainScheduledExecutor.schedule(() -> {
                rpcServer.runAsync(command);
            }, delay, unit);

        }

    }

}
