package com.liboshuai.demo.factory.simple;

import java.util.Objects;

public class CoffeeSimpleFactory {
    public Coffee createCoffee(String type) {
        Coffee coffee;
        if (Objects.equals(type, "latte")) {
            coffee = new LatteCoffee();
        } else if (Objects.equals(type, "american")) {
            coffee = new AmericanCoffee();
        } else {
            throw new IllegalArgumentException("没有[" + type + "]类型的coffee");
        }
        return coffee;
    }
}
