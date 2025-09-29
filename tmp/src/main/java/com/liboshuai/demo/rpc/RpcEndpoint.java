package com.liboshuai.demo.rpc;

import lombok.Getter;

/**
 * 提供rpc方法实现的共同抽象父类
 * 赋予子类rpc方法实现的功能
 */
public abstract class RpcEndpoint implements RpcGateway {
    @Getter
    private final RpcService rpcService;
    @Getter
    private final String endpointId;
    @Getter
    private final RpcServer rpcServer;

    protected RpcEndpoint(RpcService rpcService, String endpointId) {
        this.rpcService = rpcService;
        this.endpointId = endpointId;

        // 获取自身本地rpc请求代理对象，为了使用同步的方式编写异步代码
        this.rpcServer = rpcService.startServer(this);
    }
}
