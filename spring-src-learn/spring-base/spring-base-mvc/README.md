# 原生Spring MVC Web应用示例 (Native Spring MVC Example)

本项目是一个使用**原生Spring框架 (Spring Framework 5.3.10)** 构建的简单RESTful Web应用，**未使用Spring Boot**。

其主要目的是为了展示在没有Spring Boot的情况下，如何通过Java配置（`@Configuration`）来手动搭建和配置一个完整的Spring MVC应用，以便与Spring Boot项目进行对比，理解其简化之处。

## ✨ 项目特性

* **RESTful API**: 实现了一个 `GET /hello` 接口，返回JSON格式的响应。
* **IoC/DI**: 演示了通过 `@Autowired` 将Service注入到Controller中。
* **AOP**: 使用 `@Aspect` 创建了一个简单的日志切面，在Controller方法执行前后打印日志。
* **纯Java配置**: 完全移除了XML配置，使用`@Configuration`注解进行所有Spring容器的配置。
* **WAR包部署**: 项目打包为传统的WAR文件，需要部署在外部Servlet容器中。

## 🛠️ 技术栈

* **Java**: 1.8
* **构建工具**: Maven
* **框架**: Spring Framework 5.3.10
* **Servlet API**: 4.0.1
* **部署环境**: Apache Tomcat 9+ 或其他Servlet 3.1+ 容器

## 🚀 如何构建和运行

### 先决条件

1.  安装 [JDK 1.8](https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html)
2.  安装 [Apache Maven](https://maven.apache.org/)
3.  安装并配置好一个Servlet容器，例如 [Apache Tomcat 9](https://tomcat.apache.org/download-90.cgi)

### 步骤

1.  **克隆或下载项目**

2.  **构建项目**
    在项目根目录下，打开终端并执行以下Maven命令。这会在 `target` 目录下生成一个 `spring-native-example-1.0-SNAPSHOT.war` 文件。
    ```bash
    mvn clean package
    ```

3.  **部署应用**
    将生成的 `target/spring-native-example-1.0-SNAPSHOT.war` 文件复制到你的Tomcat服务器的 `webapps` 目录下。

4.  **启动Tomcat服务器**
    执行Tomcat的启动脚本（`bin/startup.sh` 或 `bin/startup.bat`）。Tomcat会自动解压并部署该应用。

5.  **访问接口**
    应用启动后，使用浏览器或curl工具访问以下URL：
    ```
    http://localhost:8080/spring-native-example-1.0-SNAPSHOT/hello?name=World
    ```
    *注意：URL中的 `spring-native-example-1.0-SNAPSHOT` 是WAR包的文件名，也即应用的上下文路径 (Context Path)。*

### 预期结果

* **HTTP响应**:
    ```json
    {"message":"Hello, World! This is from native Spring."}
    ```
* **Tomcat控制台日志**:
    ```
    AOP Before: 方法 sayHello 开始执行...
    AOP Before: 参数列表: [World]
    AOP After: 方法 sayHello 执行完毕。
    ```