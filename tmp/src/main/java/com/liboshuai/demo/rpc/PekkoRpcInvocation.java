package com.liboshuai.demo.rpc;

public class PekkoRpcInvocation implements RpcInvocation {

    private final String methodName;
    private final Class<?>[] parameterTypes;
    private final Object[] args;

    public PekkoRpcInvocation(String methodName, Class<?>[] parameterTypes, Object[] args) {
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.args = args;
    }

    @Override
    public String getMethodName() {
        return methodName;
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    @Override
    public Object[] getArgs() {
        return args;
    }
}
