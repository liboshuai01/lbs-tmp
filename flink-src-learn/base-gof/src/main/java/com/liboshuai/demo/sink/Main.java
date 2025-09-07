package com.liboshuai.demo.sink;

public class Main {
    public static void main(String[] args) {
        SouGouInput souGouInput = new SouGouInput();
        AbstractSkin skin = new HeimaSpecificSkin();
        souGouInput.setSink(skin);
        skin.display();
    }
}
