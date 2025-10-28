package com.liboshuai.flink.streaming.api.datastream;

public class DataStreamSource<T> extends SingleOutputStreamOperator<T> {
    public <R> SingleOutputStreamOperator<R> map(MapFunction<T, R> mapper) {
        return null;
    }
}
