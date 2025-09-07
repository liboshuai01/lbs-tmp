package com.liboshuai.demo.principle.dip.bad;

/**
 * 低层模块：具体的书
 */
class Book {
    public String getContent() {
        return "从前有座山，山里有座庙...";
    }
}

/**
 * 高层模块：人
 * 这个类直接依赖了具体的 Book 类。
 */
public class Person {

    public void read() {
        // Person 类直接创建并依赖于 Book 类
        Book book = new Book();
        System.out.println("今天开始读书：");
        System.out.println(book.getContent());
    }

    public static void main(String[] args) {
        Person person = new Person();
        person.read();
        // 如果想读报纸，就必须修改 Person 类的 read 方法，或者再加一个 readNewspaper 方法。
    }
}