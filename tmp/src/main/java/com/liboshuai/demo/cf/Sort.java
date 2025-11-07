package com.liboshuai.demo.cf;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Sort {
    public static void main(String[] args) {
        test1();
        test2();
        test3();
    }

    private static void test1() {
        List<String> immutableList = List.of("one", "two", "three");
        List<String> list = new ArrayList<>(immutableList);

        list.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });

        System.out.println("list1: " + list);
    }

    private static void test2() {
        List<String> immutableList = List.of("one", "two", "three");
        List<String> list = new ArrayList<>(immutableList);

        list.sort((o1, o2) -> o1.compareTo(o2));

        System.out.println("list2: " + list);
    }

    private static void test3() {
        List<String> immutableList = List.of("one", "two", "three");
        List<String> list = new ArrayList<>(immutableList);

        list.sort(String::compareTo);

        System.out.println("list3: " + list);
    }

}
