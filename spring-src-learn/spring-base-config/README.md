好的，我们来到最后一种方式，也是目前非SpringBoot项目中非常推荐的一种方式：**纯Java配置类 (JavaConfig)**。

这种方式可以让你完全摆脱XML文件，将所有Spring容器的配置都集中在Java代码中完成。这样做的好处是配置是类型安全的（可以在编译期发现错误），并且可以利用Java语言的所有特性（如逻辑判断、继承等）来动态配置Bean。

-----

### **第三部分：使用纯Java配置类演示Spring IoC与AOP**

我们将继续沿用之前的项目结构。最关键的变化是：我们将删除 `applicationContext.xml` 文件，并创建一个专门的Java类来替代它的作用。

#### **第一步：保持业务和切面类不变**

一个重要的前提是，我们在第二部分（注解方式）中修改过的业务类和切面类**完全不需要再做任何改动**。`@Service`, `@Repository`, `@Autowired`, `@Aspect` 等注解依然是核心。Java配置类将负责去“启用”这些注解的功能。

所以，`UserDao.java`, `UserDaoImpl.java`, `UserService.java`, `LoggingAspect.java` 都保持原样。

#### **第二步：创建Java配置类**

我们需要创建一个新的Java类，通常放在一个专门的 `config` 包下，用来替代 `applicationContext.xml` 的所有功能。

**`src/main/java/com/example/config/AppConfig.java`**

```java
package com.example.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 这是一个Spring的配置类，它完全替代了applicationContext.xml文件。
 */
@Configuration // 1. @Configuration: 声明这个类是一个Spring配置类。
@ComponentScan(basePackages = "com.example") // 2. @ComponentScan: 等同于XML中的<context:component-scan>，开启组件扫描。
@EnableAspectJAutoProxy // 3. @EnableAspectJAutoProxy: 等同于XML中的<aop:aspectj-autoproxy/>，开启AOP注解支持。
public class AppConfig {
    // 这个类可以是空的，因为我们所有的Bean都通过@ComponentScan来自动注册。
    // 如果需要手动定义一些Bean（比如来自第三方库的类），可以在这里添加@Bean方法。
}
```

**配置解读:**

* `@Configuration`: 这是最核心的注解，它告诉Spring容器，这个类是配置信息的来源。
* `@ComponentScan(basePackages = "com.example")`: 功能和XML中的 `<context:component-scan>` 完全一样，用于扫描指定包下的组件。
* `@EnableAspectJAutoProxy`: 功能和XML中的 `<aop:aspectj-autoproxy/>` 完全一样，用于启用对 `@Aspect` 切面的支持。

可以看到，XML中的核心配置都以注解的形式一一对应到了这个Java类上。

#### **第三步：修改启动类**

因为我们不再使用XML文件来启动Spring容器，所以启动类 `Main.java` 需要进行修改，以适配从Java配置类加载。

**`src/main/java/com/example/Main.java` (更新后)**

```java
package com.example;

import com.example.config.AppConfig;
import com.example.service.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) {
        // 1. 使用AnnotationConfigApplicationContext从Java配置类加载容器
        // 不再使用ClassPathXmlApplicationContext
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        // 2. 从容器中获取Bean (这一步和之前完全一样)
        // Bean的名称默认是类名首字母小写，所以是"userService"
        UserService userService = context.getBean("userService", UserService.class);

        // 3. 调用方法 (这一步和之前完全一样)
        userService.addUser("王五");
    }
}
```

**核心变化:**

* 我们用 `AnnotationConfigApplicationContext` 替换了 `ClassPathXmlApplicationContext`。
* 在创建Context时，传入的不再是XML文件的路径字符串，而是配置类 `AppConfig.class` 本身。

#### **第四步：删除XML文件**

现在，`src/main/resources/applicationContext.xml` 文件已经没有任何作用了，你可以安全地将它从项目中删除。

#### **运行与总结**

再次运行 `Main.java`。

**运行结果 (依然完全相同):**

```
【AOP-前置通知】: addUser 方法开始执行...
业务层调用DAO层...
成功保存用户到数据库: 王五
【AOP-返回通知】: addUser 方法成功返回, 结果是: null
【AOP-后置通知】: addUser 方法执行结束.
```

-----

### **三种方式的回顾与总结**

到这里，我们已经用三种不同的方式演示了原生Spring框架的IoC和AOP功能。

1.  **XML配置**:

    * **优点**: 配置集中，一目了然，可以清晰地看到整个应用的组件和它们之间的关系。
    * **缺点**: 繁琐，易出错（比如类名写错），与业务代码分离，不够灵活。
    * **理解**: 这是理解Spring底层原理的最佳方式，让你明白Bean是如何被定义和注入的。


2.  **注解配置 (XML + Annotations)**:

    * **优点**: 大大简化配置，配置信息和代码结合紧密，开发效率高。
    * **缺点**: 仍然依赖一个XML文件来启动组件扫描和AOP等功能。
    * **理解**: 这是从XML向现代Spring开发过渡的桥梁，也是目前大量老旧项目正在使用的方式。

3.  **纯Java配置类**:

    * **优点**: 完全脱离XML，配置是类型安全的，可以利用Java语言的全部能力进行编程化配置，是SpringBoot配置方式的基础。
    * **缺点**: 配置分散在代码中，对于初学者可能不如XML直观。
    * **理解**: 这是当前纯Spring项目（非SpringBoot）的推荐方式，也是通向SpringBoot自动配置原理的必经之路。

你之前一直使用SpringBoot，其实就是在使用第三种方式的“超级进化版”。SpringBoot通过大量的默认 `AppConfig`（自动配置类）和更简单的注解（如 `@SpringBootApplication`），将这一切都帮你自动完成了，所以你只需要关注业务代码。

希望这个从“手动挡”到“自动挡”的演示过程，能帮助你更好地理解Spring源码的核心思想！