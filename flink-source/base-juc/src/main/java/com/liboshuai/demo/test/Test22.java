package com.liboshuai.demo.test;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class Test22 {

    static class MyAtomicInteger {
        // 1. 获取 Unsafe 实例
        private static final Unsafe unsafe;

        // 2. 存储 value 字段在内存中的偏移量
        private static final long valueOffset;

        // 3. 使用 volatile 保证多线程环境下的可见性
        private volatile int value;

        // 静态代码块，用于初始化 unsafe 实例和 valueOffset
        static {
            try {
                // Unsafe 的构造函数是私有的，需要通过反射获取单例的 theUnsafe 字段
                Field field = Unsafe.class.getDeclaredField("theUnsafe");
                field.setAccessible(true);
                unsafe = (Unsafe) field.get(null);
                // 获取 value 字段的内存偏移量
                valueOffset = unsafe.objectFieldOffset(MyAtomicInteger.class.getDeclaredField("value"));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * 构造函数
         */
        public MyAtomicInteger(int initialValue) {
            this.value = initialValue;
        }

        /**
         * 获取当前值
         */
        public int get() {
            return value;
        }

        /**
         * 以原子方式将当前值加1，并返回旧值
         */
        public int getAndIncrement() {
            // 使用无限循环（自旋）来保证操作成功
            for (;;) {
                // 获取当前值
                int current = get();
                // 计算期望的下一个值
                int next = current + 1;
                // 使用 CAS 操作尝试更新
                // 如果当前值 current 仍然等于内存中的值（valueOffset 处的值），则更新为 next
                // 如果操作成功，compareAndSwapInt 返回 true，循环结束
                if (compareAndSet(current, next)) {
                    // 返回更新前的旧值
                    return current;
                }
                // 如果 CAS 失败，说明在 "获取-计算-更新" 期间，value 被其他线程修改了
                // 循环会继续，重新获取最新的值，然后再次尝试 CAS 操作
            }
        }

        /**
         * CAS 核心操作：如果当前值等于期望值 expect，则以原子方式将该值设置为 update
         */
        public final boolean compareAndSet(int expect, int update) {
            // 调用 Unsafe 的 CAS 方法
            // 参数：操作的对象，字段的内存偏移量，期望的值，要更新成的新值
            return unsafe.compareAndSwapInt(this, valueOffset, expect, update);
        }

    }
}
