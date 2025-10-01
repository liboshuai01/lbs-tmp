package com.liboshuai.demo.singleton;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class Singleton1Test {

    @Test
    void testNormal() {
        Singleton1 instance1 = Singleton1.getInstance();
        Singleton1 instance2 = Singleton1.getInstance();

        assertNotNull(instance1, "单例实例不应该为 null");
        assertNotNull(instance2, "单例实例不应该为 null");

        assertSame(instance1, instance2, "多次获取的实例应该是同一个对象");
    }

    @Test
    void testSerializer() {
        Path path = Paths.get("C:/Users/lbs/me/project/flink-project/lbs-tmp/flink-src-learn/base-gof/data/singleton.txt");

        // 序列化之前，先获取原始的单例实例
        Singleton1 originalInstance = Singleton1.getInstance();

        try {
            // 步骤 1: 将对象序列化到文件
            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(path))) {
                oos.writeObject(originalInstance);
            }

            // 步骤 2: 从文件中反序列化对象
            Singleton1 deserializedInstance;
            try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(path))) {
                deserializedInstance = (Singleton1) ois.readObject();
            }

            // 步骤 3: 断言结果
            log.info("原始实例的哈希值: {}", originalInstance.hashCode());
            log.info("反序列化实例的哈希值: {}", deserializedInstance.hashCode());

            // 这个断言的目的就是为了证明：普通的序列化/反序列化会创建新的对象，从而破坏单例。
            // 因此，我们期望它们“不相同”（not same）。这个测试一开始应该是可以通过的。
            assertSame(originalInstance, deserializedInstance, "反序列化后应该返回同一个实例");

        } catch (IOException | ClassNotFoundException e) {
            log.error("序列化或反序列化Java对象出现异常", e);
            // 如果发生异常，显式地让测试失败
            fail("测试因异常而失败: " + e.getMessage());
        }
    }

}