package com.liboshuai.flink.api.common.functions;

import java.util.stream.Collector;

public interface FlatMapFunction<T, O> extends Function{
    void flatMap(T value, Collector<O> out) throws Exception;
}
