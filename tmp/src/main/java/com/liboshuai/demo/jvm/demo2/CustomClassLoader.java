package com.liboshuai.demo.jvm.demo2;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class CustomClassLoader extends ClassLoader{

    private final String customClassPath;

    public CustomClassLoader(String customClassPath, ClassLoader parent) {
        super(parent);
        this.customClassPath = customClassPath;
    }

    /**
     * 基类ClassLoader中loadClass方法是实现双亲委派的关键, 我们为了打破双亲委派, 所以要重写这个方法
     */
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        System.out.println("\n[CustomClassLoader]: 收到加载请求: " + name);
        synchronized (getClassLoadingLock(name)) {
            Class<?> loadedClass = findLoadedClass(name);
            if (loadedClass != null) {
                System.out.println("  [CustomClassLoader]: " + name + " 已经被加载过了 (来自 JVM 缓存)");
                return loadedClass;
            }
            if (name.startsWith("com.example.")) {
                System.out.println("  [CustomClassLoader]: 这是一个 'com.example' 包下的类, 执行 Child-first 策略");
                try {
                    loadedClass = findClass(name);
                    if (loadedClass != null) {
                        return loadedClass;
                    }
                } catch (ClassNotFoundException e) {
                    System.out.println("  [CustomClassLoader]: Child-first 未在 " + customClassPath + " 找到, 尝试委托给 Parent ...");
                }
            } else {
                System.out.println("  [CustomClassLoader]: 这不是 'com.example' 包下类, 执行 Parent-First (默认) 策略");
            }
            System.out.println("  [CustomClassLoader]: 委托给父加载器 (AppClassLoader) 加载 " + name);
            return super.loadClass(name);
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        System.out.println("  [CustomClassLoader]: 正在调用 findClass() 加载 " + name);
        // 1. 从对应的class文件中读取数据到byte数组
        byte[] classData = loadClassData(name);
        // 2. 将byte数组传入defineClass()方法获取Class对象
        return defineClass(name, classData, 0, classData.length);
    }

    private byte[] loadClassData(String name) {
        // 1. 将包名转换为路径名 (即将`com.example.MyTestClass`转为`C:/Users/lbs/me/project/java-project/lbs-demo/java-base/temp_classes/com/example/MyTestClass.java`)
        String path = customClassPath + File.separator + name.replace(".", File.separator) + ".class";
        // 2. 通过文件流读取绝对路径上的class文件内容到byte数组
        try (FileInputStream fis = new FileInputStream(path); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }
}
