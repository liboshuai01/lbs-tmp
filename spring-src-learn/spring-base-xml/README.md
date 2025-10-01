好的，很高兴能帮助你重新回顾原生Spring框架的使用。忘记了很正常，因为SpringBoot确实极大地简化了Spring的配置和使用过程。

我们将分三步进行，今天我们先从最经典的 **XML配置方式** 开始。这会让你对Spring最核心的IoC（控制反转）和AOP（面向切面编程）有一个非常直观和底层的理解。

-----

### **第一部分：使用XML配置演示Spring IoC与AOP**

在开始之前，我们需要先搭建一个简单的Maven项目。

#### **第一步：项目设置 (Project Setup)**

1.  创建一个标准的Maven项目。
2.  在 `pom.xml` 文件中加入Spring的核心依赖。对于我们的演示，需要 `spring-context` (包含了核心的IoC容器) 和 `spring-aspects` (用于AOP功能)。

**`pom.xml`**

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>spring-xml-example</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
        <spring.version>5.3.31</spring.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
            <version>${spring.version}</version>
        </dependency>

        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-aspects</artifactId>
            <version>${spring.version}</version>
        </dependency>
    </dependencies>
</project>
```

#### **第二步：IoC (控制反转) 功能演示**

IoC的核心思想是：**将对象的创建和对象之间依赖关系的管理，从程序代码中转移到Spring容器中**。我们通过XML文件来告诉Spring容器要创建哪些对象（我们称之为Bean），以及它们之间的依赖关系。

**1. 创建业务相关的类**

我们来模拟一个简单的用户管理场景，包含 `UserService` 和 `UserDao`。`UserService` 需要依赖 `UserDao` 来完成保存用户的操作。

* **UserDao (数据访问接口和实现)**

<!-- end list -->

```java
// src/main/java/com/example/dao/UserDao.java
package com.example.dao;

public interface UserDao {
    void saveUser(String username);
}

// src/main/java/com/example/dao/UserDaoImpl.java
package com.example.dao;

public class UserDaoImpl implements UserDao {
    @Override
    public void saveUser(String username) {
        // 模拟数据库保存操作
        System.out.println("成功保存用户到数据库: " + username);
    }
}
```

* **UserService (业务逻辑类)**

`UserService` 有一个 `UserDao` 类型的属性，并提供了 `setter` 方法。Spring容器将通过这个 `setter` 方法来注入 `UserDao` 的实例，这就是所谓的“依赖注入（DI）”，它是IoC的一种具体实现方式。

```java
// src/main/java/com/example/service/UserService.java
package com.example.service;

import com.example.dao.UserDao;

public class UserService {

    private UserDao userDao;

    // Spring容器将通过这个方法注入一个UserDao的实例
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void addUser(String username) {
        System.out.println("业务层调用DAO层...");
        userDao.saveUser(username);
    }
}
```

**2. 创建Spring XML配置文件**

在 `src/main/resources` 目录下创建一个XML文件，通常命名为 `applicationContext.xml`。这个文件是Spring容器的“配置图纸”。

**`src/main/resources/applicationContext.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="userDao" class="com.example.dao.UserDaoImpl"/>

    <bean id="userService" class="com.example.service.UserService">
        <property name="userDao" ref="userDao"/>
    </bean>

</beans>
```

**配置解读:**

* 我们定义了两个Bean: `userDao` 和 `userService`。
* 在 `userService` 的定义中，我们使用 `<property>` 标签告诉Spring：“请将ID为 `userDao` 的那个Bean，通过 `setUserDao()` 方法，注入到 `userService` 这个Bean中”。

**3. 创建启动类，测试IoC**

现在我们写一个 `main` 方法来启动Spring容器，并从容器中获取我们配置好的 `userService` 对象来执行业务方法。

```java
// src/main/java/com/example/Main.java
package com.example;

import com.example.service.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {
    public static void main(String[] args) {
        // 1. 初始化Spring容器，加载配置文件
        // ClassPathXmlApplicationContext会从类路径下查找配置文件
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");

        // 2. 从容器中获取Bean
        // 我们不再需要手动 new UserService()，而是直接从容器中获取
        UserService userService = context.getBean("userService", UserService.class);

        // 3. 调用方法
        userService.addUser("张三");
    }
}
```

**运行结果:**

```
业务层调用DAO层...
成功保存用户到数据库: 张三
```

**IoC小结:**
可以看到，整个过程中我们没有在Java代码中写过 `new UserDaoImpl()` 或者 `userService.setUserDao(new UserDaoImpl())` 这样的代码。对象的创建和组装全部交给了Spring容器来完成，这就是控制反转。我们的业务代码只负责使用对象，不再关心对象的创建和依赖关系，实现了代码的解耦。

-----

#### **第三步：AOP (面向切面编程) 功能演示**

AOP允许我们将那些与核心业务无关，但又散布在多个地方的“横切关注点”（Cross-cutting Concerns）代码（如日志记录、性能监控、事务管理等）模块化。

现在，我们想在 `addUser` 方法执行前后打印一些日志。

**1. 创建切面类 (Aspect)**

切面类就是一个普通的Java类，里面包含了我们希望在特定时机（例如方法执行前、执行后）执行的代码，这些代码被称为“通知 (Advice)”。

```java
// src/main/java/com/example/aop/LoggingAspect.java
package com.example.aop;

