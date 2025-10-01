# Spring Boot Web应用示例 (Spring Boot Example)

本项目是一个使用**Spring Boot框架 (2.5.5)** 构建的简单RESTful Web应用。

它实现了与原生Spring版本完全相同的功能，旨在展示Spring Boot如何通过自动化配置、依赖管理和内嵌服务器等特性，极大地简化Java Web应用的开发、构建和部署过程。

## ✨ 项目特性

* **RESTful API**: 实现了一个 `GET /hello` 接口，返回JSON格式的响应。
* **IoC/DI**: 演示了通过 `@Autowired` 实现依赖注入。
* **AOP**: 使用 `@Aspect` 创建了一个简单的日志切面。
* **自动化配置**: 依赖Spring Boot的自动配置能力，无需手动编写繁琐的配置类。
* **内嵌服务器**: 内置了Tomcat服务器，无需部署到外部容器。
* **可执行JAR**: 项目打包为单一的可执行JAR文件，方便部署和运行。

## 🛠️ 技术栈

* **Java**: 1.8
* **构建工具**: Maven
* **框架**: Spring Boot 2.5.5
    * Spring Framework 5.3.10
    * 内嵌Tomcat服务器

## 🚀 如何构建和运行

### 先决条件

1.  安装 [JDK 1.8](https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html)
2.  安装 [Apache Maven](https://maven.apache.org/)

### 步骤

#### 方式一：在IDE中直接运行 (推荐)

1.  **克隆或下载项目**
2.  **导入项目** 到你喜欢的IDE中（如IntelliJ IDEA, Eclipse）。
3.  **找到主启动类** `com.example.springboot.SpringBootExampleApplication.java`。
4.  右键点击该文件，选择 `Run 'SpringBootExampleApplication.main()'`。IDE会自动启动内嵌的Tomcat服务器并运行应用。

#### 方式二：使用Maven在命令行中运行

1.  **克隆或下载项目**

2.  **构建并打包项目**
    在项目根目录下，打开终端并执行以下Maven命令。这会在 `target` 目录下生成一个可执行的 `spring-boot-example-1.0-SNAPSHOT.jar` 文件。
    ```bash
    mvn clean package
    ```

3.  **运行应用**
    继续在终端中执行以下命令：
    ```bash
    java -jar target/spring-boot-example-1.0-SNAPSHOT.jar
    ```

### 访问接口

无论使用哪种方式启动，应用成功运行后，使用浏览器或curl工具访问以下URL：
```
http://localhost:8080/hello?name=Boot
```

### 预期结果

* **HTTP响应**:
    ```json
    {"message":"Hello, Boot! This is from Spring Boot."}
    ```
* **程序控制台日志**:
    ```
    ... (Spring Boot启动日志) ...
    AOP Before: 方法 sayHello 开始执行...
    AOP Before: 参数列表: [Boot]
    AOP After: 方法 sayHello 执行完毕。
    ```