package com.liboshuai.demo.generic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class MiniFlinkGenericsDemo {

    private static final Logger log = LoggerFactory.getLogger(MiniFlinkGenericsDemo.class);


    @FunctionalInterface
    private interface MapFunction<IN, OUT> {
        OUT map(IN value) throws Exception;
    }

    @FunctionalInterface
    private interface SinkFunction<IN> {
        void invoke(IN value) throws Exception;
    }

    private static class TypeInformation<T> {
        private final Class<T> typeClass;

        public TypeInformation(Class<T> typeClass) {
            this.typeClass = typeClass;
        }

        public Class<T> getTypeClass() {
            return typeClass;
        }

        public static final TypeInformation<String> STRING = new TypeInformation<>(String.class);
        public static final TypeInformation<Integer> INT = new TypeInformation<>(Integer.class);
        public static final TypeInformation<Event> EVENT = new TypeInformation<>(Event.class);

        public static <T> TypeInformation<T> of(Class<T> clazz) {
            return new TypeInformation<>(clazz);
        }

        @Override
        public String toString() {
            return "TypeInfo<" + typeClass.getSimpleName() + ">";
        }
    }

    private static class DataStream<T> {
        private final TypeInformation<T> typeInfo;

        public DataStream(TypeInformation<T> typeInfo) {
            this.typeInfo = typeInfo;
            log.info("[创建流] {}", this);
        }

        public TypeInformation<T> getTypeInfo() {
            return typeInfo;
        }

        public <R> PendingOperation<T, R> map(MapFunction<T, R> mapFunction) {
            log.info("[调用map] 准备转换 {} -> ?", this.typeInfo);
            return new PendingOperation<>(this, mapFunction);
        }

        public void addSink(SinkFunction<? super T> sinkFunction) {
            log.info("[调用sink] " + this.typeInfo + " 将被 "
                    + sinkFunction.getClass().getSimpleName() + " 消费");
        }

        public DataStream<T> union(DataStream<? extends T>... others) {
            log.info("[调用union] " + this.typeInfo + " 准备合并...");
            for (DataStream<? extends T> other : others) {
                log.info("  -> 合并 " + other.typeInfo);
            }
            // 返回一个合并后的新流 (这里简化为返回自己)
            return this;
        }

        @Override
        public String toString() {
            return "DataStream<" + (typeInfo != null ? typeInfo.typeClass.getSimpleName() : "?") + ">";
        }

    }

    private static class PendingOperation<IN, OUT> {
        private final DataStream<IN> inputStream;
        private final MapFunction<IN, OUT> mapFunction;

        public PendingOperation(DataStream<IN> inputStream, MapFunction<IN, OUT> mapFunction) {
            this.inputStream = inputStream;
            this.mapFunction = mapFunction;
        }

        public DataStream<OUT> attemptAutoType() {
            log.info("  [类型推断] 尝试从 {} 自动推断输出类型...", mapFunction.getClass());
            TypeInformation<OUT> outType = TypeExtractor.getMapOutType(mapFunction);
            if (outType == null) {
                log.warn("  [类型推断] 失败！无法从 Lambda 或泛型类推断。");
                throw new IllegalStateException("自动类型推断失败！请使用 .returns() 显式指定类型。");
            }
            log.info("  [类型推断] 成功！推断输出类型为: {}", outType);
            return new DataStream<>(outType);
        }

        public DataStream<OUT> returns(TypeInformation<OUT> outType) {
            log.info("  [类型指定] 用户通过 .returns() 显式指定输出类型: {}", outType);
            return new DataStream<>(outType);
        }
    }


    private static class TypeExtractor {

        @SuppressWarnings("unchecked")
        public static <OUT> TypeInformation<OUT> getMapOutType(MapFunction<?,?> mapFunction) {
            if (mapFunction.getClass().isSynthetic()) {
                return null;
            }

            try {
                // 遍历该类实现的所有接口
                for (Type genericInterface : mapFunction.getClass().getGenericInterfaces()) {
                    if (genericInterface instanceof ParameterizedType) {
                        ParameterizedType pType = (ParameterizedType) genericInterface;
                        // 找到 MapFunction 接口
                        if (pType.getRawType().equals(MapFunction.class)) {
                            // 获取泛型参数列表 <IN, OUT>
                            Type[] typeArgs = pType.getActualTypeArguments();
                            if (typeArgs.length == 2) {
                                // 第 1 个(索引0)是 IN, 第 2 个(索引1)是 OUT
                                Class<OUT> outClass = (Class<OUT>) typeArgs[1];
                                return TypeInformation.of(outClass);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // Flink 中有复杂的 CGLIB、TypeVariable 等处理，这里简化
                return null;
            }
            return null;
        }
    }


    private static class Event {
        public int id;
        public long timestamp;
        public Event(int id) {
            this.id = id;
            this.timestamp = System.currentTimeMillis();
        }
    }

    private static class Alert extends Event {
        public String msg;
        public Alert(int id, String msg) {
            super(id);
            this.msg = msg;
        }
    }

    private static class EventSink implements SinkFunction<Event> {

        @Override
        public void invoke(Event value) throws Exception {

        }
    }

    private static class ObjectSink implements SinkFunction<Event> {

        @Override
        public void invoke(Event value) throws Exception {

        }
    }

    public static void main(String[] args) {
        DataStream<Event> eventDataStream = new DataStream<>(TypeInformation.EVENT);
        log.info("--- 演示1: map (使用匿名内部类，自动推断类型) ---");
        DataStream<String> stringDataStream = eventDataStream.map(new MapFunction<Event, String>() {
            @Override
            public String map(Event value) throws Exception {
                return "EventID:" + value.id;
            }
        }).attemptAutoType();
        log.info("--> 成功创建新流: " + stringDataStream);

        log.info("--- 演示2: map (使用Lambda，自动推断失败，必须 .returns()) ---");
        // Lambda 表达式的类型擦除更彻底，Flink 无法推断
        DataStream<Integer> intStream;
        try {
            intStream = eventDataStream.map(event -> event.id).attemptAutoType();
        } catch (IllegalStateException e) {
            System.err.println("错误捕获: " + e.getMessage());

            // 这就是为什么 Flink 强烈建议对 Lambda 使用 .returns()
            log.info("   [补救] 使用 .returns() 显式指定类型...");
            intStream = eventDataStream.map(event -> event.id)
                    .returns(TypeInformation.INT); // 手动指定返回类型
            log.info("--> 成功创建新流: " + intStream);
        }

        log.info("--- 演示3: addSink (演示 ? super T 消费者) ---");
        DataStream<Alert> alertDataStream = new DataStream<>(TypeInformation.of(Alert.class));
        EventSink eventSink = new EventSink();
        ObjectSink objectSink = new ObjectSink();
        alertDataStream.addSink(eventSink);
        alertDataStream.addSink(objectSink);
        log.info("--> addSink 演示完毕，编译通过，符合PECS原则。");

        log.info("--- 演示4: union (演示 ? extends T 生产者) ---");
        DataStream<Event> unionEventDataStream = eventDataStream.union(alertDataStream);
        log.info("--> union 演示完毕，编译通过，符合PECS原则。");
    }
}