import org.aspectj.lang.JoinPoint;

public class LoggingAspect {

    // 前置通知：在目标方法执行之前执行
    public void beforeAdvice(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        System.out.println("【AOP-前置通知】: " + methodName + " 方法开始执行...");
    }

    // 后置通知：在目标方法执行之后执行（无论是否发生异常）
    public void afterAdvice(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        System.out.println("【AOP-后置通知】: " + methodName + " 方法执行结束.");
    }

    // 返回通知：在目标方法成功执行并返回结果后执行
    public void afterReturningAdvice(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        System.out.println("【AOP-返回通知】: " + methodName + " 方法成功返回, 结果是: " + result);
    }

    // 异常通知：在目标方法抛出异常后执行
    public void afterThrowingAdvice(JoinPoint joinPoint, Exception ex) {
        String methodName = joinPoint.getSignature().getName();
        System.out.println("【AOP-异常通知】: " + methodName + " 方法执行时发生异常: " + ex.getMessage());
    }
}
```

**2. 更新XML配置以启用AOP**

我们需要修改 `applicationContext.xml` 文件，加入AOP相关的配置。

**`src/main/resources/applicationContext.xml` (更新后)**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/aop
        https://www.springframework.org/schema/aop/spring-aop.xsd">

    <bean id="userDao" class="com.example.dao.UserDaoImpl"/>
    <bean id="userService" class="com.example.service.UserService">
        <property name="userDao" ref="userDao"/>
    </bean>

    <bean id="loggingAspect" class="com.example.aop.LoggingAspect"/>

    <aop:config>
        <aop:pointcut id="serviceLayer" expression="execution(* com.example.service.UserService.addUser(..))"/>

        <aop:aspect ref="loggingAspect">
            <aop:before method="beforeAdvice" pointcut-ref="serviceLayer"/>
            <aop:after method="afterAdvice" pointcut-ref="serviceLayer"/>
            <aop:after-returning method="afterReturningAdvice" pointcut-ref="serviceLayer" returning="result"/>
            <aop:after-throwing method="afterThrowingAdvice" pointcut-ref="serviceLayer" throwing="ex"/>
        </aop:aspect>
    </aop:config>
</beans>
```

**配置解读:**

1.  首先，我们在XML的头部加入了 `aop` 的命名空间和 `schemaLocation`。
2.  将 `LoggingAspect` 类也配置成一个Bean。
3.  使用 `<aop:config>` 开始AOP的配置。
4.  `<aop:pointcut>` 定义了“在哪里”执行切面逻辑。`expression` 使用AspectJ的表达式语言，`execution(* com.example.service.UserService.addUser(..))` 的意思是“匹配`com.example.service.UserService`类中名为`addUser`、不限返回值、任意参数的方法”。
5.  `<aop:aspect>` 将 `loggingAspect` 这个Bean声明为一个切面，并使用不同的通知标签（如 `<aop:before>`）将切面中的方法（如 `beforeAdvice`）和我们定义的切点 `serviceLayer` 关联起来。

**3. 运行测试**

我们不需要修改 `Main.java`，直接再次运行它。

**运行结果:**

```
【AOP-前置通知】: addUser 方法开始执行...
业务层调用DAO层...
成功保存用户到数据库: 张三
【AOP-返回通知】: addUser 方法成功返回, 结果是: null
【AOP-后置通知】: addUser 方法执行结束.
```

**AOP小结:**
我们没有修改任何一行 `UserService` 的代码，就成功地为其添加了日志功能。Spring在运行时动态地创建了一个 `UserService` 的代理对象，这个代理对象在调用原始 `addUser` 方法的前后，织入了我们在 `LoggingAspect` 中定义的逻辑。这就是AOP的强大之处，它让我们的业务代码更纯粹，更容易维护。

-----

至此，我们就完成了使用XML配置方式对Spring IoC和AOP核心功能的演示。这种方式虽然繁琐，但能让你非常清晰地看到每个Bean是如何被定义、依赖是如何被注入、切面是如何被织入的，是理解Spring底层原理的绝佳途径。

接下来，，我们继续探讨 **使用注解的方式**，你会看到配置将如何被大大简化。