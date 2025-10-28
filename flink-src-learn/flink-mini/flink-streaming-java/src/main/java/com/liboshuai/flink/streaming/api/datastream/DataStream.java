package com.liboshuai.flink.streaming.api.datastream;

import com.liboshuai.flink.api.common.functions.FlatMapFunction;
import com.liboshuai.flink.api.common.functions.MapFunction;
import com.liboshuai.flink.api.java.tuple.Tuple2;

public class DataStream<T> {

    public <R> SingleOutputStreamOperator<R> map(MapFunction<T, R> mapper) {
        return null;
    }

    public <R> SingleOutputStreamOperator<Tuple2<T, R>> flatMap(FlatMapFunction<T,R> flatMapper) {
        return null;
    }
}
