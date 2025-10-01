package com.liboshuai.demo.factory.abstract_factory.factory;

import com.liboshuai.demo.factory.abstract_factory.coffee.Coffee;
import com.liboshuai.demo.factory.abstract_factory.dessert.Dessert;

public interface DessertFactory {
    Coffee createCoffee();
    Dessert createDessert();
}
