package com.liboshuai.demo.rpc;

public interface RpcInvocation extends Message {
    String getMethodName();
    Class<?>[] getParameterTypes();
    Object[] getArgs();
}
