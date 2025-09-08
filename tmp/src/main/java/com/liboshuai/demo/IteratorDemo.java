package com.liboshuai.demo;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class IteratorDemo {
    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        list.add("王多余");
        list.add("卧龙");
        list.add("凤雏");

        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            log.info("next: {}", next);
        }
    }
}
