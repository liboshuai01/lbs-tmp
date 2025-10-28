package com.liboshuai.flink.api.common.functions;

public interface MapFunction<T, O> extends Function {
    O map(T value) throws Exception;
}
