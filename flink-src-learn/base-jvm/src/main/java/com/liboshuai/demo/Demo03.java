package com.liboshuai.demo;

public class Demo03 {
    public static class Father {
        int x = 10;

        public Father() {
            printX();
            this.x = 20;
        }
        public void printX() {
            System.out.println("Father.x = " + x);
        }
    }

    public static class Son extends Father {
        int x = 30;

        public Son() {
            printX();
            this.x = 40;
        }
        public void printX() {
            System.out.println("Son.x = " + x);
        }
    }

    public static void main(String[] args) {
        Father father = new Father(); // Father.x = 10
        Son son = new Son(); // Son.x = 0\n Son.x = 30;
    }
}
