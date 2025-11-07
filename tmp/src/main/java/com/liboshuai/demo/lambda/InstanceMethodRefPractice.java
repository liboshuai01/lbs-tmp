package com.liboshuai.demo.lambda;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.Comparator;

/**
 * 练习 1.3.2：任意对象的实例方法引用 (ClassName::instanceMethod)
 *
 * 这种形式是最绕的，我们通过两个变体来理解。
 *
 * 变体 A (Function 形式)：Lambda 只有一个参数，且该参数是方法调用者。
 * (s) -> s.instanceMethod()  =>  ClassName::instanceMethod
 *
 * 变体 B (BiFunction/Comparator 形式)：Lambda 有多个参数，第一个参数是调用者，其余是入参。
 * (s1, s2) -> s1.instanceMethod(s2)  =>  ClassName::instanceMethod
 */
public class InstanceMethodRefPractice {

    public static void main(String[] args) {
        System.out.println("### 变体 A: (s) -> s.instanceMethod() ###");
        practiceVariantA();

        System.out.println("\n---\n");

        System.out.println("### 变体 B: (s1, s2) -> s1.instanceMethod(s2) ###");
        practiceVariantB();
    }

    /**
     * 变体 A: (s) -> s.instanceMethod() 变为 ClassName::instanceMethod
     * 对应契约：Function<T, R>
     */
    private static void practiceVariantA() {
        List<String> list = Arrays.asList("Java", "Flink", "Spring");

        // 目标：将 List<String> 转换为 List<Integer> (每个字符串的长度)

        // 1. 匿名内部类 (Java 7)
        Function<String, Integer> anonymousClass = new Function<String, Integer>() {
            @Override
            public Integer apply(String s) {
                // 's' 是传入的参数，我们调用它的 .length() 方法
                return s.length();
            }
        };
        System.out.println("1. 匿名内部类: " +
                list.stream().map(anonymousClass).toList());

        // 2. Lambda 表达式
        // 's' 是 stream 中的每个元素
        Function<String, Integer> lambda = s -> s.length();
        System.out.println("2. Lambda: " +
                list.stream().map(lambda).toList());

        // 3. 方法引用 (ClassName::instanceMethod)
        // 关键对比：
        // Lambda:    s -> s.length()
        // 引用:      String::length

        // 编译器会理解：
        // - 这是一个 Function<String, Integer> (map 操作需要)
        // - `apply(String s)` 是它的抽象方法。
        // - 看到 `String::length`，它知道 `length()` 是 String 的一个实例方法。
        // - 它会将传入的 's' 自动作为调用 `length()` 的那个实例。
        // - 即 `String::length` 等价于 `s -> s.length()`

        System.out.println("3. 方法引用: " +
                list.stream().map(String::length).toList());
    }


    /**
     * 变体 B: (s1, s2) -> s1.instanceMethod(s2) 变为 ClassName::instanceMethod
     * 对应契约：Comparator<T> (它继承了 BiFunction)
     */
    private static void practiceVariantB() {
        List<String> list = Arrays.asList("Spring", "Java", "Flink", "Akka");

        // 目标：对列表进行不区分大小写的排序

        // 1. 匿名内部类 (Java 7)
        Comparator<String> anonymousClass = new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                // 's1' 是第一个参数，'s2' 是第二个参数
                // s1 作为调用者，s2 作为入参
                return s1.compareToIgnoreCase(s2);
            }
        };
        // list.sort(anonymousClass); // 原地排序
        System.out.println("1. 匿名内部类 (排序前): " + list);
        Collections.sort(list, anonymousClass); // 使用 Collections.sort 演示
        System.out.println("1. 匿名内部类 (排序后): " + list);

        // 重置列表
        list = Arrays.asList("Spring", "Java", "Flink", "Akka");

        // 2. Lambda 表达式
        Comparator<String> lambda = (s1, s2) -> s1.compareToIgnoreCase(s2);
        Collections.sort(list, lambda);
        System.out.println("2. Lambda (排序后): " + list);

        // 重置列表
        list = Arrays.asList("Spring", "Java", "Flink", "Akka");

        // 3. 方法引用 (ClassName::instanceMethod)
        // 关键对比：
        // Lambda:    (s1, s2) -> s1.compareToIgnoreCase(s2)
        // 引用:      String::compareToIgnoreCase

        // 编译器会理解：
        // - 这是一个 Comparator<String> (sort 方法需要)
        // - `compare(String s1, String s2)` 是它的抽象方法。
        // - 看到 `String::compareToIgnoreCase`，它知道这是 String 的一个实例方法。
        // - 它会将 Lambda 的*第一个参数* 's1' 作为调用者。
        // - 它会将 Lambda 的*第二个参数* 's2' 作为 `compareToIgnoreCase` 的入参。

        Collections.sort(list, String::compareToIgnoreCase);
        System.out.println("3. 方法引用 (排序后): " + list);
    }
}
