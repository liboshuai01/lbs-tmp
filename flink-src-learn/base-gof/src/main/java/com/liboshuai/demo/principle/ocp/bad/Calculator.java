package com.liboshuai.demo.principle.ocp.bad;

/**
 * 违背开闭原则的设计
 * 当需要添加新的运算功能（如乘法）时，必须修改 calculate 方法的源代码。
 */
public class Calculator {

    public int calculate(int numberA, int numberB, String operation) {
        if ("add".equalsIgnoreCase(operation)) {
            return numberA + numberB;
        } else if ("subtract".equalsIgnoreCase(operation)) {
            return numberA - numberB;
        }
        // 如果要增加乘法功能，就需要在这里添加代码：
        // else if ("multiply".equalsIgnoreCase(operation)) {
        //     return numberA * numberB;
        // }
        // 这就修改了已有的类，违反了开闭原则。
        return 0; // 或者抛出异常
    }

    public static void main(String[] args) {
        Calculator calculator = new Calculator();
        int sum = calculator.calculate(10, 5, "add");
        int difference = calculator.calculate(10, 5, "subtract");

        System.out.println("10 + 5 = " + sum);         // 输出: 10 + 5 = 15
        System.out.println("10 - 5 = " + difference);  // 输出: 10 - 5 = 5
    }
}