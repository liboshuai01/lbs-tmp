package com.liboshuai.demo.rpc;

import lombok.Getter;

public abstract class RpcEndpoint {

    @Getter
    private final RpcService rpcService;
    @Getter
    private final String endpointId;

    public RpcEndpoint(RpcService rpcService, String endpointId) {
        this.rpcService = rpcService;
        this.endpointId = endpointId;
    }
}
