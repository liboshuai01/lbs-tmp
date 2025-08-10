package com.liboshuai.demo.safe.singleton;

/**
 * 饿汉式单例模式、
 * 优点：实现简单，天生就是线程安全的，因为实例在类加载时就创建好了
 * 缺点：如果这个实例从始至终都未被使用，会造成内存浪费
 */
public class EagerSingleton {
    /**
     * 私有化构建函数，防止外部通过new来实例化对象
     */
    private EagerSingleton() {

    }

    /**
     * 在类加载的时候就创建一个唯一实例对象
     */
    private static final EagerSingleton instance = new EagerSingleton();

    /**
     * 提供一个公共的静态方法，用于获取上面这个唯一实例对象
     * @return EagerSingleton的唯一实例对象
     */
    public static EagerSingleton getInstance() {
        return instance;
    }

    /**
     * 示例方法
     */
    public void showMessage() {
        System.out.println("这是一个饿汉式单例！");
    }
}
