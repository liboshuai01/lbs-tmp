package com.liboshuai.flink.demo;

import com.liboshuai.flink.streaming.api.datastream.DataStreamSource;
import com.liboshuai.flink.streaming.api.datastream.SingleOutputStreamOperator;
import com.liboshuai.flink.streaming.api.environment.StreamExecutionEnvironment;

public class WordCount {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<String> dataStream = env.socketTextStream("localhost", 6666);
        SingleOutputStreamOperator<String> mapDataStream = dataStream.map(String::toLowerCase);
        SingleOutputStreamOperator<Tuple2<String, Integer>> flatMapDataStream = mapDataStream.flatMap(new Splitter());
        KeyedStream<Tuple2<String, Integer>, String> keyedDataStream = flatMapDataStream.keyBy(value -> value.f0);
        SingleOutputStreamOperator<Tuple2<String, Integer>> sumDataStream = keyedDataStream.sum(1);
        DataStreamSink<Tuple2<String, Integer>> dataStreamSink = sumDataStream.print();

        env.execute("word count demo");
    }


    public static class Splitter implements FlatMapFunction<String, Tuple2<String, Integer>> {
        @Override
        public void flatMap(
                String sentence,
                Collector<Tuple2<String, Integer>> out) throws Exception {
            for (String word : sentence.split(" ")) {
                out.collect(new Tuple2<>(word, 1));
            }
        }
    }
}
