package com.liboshuai.demo.rpc;

public interface RpcService {
    /**
     * 获取远程rpc请求代理对象
     */
    <C extends RpcGateway> C connect(String address, Class<C> clazz);

    /**
     * 获取本地rpc请求代理对象
     */
    <T extends RpcEndpoint & RpcGateway> RpcServer startServer(T endpoint);

    /**
     * 获取actor地址
     */
    String getAddress(String endpointId);
}
