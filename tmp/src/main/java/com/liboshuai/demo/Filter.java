package com.liboshuai.demo;

public class Filter {

    private static boolean checkString(String s, MyPredicate<String> myPredicate) {
        return myPredicate.test(s);
    }

    public static void main(String[] args) {
        MyPredicate<String> myPredicate = (s) -> s.length() > 5;
        boolean result = checkString("我是李博帅！", myPredicate);
        System.out.println("result: " + result);
    }

    @FunctionalInterface
    interface MyPredicate<T> {
        boolean test(T value);

        default void justChecking() {
            System.out.println("这是一个默认方法");
        }
    }

}
