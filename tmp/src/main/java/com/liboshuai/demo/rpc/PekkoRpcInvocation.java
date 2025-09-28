package com.liboshuai.demo.rpc;

import lombok.Data;

/**
 * 远程/本地RPC调用消息，当actor通信远程/本地时使用。
 */
@Data
public class PekkoRpcInvocation implements RpcInvocation {
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] args;
}
