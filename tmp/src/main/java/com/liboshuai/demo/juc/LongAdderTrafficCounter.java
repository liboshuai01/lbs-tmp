package com.liboshuai.demo.juc;

import java.util.concurrent.atomic.LongAdder;

public class LongAdderTrafficCounter {

    // TODO: 定义 LongAdder
    private final LongAdder adder = new LongAdder();

    // 增加计数
    public void increment() {
        // TODO: 如何自增？
        adder.increment();
    }

    // 获取当前值
    public long get() {
        // TODO: 如何获取总和？
        return adder.longValue();
    }

    // 思考题：
    // 既然 LongAdder 写入性能这么好，为什么 Flink 或 Java 不把所有的 AtomicLong 都换成 LongAdder？
    // 它有什么缺点吗？(提示：实时性/内存占用)
    /*
    回答:
        缺点1: 实时性不如AtomicLong, LongAdder虽然可以保证最终一致性, 但是并发修改场景下, 获取到值并不是瞬时的精准快照值.
        缺点2: LongAdder因为内部存储将变量切分为了一个Cell数组, 它的内存占用要比AtomicLong更大.
     */
}