package com.liboshuai.demo.factory.abstract_factory.factory;

import com.liboshuai.demo.factory.abstract_factory.coffee.Coffee;
import com.liboshuai.demo.factory.abstract_factory.coffee.LatteCoffee;
import com.liboshuai.demo.factory.abstract_factory.dessert.Dessert;
import com.liboshuai.demo.factory.abstract_factory.dessert.Tiramisu;

public class ItalicanDessertFactory implements DessertFactory {
    @Override
    public Coffee createCoffee() {
        return new LatteCoffee();
    }

    @Override
    public Dessert createDessert() {
        return new Tiramisu();
    }
}
