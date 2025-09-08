package com.liboshuai.demo.factory.before;

import java.util.Objects;

public class CoffeeStore {
    public String product(String type) {
        Coffee coffee;
        if (Objects.equals(type, "latte")) {
            coffee = new LatteCoffee();
        } else if (Objects.equals(type, "american")) {
            coffee = new AmericanCoffee();
        } else {
            throw new IllegalArgumentException("没有[" + type + "]类型的coffee");
        }
        return coffee.getName();
    }
}
