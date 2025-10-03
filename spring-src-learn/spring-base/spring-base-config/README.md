# Spring 纯Java类配置示例 (JavaConfig)

本项目演示了如何使用纯Java配置类 (JavaConfig) 来完全替代XML文件，实现一个零XML的Spring应用配置。这是目前非SpringBoot项目中推荐的现代配置方式。

## 功能演示

1.  **IoC (控制反转)**
    * 使用 `@Configuration` 注解将一个类标识为配置类。
    * 使用 `@ComponentScan` 注解来替代XML中的组件扫描功能。

2.  **AOP (面向切面编程)**
    * 使用 `@EnableAspectJAutoProxy` 注解来开启AOP功能。

3.  **容器启动**
    * 使用 `AnnotationConfigApplicationContext` 类从Java配置类加载Spring容器。

## 技术栈

* Java 1.8
* Spring Framework 5.3.31
* Maven

## 如何运行

1.  确保已安装 Java 1.8 和 Maven。
2.  克隆或下载项目。
3.  在IDE中打开项目，等待Maven下载依赖。
4.  运行 `src/main/java/com/example/Main.java` 类中的 `main` 方法。
5.  观察控制台输出，验证IoC和AOP功能是否正常工作。

## 核心配置文件

* `src/main/java/com/example/config/AppConfig.java`