package com.liboshuai.demo.jvm.demo2;

public class BreakDelegationDemo {
    public static void main(String[] args) throws Exception{
        System.out.println("[Main]-AppClassLoader加载类路径: " + System.getProperty("java.class.path"));
        
        // 1. 定义我们的 "插件" 目录
        String customPath = "C:/Users/lbs/me/project/flink-project/lbs-tmp/tmp/temp_classes";

        // 2. 创建我们的自定义加载器, 并设置它的父加载器为AppClassLoader
        CustomClassLoader loader = new CustomClassLoader(customPath, BreakDelegationDemo.class.getClassLoader());

        System.out.println("[Main]--- 尝试加载 'com.example.MyTestClass' (执行 Child-first) ---");
        // 3. 使用自定义加载器加载 "插件类" (这将触发 CustomClassLoader.loadClass() -> findClass()
        Class<?> myClass = loader.loadClass("com.example.MyTestClass");
        Object myInstance = myClass.getDeclaredConstructor().newInstance();
        myClass.getMethod("sayHello").invoke(myInstance);

        System.out.println("\n\n[Main]--- 尝试加载 'java.lang.String' (执行 Parent-First) ---");
        // 4. 尝试加载一个 JRE 核心类 (这将触发 CustomClassLoader.loadClass() -> super.loadClass() -> ... -> Bootstrap)
        Class<?> stringClass = loader.loadClass("java.lang.String");
        System.out.println("[Main]-加载 String 成功, 实际加载器: " + stringClass.getClassLoader() + " (null=Bootstrap)");

        System.out.println("\n\n[main]---证明 AppClassLoader 无法加载 MyTestClass ---");
        try {
            Class<?> ignore = BreakDelegationDemo.class.getClassLoader().loadClass("com.example.MyTestClass");
        } catch (ClassNotFoundException e) {
            System.out.println("[Main]-AppClassLoader 无法找到 'com.example.MyTestClass', 抛出异常: " + e.getClass().getName());
        }
    }
}
