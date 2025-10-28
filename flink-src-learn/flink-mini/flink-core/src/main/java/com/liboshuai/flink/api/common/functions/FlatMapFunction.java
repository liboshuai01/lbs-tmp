package com.liboshuai.flink.api.common.functions;


import com.liboshuai.flink.util.Collector;

public interface FlatMapFunction<T, O> extends Function{
    void flatMap(T value, Collector<O> out) throws Exception;
}
