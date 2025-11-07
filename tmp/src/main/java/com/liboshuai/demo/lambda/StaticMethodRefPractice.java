package com.liboshuai.demo.lambda;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 练习 1.3.1：静态方法引用 (ClassName::staticMethod)
 * 教学点：当 Lambda 表达式的方法体 *仅仅* 是在调用一个 *已知的静态方法* 时，
 * 并且该静态方法的参数列表与 Lambda 的参数列表一致时，
 * 就可以使用这种语法糖。
 */
public class StaticMethodRefPractice {

    /**
     * 这是一个我们自定义的、简单的静态方法，用于示例。
     * 它接收一个 String，返回一个 Integer。
     */
    public static Integer convertStringToInt(String s) {
        System.out.println("-> 正在调用自定义的 convertStringToInt: " + s);
        return Integer.parseInt(s);
    }

    public static void main(String[] args) {
        List<String> stringList = Arrays.asList("10", "20", "30", "40");

        // 目标：将 List<String> 转换为 List<Integer>

        // ----------------------------------------------------------------
        // 方式一：使用完整的“匿名内部类” (Java 7 的写法)
        // ----------------------------------------------------------------
        // 这里的 `new Function<...>() { ... }` 就是 1.1 节定义的“契约”的实现
        Function<String, Integer> anonymousClass = new Function<String, Integer>() {
            @Override
            public Integer apply(String s) {
                // 这里的实现是调用一个静态方法
                return Integer.parseInt(s);
            }
        };
        List<Integer> result1 = stringList.stream()
                .map(anonymousClass)
                .toList();
        System.out.println("1. 匿名内部类结果: " + result1);
        System.out.println("---");


        // ----------------------------------------------------------------
        // 方式二：使用“Lambda 表达式” (1.2 节的核心语法)
        // ----------------------------------------------------------------
        // 这是对上面匿名内部类的简化
        Function<String, Integer> lambdaExpression = s -> Integer.parseInt(s);

        List<Integer> result2 = stringList.stream()
                .map(lambdaExpression) // 传入 Lambda 表达式
                .toList();
        System.out.println("2. Lambda 表达式结果: " + result2);
        System.out.println("---");


        // ----------------------------------------------------------------
        // 方式三：使用“静态方法引用” (1.3 节的语法糖)
        // ----------------------------------------------------------------

        // 关键对比：
        // Lambda:    s -> Integer.parseInt(s)
        // 引用:      Integer::parseInt

        // 因为 Lambda (s -> Integer.parseInt(s)) 的方法体
        // 只是在调用 `Integer` 类的 `parseInt` 静态方法，
        // 并且参数 `s` 被原样传递了过去，
        // 所以我们可以将其简写为方法引用。

        List<Integer> result3 = stringList.stream()
                .map(Integer::parseInt) // 传入静态方法引用
                .toList();
        System.out.println("3. 静态方法引用 (Integer::parseInt) 结果: " + result3);
        System.out.println("---");


        // ----------------------------------------------------------------
        // 方式四：使用我们 *自定义* 的静态方法引用
        // ----------------------------------------------------------------

        // 关键对比：
        // Lambda:    s -> StaticMethodRefPractice.convertStringToInt(s)
        // 引用:      StaticMethodRefPractice::convertStringToInt

        List<Integer> result4 = stringList.stream()
                .map(StaticMethodRefPractice::convertStringToInt)
                .toList();
        System.out.println("4. 静态方法引用 (自定义方法) 结果: " + result4);
    }
}
