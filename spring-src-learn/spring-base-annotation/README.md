# Spring 注解配置示例

本项目演示了如何使用注解来简化Spring框架的配置。此方式减少了XML的编写量，将配置信息与对应的Java类结合在一起。

## 功能演示

1.  **IoC (控制反转)**
  * 在XML中开启组件扫描 (`<context:component-scan>`)。
  * 使用 `@Component`, `@Service`, `@Repository` 等注解将类标识为Spring Bean。
  * 使用 `@Autowired` 自动完成依赖注入。

2.  **AOP (面向切面编程)**
  * 在XML中开启对AspectJ注解的支持 (`<aop:aspectj-autoproxy/>`)。
  * 使用 `@Aspect` 将一个类定义为切面。
  * 使用 `@Before`, `@After`, `@Pointcut` 等注解来定义通知和切点。

## 技术栈

* Java 1.8
* Spring Framework 5.3.31
* Maven

## 如何运行

1.  确保已安装 Java 1.8 和 Maven。
2.  克隆或下载项目。
3.  在IDE中打开项目，等待Maven下载依赖。
4.  运行 `src/main/java/com/example/Main.java` 类中的 `main` 方法。
5.  观察控制台输出，其结果应与XML配置方式完全相同。

## 核心配置文件

* `src/main/resources/applicationContext.xml` (已简化)
* 项目中的Java类 (例如 `UserService.java`, `LoggingAspect.java`)