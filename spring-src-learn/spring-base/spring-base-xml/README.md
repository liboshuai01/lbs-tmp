# Spring XML 配置示例

本项目是一个简单的Java Maven项目，旨在演示如何使用传统的XML文件来配置原生Spring框架。

## 功能演示

1.  **IoC (控制反转)**
    * 通过在 `applicationContext.xml` 中使用 `<bean>` 标签来定义和管理Java对象 (Bean)。
    * 使用 `<property>` 标签和setter方法实现依赖注入 (DI)。

2.  **AOP (面向切面编程)**
    * 通过在 `applicationContext.xml` 中使用 `<aop:config>` 和 `<aop:aspect>` 来配置切面。
    * 演示了前置通知 (`<aop:before>`)、后置通知 (`<aop:after>`) 等。

## 技术栈

* Java 1.8
* Spring Framework 5.3.31
* Maven

## 如何运行

1.  确保已安装 Java 1.8 和 Maven。
2.  克隆或下载项目。
3.  在IDE中打开项目，等待Maven下载依赖。
4.  运行 `src/main/java/com/example/Main.java` 类中的 `main` 方法。
5.  观察控制台输出，可以看到Bean被成功创建和调用，AOP日志也已正确打印。

## 核心配置文件

* `src/main/resources/applicationContext.xml`