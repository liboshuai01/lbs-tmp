package com.liboshuai.demo.reflection;


import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionPractice {
    public static void main(String[] args) throws Exception {
        System.out.println("---------- 1. 获取 Class 对象 ----------");
        demoGetClassObjects();

        System.out.println("\n---------- 2. 反射构造方法并创建实例 ----------");
        demoConstructors();

        System.out.println("\n---------- 3. 反射字段并读写值 ----------");
        demoFields();

        System.out.println("\n---------- 4. 反射方法并执行 ----------");
        demoMethods();

        System.out.println("\n---------- 5. 反射注解 ----------");
        demoAnnotations();
    }

    private static void demoAnnotations() throws Exception {
        Class<User> clazz = User.class;

        // 判断类上是否存在指定的注解
        boolean hasAnnotation = clazz.isAnnotationPresent(MyAnnotation.class);
        System.out.println("User 类上是否存在 MyAnnotation 注解: " + hasAnnotation);

        if (hasAnnotation) {
            // 获取注解的实例
            MyAnnotation myAnnotation = clazz.getAnnotation(MyAnnotation.class);
            // 从注解实例中读取属性值
            String value = myAnnotation.value();
            System.out.println("MyAnnotation 的 value 属性值：" + value);
        }
    }

    /**
     * 演出操作方法
     */
    private static void demoMethods() throws Exception {
        Class<User> clazz = User.class;
        User user = clazz.getConstructor(String.class, int.class).newInstance("孙七", 50);

        // --- 操作 public 方法 ---
        System.out.println("--- 操作 public 方法 ---");
        Method publicMethod = clazz.getMethod("publicMethod");
        // 执行方法：method.invoke(对象实例, 方法参数...)
        publicMethod.invoke(user);

        // --- 操作 private 方法 ---
        System.out.println("--- 操作 private 方法 ---");
        // TODO: 为什么这里还需要传入参数类型列表，代码里面不是写的有吗？是为了区别重载方法吗？
        Method privateMethod = clazz.getDeclaredMethod("privateMethod", String.class);
        // **关键**：破解私有方法的访问权限
        privateMethod.setAccessible(true);
        // 执行私有方法并接收返回值
        Object returnValue = privateMethod.invoke(user, "一条秘密信息");
        System.out.println("私有方法返回值：" + returnValue);

        // --- 操作 static 方法 ---
        System.out.println("--- 操作 static 方法 ---");
        Method staticMethod = clazz.getMethod("staticMethod");
        // **注意**：执行静态方法时，第一个参数（对象实例）为null
        staticMethod.invoke(null);
    }

    /**
     * 演示操作字段
     */
    private static void demoFields() throws Exception {
        Class<User> clazz = User.class;
        User user = clazz.getConstructor(String.class, int.class).newInstance("王五", 40);
        System.out.println("原始对象：" + user);

        // --- 操作 public 字段 ---
        System.out.println("--- 操作 public 字段 ---");
        Field addressField = clazz.getField("address");
        // 读取值：field.get(对象实例)
        String originalAddress = (String) addressField.get(user);
        System.out.println("原始 public address: " + originalAddress);
        // 写入值： field.set(对象实例, 新的值)
        addressField.set(user, "上海");
        System.out.println("修改后对象: " + user);

        // --- 操作 private 字段 ---
        System.out.println("--- 操作 private 字段 ---");
        Field nameField = clazz.getDeclaredField("name");
        // **关键**：破解私有字段的访问权限
        nameField.setAccessible(true);
        String originalName = (String) nameField.get(user);
        System.out.println("原始 private name: " + originalName);
        nameField.set(user, "赵六");
        System.out.println("修改后对象：" + user);
    }

    /**
     * 演示操作构造方法
     */
    private static void demoConstructors() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<User> clazz = User.class;

        // --- 获取并使用 public 构造方法 ---
        System.out.println("--- 操作 public 构造方法 ---");
        // 获取有两个参数 (String, int) 的 public 构造
        Constructor<User> publicConstructor = clazz.getConstructor(String.class, int.class);
        // 使用构造方法创建实例
        User user1 = publicConstructor.newInstance("李四", 30);
        System.out.println("通过 public 构造创建的实例：" + user1);

        // --- 获取并使用 private 构造方法 ---
        System.out.println("--- 操作 private 构造方法 ---");
        // 获取无参的 private 构造
        Constructor<User> privateConstructor = clazz.getDeclaredConstructor();
        // **关键**：私有构造默认无法访问，需要“暴力破解”
        privateConstructor.setAccessible(true);
        // 使用私有构造创建实例
        User user2 = privateConstructor.newInstance();
        System.out.println("通过 private 构造创建的实例：" + user2);
    }

    /**
     * 演示获取 Class 对象的
     */
    private static void demoGetClassObjects() throws ClassNotFoundException {
        // 方式一：通过类名.class
        Class<User> clazz1 = User.class;
        // 方式二：通过实例对象.getClass()
        User user = new User("张三", 25);
        Class<? extends User> clazz2 = user.getClass();
        // 方式三：通过 Class.forName("类的全限定名")
        Class<?> clazz3 = Class.forName("com.liboshuai.demo.reflection.User");

        System.out.println("方式一：" + clazz1);
        System.out.println("方式二：" + clazz2);
        System.out.println("方式三：" + clazz3);
        System.out.println("三者是否相等：" + (clazz1 == clazz2 && clazz2 == clazz3));
    }
}



























