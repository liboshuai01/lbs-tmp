package com.liboshuai.demo.singleton;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class Singleton5Test {

    @Test
    void test() {
        Singleton5 instance1 = Singleton5.getInstance();
        Singleton5 instance2 = Singleton5.getInstance();

        assertNotNull(instance1, "单例实例不应该为 null");
        assertNotNull(instance2, "单例实例不应该为 null");

        assertSame(instance1, instance2, "多次获取的实例应该是同一个对象");
    }

    @Test
    void testSerializer() {
        Path path = Paths.get("C:/Users/lbs/me/project/flink-project/lbs-tmp/flink-src-learn/base-gof/data/singleton.txt");

        // 序列化之前，先获取原始的单例实例
        Singleton5 originalInstance = Singleton5.getInstance();

        try {
            // 步骤 1: 将对象序列化到文件
            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(path))) {
                oos.writeObject(originalInstance);
            }

            // 步骤 2: 从文件中反序列化对象
            Singleton5 deserializedInstance;
            try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(path))) {
                deserializedInstance = (Singleton5) ois.readObject();
            }

            // 步骤 3: 断言结果
            log.info("原始实例的哈希值: {}", originalInstance.hashCode());
            log.info("反序列化实例的哈希值: {}", deserializedInstance.hashCode());

            assertSame(originalInstance, deserializedInstance, "反序列化后应该返回同一个实例");

        } catch (IOException | ClassNotFoundException e) {
            log.error("序列化或反序列化Java对象出现异常", e);
            // 如果发生异常，显式地让测试失败
            fail("测试因异常而失败: " + e.getMessage());
        }
    }

}