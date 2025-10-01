package com.liboshuai.demo.principle.lsp.bad;

/**
 * 基类：鸟
 */
class Bird {
    public void fly() {
        System.out.println("鸟在天空中飞翔...");
    }
}

/**
 * 子类：燕子
 * 燕子继承鸟，并且可以正常飞行，它没有改变父类的行为。
 */
class Swallow extends Bird {
    // 这里可以重写，也可以不重写，行为是一致的
}

/**
 * 子类：鸵鸟
 * 鸵鸟继承鸟，但它重写了 fly 方法，改变了父类原有的行为，使其变得不可用。
 * 这就违反了里氏代换原则。
 */
class Ostrich extends Bird {
    @Override
    public void fly() {
        // 问题所在：鸵鸟不会飞，重写方法使其抛出异常，这与父类的预期行为完全不同。
        throw new UnsupportedOperationException("鸵鸟不会飞！");
    }
}


public class AnimalShow {

    // 这个方法的参数是基类 Bird，它期望任何传入的 Bird 对象都能调用 fly()
    public void startShow(Bird bird) {
        System.out.println("表演开始...");
        try {
            bird.fly();
        } catch (Exception e) {
            System.err.println("出错了: " + e.getMessage());
        }
        System.out.println("表演结束。");
        System.out.println("--------------------");
    }

    public static void main(String[] args) {
        AnimalShow show = new AnimalShow();

        Bird swallow = new Swallow();
        Bird ostrich = new Ostrich();

        System.out.println("让燕子表演:");
        show.startShow(swallow); // 程序正常运行

        System.out.println("让鸵鸟表演:");
        show.startShow(ostrich); // 程序虽然被try-catch捕获了，但本质上已经出错了
        // 如果没有 try-catch，程序会直接崩溃
    }
}