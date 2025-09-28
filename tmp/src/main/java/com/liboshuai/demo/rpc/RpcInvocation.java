package com.liboshuai.demo.rpc;

/**
 * RPC调用消息的接口。该接口允许请求所有必要的信息以查找方法并使用相应的参数调用。
 */
public interface RpcInvocation extends Message {

    String getMethodName();

    Class<?>[] getParameterTypes();

    Object[] getArgs();
}
