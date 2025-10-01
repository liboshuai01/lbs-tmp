package com.liboshuai.demo.principle.lsp.good;

/**
 * 更通用的基类，只包含所有动物共有的行为。
 */
class Animal {
    public void eat() {
        System.out.println("动物在吃东西...");
    }
}

/**
 * 步骤 1: 将“飞行”行为抽象成接口
 */
interface Flyable {
    void fly();
}

/**
 * 步骤 2: 会飞的鸟 - 燕子
 * 它继承了 Animal，并实现了 Flyable 接口。
 */
class Swallow extends Animal implements Flyable {
    @Override
    public void fly() {
        System.out.println("燕子在天空中快速飞翔...");
    }
}

/**
 * 步骤 3: 不会飞的鸟 - 鸵鸟
 * 它只继承 Animal，不实现 Flyable 接口。
 */
class Ostrich extends Animal {
    public void run() {
        System.out.println("鸵鸟在地上飞快地奔跑...");
    }
}


public class AnimalShow {

    // 步骤 4: 这个方法现在明确地要求传入一个“会飞的”动物
    // 它的参数类型是 Flyable 接口，而不是宽泛的 Animal 或 Bird
    public void startFlyingShow(Flyable flyer) {
        System.out.println("飞行表演开始...");
        flyer.fly();
        System.out.println("飞行表演结束。");
        System.out.println("--------------------");
    }

    public void startRunningShow(Ostrich ostrich) {
        System.out.println("奔跑表演开始...");
        ostrich.run();
        System.out.println("奔跑表演结束。");
        System.out.println("--------------------");
    }

    public static void main(String[] args) {
        AnimalShow show = new AnimalShow();

        Swallow swallow = new Swallow();
        Ostrich ostrich = new Ostrich();

        // 我们可以安全地让燕子去表演飞行
        show.startFlyingShow(swallow);

        // 关键点：我们根本无法将鸵鸟对象传给 startFlyingShow 方法
        // show.startFlyingShow(ostrich); // 这行代码在编译时就会报错！

        // 我们可以为鸵鸟安排一个适合它的表演
        show.startRunningShow(ostrich);
    }
}