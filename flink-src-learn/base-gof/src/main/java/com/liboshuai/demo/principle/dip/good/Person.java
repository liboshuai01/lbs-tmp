package com.liboshuai.demo.principle.dip.good;

/**
 * 步骤 1: 创建一个抽象接口，代表“可读物”
 */
interface Readable {
    String getContent();
}

/**
 * 步骤 2: 低层模块（细节）实现这个抽象接口
 */
class Book implements Readable {
    @Override
    public String getContent() {
        return "从前有座山，山里有座庙...";
    }
}

class Newspaper implements Readable {
    @Override
    public String getContent() {
        return "今天发生了件大事...";
    }
}

class Ebook implements Readable {
    @Override
    public String getContent() {
        return "【听书】很久很久以前...";
    }
}


/**
 * 步骤 3: 高层模块 Person 依赖于 Readable 接口
 */
public class Person {
    private Readable readable;

    // 步骤 4: 通过构造函数“注入”依赖的对象
    public Person(Readable readable) {
        this.readable = readable;
    }

    public void read() {
        System.out.println("今天开始阅读：");
        System.out.println(this.readable.getContent());
    }

    public static void main(String[] args) {
        // Person 想读书，就给他注入一本书
        Readable book = new Book();
        Person person1 = new Person(book);
        person1.read();

        System.out.println("--------------------");

        // Person 想读报纸，就给他注入一份报纸。Person 类本身无需任何改动！
        Readable newspaper = new Newspaper();
        Person person2 = new Person(newspaper);
        person2.read();

        System.out.println("--------------------");

        // Person 想听电子书，就给他注入一本电子书。
        Readable ebook = new Ebook();
        Person person3 = new Person(ebook);
        person3.read();
    }
}