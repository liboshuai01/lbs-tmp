package com.liboshuai.demo.generic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicGenericsDemo {

    private static final Logger log = LoggerFactory.getLogger(BasicGenericsDemo.class);

    @FunctionalInterface
    interface Processor<T,R> {
        R process(T input);
    }

    static class StringToLengthProcessor implements Processor<String, Integer> {

        @Override
        public Integer process(String input) {
            return input == null ? 0 : input.length();
        }
    }

    static class Container<T> {
        private T item;

        public Container(T item) {
            this.item = item;
        }

        public T getItem() {
            return item;
        }

        public void setItem(T item) {
            this.item = item;
        }

        public void printItemType() {
            if (item != null) {
                log.info("  [泛型类] 容器中持有的项目类型是：{}", item.getClass().getName());
            } else {
                log.warn("  [泛型类] 容器中没有项目。");
            }
        }

        @Override
        public String toString() {
            return "Container{" +
                    "item=" + item +
                    '}';
        }
    }

    static class Utils {
        public static <E> void printElement(E element) {
            log.info("  [泛型方法] 打印: {} (实际类型: {})", element.toString(), element.getClass().getName());
        }

        public static <T> T identity(T item) {
            // 这里可以做一些通用的处理...
            return item;
        }
    }

    public static void main(String[] args) {
        log.info("--- 1. 泛型接口 (Generic Interface) 演示 ---");
        // 创建一个实现
        StringToLengthProcessor stringToLengthProcessor = new StringToLengthProcessor();
        Integer length = stringToLengthProcessor.process("hello world");
        log.info("处理结果: {}", length);

        // 我们也可以使用Lambda表达式
        Processor<Integer, String> intToStringProcessor = i -> "数字-" + i;
        String result = intToStringProcessor.process(1);
        log.info("处理结果: {}", result);

        log.info("--- 2. 泛型类 (Generic Class) 演示 ---");
        // 创建一个持有 String 的容器
        Container<String> stringContainer = new Container<>("这是一段文本");
        stringContainer.printItemType();
        String item1 = stringContainer.getItem();
        log.info("取出的项目: {}", item1);
        // 创建一个持有 Integer 的容器
        Container<Integer> integerContainer = new Container<>(12345);
        integerContainer.printItemType();
        Integer item2 = integerContainer.getItem();
        log.info("取出的项目: {}", item2);

        log.info("--- 3. 泛型方法 (Generic Method) 演示 ---");
        log.info("调用泛型方法 Utils.printElement:");
        Utils.printElement("Hello World");
        Utils.printElement(12345);
        Utils.printElement(3.14);
        Utils.printElement(new Container<>("泛型类实例"));
        // 调用泛型方法 Utils.identity
        String identityStr = Utils.identity("abc");
        Integer identityInt = Utils.identity(999);
        log.info("  [泛型方法] identityStr: {}", identityStr);
        log.info("  [泛型方法] identityInt: {}", identityInt);
    }
}
