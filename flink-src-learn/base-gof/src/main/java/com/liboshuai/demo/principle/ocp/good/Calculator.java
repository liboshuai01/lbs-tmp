package com.liboshuai.demo.principle.ocp.good;

// 步骤 1: 创建一个抽象的运算接口
interface Operation {
    int perform(int numberA, int numberB);
}

// 步骤 2: 为每种运算创建具体的实现类
class Addition implements Operation {
    @Override
    public int perform(int numberA, int numberB) {
        return numberA + numberB;
    }
}

class Subtraction implements Operation {
    @Override
    public int perform(int numberA, int numberB) {
        return numberA - numberB;
    }
}

// 步骤 3: 【对扩展开放】当需要新功能时，创建一个新的实现类
class Multiplication implements Operation {
    @Override
    public int perform(int numberA, int numberB) {
        return numberA * numberB;
    }
}


/**
 * 遵循开闭原则的设计
 * 这个 Calculator 类在写完之后，无论未来增加多少种运算，都不再需要任何修改。
 */
public class Calculator {

    public int calculate(int numberA, int numberB, Operation operation) {
        return operation.perform(numberA, numberB);
    }

    public static void main(String[] args) {
        Calculator calculator = new Calculator();

        // 使用加法
        Operation addition = new Addition();
        int sum = calculator.calculate(10, 5, addition);
        System.out.println("10 + 5 = " + sum); // 输出: 10 + 5 = 15

        // 使用减法
        Operation subtraction = new Subtraction();
        int difference = calculator.calculate(10, 5, subtraction);
        System.out.println("10 - 5 = " + difference); // 输出: 10 - 5 = 5

        // 【轻松扩展】现在我们使用新增的乘法功能，Calculator 类完全不用动
        Operation multiplication = new Multiplication();
        int product = calculator.calculate(10, 5, multiplication);
        System.out.println("10 * 5 = " + product); // 输出: 10 * 5 = 50
    }
}