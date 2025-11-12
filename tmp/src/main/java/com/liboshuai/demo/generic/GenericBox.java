package com.liboshuai.demo.generic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericBox {

    public static final Logger log = LoggerFactory.getLogger(GenericBox.class);

    static class Box<T> {
        private T item;

        public void setItem(T item) {
            this.item = item;
        }

        public T getItem() {
            return this.item;
        }
    }

    public static void main(String[] args) {
        // 1. 创建一个只能存放 String 的 Box
        Box<String> stringBox = new Box<>();
        stringBox.setItem("Flink");
        String stringBoxItem = stringBox.getItem();
        log.info("stringBoxItem: {}", stringBoxItem);
        log.info("---------------");

        // 2. 创建一个只能存放 Integer 的 Box
        Box<Integer> integerBox = new Box<>();
        integerBox.setItem(123);
        Integer integerBoxItem = integerBox.getItem();
        log.info("integerBoxItem: {}", integerBoxItem);
        log.info("---------------");

        // 3. 泛型不一致演示
        Box<Double> doubleBox = new Box<>();
//        doubleBox.setItem("xxx"); // 编译报错，泛型为Double，不能接收String类型
    }

}
