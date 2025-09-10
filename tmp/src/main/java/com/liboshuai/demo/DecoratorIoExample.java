package com.liboshuai.demo;

import java.io.*;

public class DecoratorIoExample {
    public static void main(String[] args) {
        try {
            // 1. 创建一个具体构件 (ConcreteComponent)
            FileInputStream fis = new FileInputStream("my-file.txt");

            // 2. 用具体装饰者 (ConcreteDecorator) 包装它，增加字节到字符的转换功能
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");

            // 3. 再用另一个具体装饰者包装，增加缓冲功能，提高效率
            BufferedReader br = new BufferedReader(isr);

            // 现在 br 对象就是一个被层层装饰过的 Reader
            String line;
            System.out.println("文件内容为:");
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }

            // 关闭流时，只需要关闭最外层的装饰者，它会自动调用被包装对象的 close 方法
            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}