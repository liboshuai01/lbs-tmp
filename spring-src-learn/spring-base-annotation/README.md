当然，我们接着来看第二种方式：**使用注解**。

你会发现，从XML配置迁移到注解配置，代码会变得更加简洁和直观，因为配置信息直接写在了它所配置的Java类上。

-----

### **第二部分：使用注解配置演示Spring IoC与AOP**

我们将沿用上一部分的目录结构和 `pom.xml` 文件，依赖是完全相同的。我们的主要工作是修改Java类和 `applicationContext.xml` 文件。

#### **第一步：IoC (控制反转) 功能演示 (注解版)**

核心思想是使用 `@Component` 及其衍生注解（`@Service`, `@Repository`）来标识一个类为Spring Bean，并使用 `@Autowired` 来自动进行依赖注入。

**1. 修改业务相关的类**

* **UserDaoImpl (添加 `@Repository` 注解)**
  `@Repository` 注解用于标注数据访问层（DAO）的组件。

  ```java
  // src/main/java/com/example/dao/UserDaoImpl.java
  package com.example.dao;

  import org.springframework.stereotype.Repository;

  @Repository // 将这个类标识为一个Spring Bean，ID默认为类名首字母小写(userDaoImpl)
  public class UserDaoImpl implements UserDao {
      @Override
      public void saveUser(String username) {
          System.out.println("成功保存用户到数据库: " + username);
      }
  }
  ```

* **UserService (添加 `@Service` 和 `@Autowired` 注解)**
  `@Service` 用于标注业务逻辑层的组件。
  `@Autowired` 用于自动注入依赖。Spring会在容器中查找类型匹配的Bean（在这里是 `UserDao` 类型的Bean）并将其注入。

  ```java
  // src/main/java/com/example/service/UserService.java
  package com.example.service;

  import com.example.dao.UserDao;
  import org.springframework.beans.factory.annotation.Autowired;
  import org.springframework.stereotype.Service;

  @Service // 将这个类标识为一个Spring Bean
  public class UserService {

      @Autowired // 告诉Spring自动注入一个UserDao类型的Bean
      private UserDao userDao;

      // 注意：使用@Autowired字段注入后，我们不再需要setter方法了！

      public void addUser(String username) {
          System.out.println("业务层调用DAO层...");
          userDao.saveUser(username);
      }
  }
  ```

**2. 修改Spring XML配置文件**

现在，我们不再需要手动声明每一个 `<bean>` 了。我们只需要告诉Spring去哪些包下面扫描带有特定注解（如 `@Component`, `@Service`等）的类，并把它们自动注册为Bean。

**`src/main/resources/applicationContext.xml` (注解版)**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        https://www.springframework.org/schema/context/spring-context.xsd">

    <context:component-scan base-package="com.example"/>

    </beans>
```

**配置解读:**

* 我们添加了 `context` 的命名空间。
* `<context:component-scan base-package="com.example"/>` 这一行配置是核心。它指示Spring容器去扫描 `com.example` 包以及其子包下的所有类，如果一个类被 `@Component`, `@Repository`, `@Service`, `@Controller` 等注解标记，Spring就会自动将其注册为一个Bean。并且，如果发现 `@Autowired` 注解，会自动完成依赖注入。

**3. 运行启动类**

启动类 `Main.java` **完全不需要修改**。它依然通过加载 `applicationContext.xml` 来启动容器。

**运行结果 (与XML版完全相同):**

```
业务层调用DAO层...
成功保存用户到数据库: 张三
```

**IoC小结:**
通过注解，我们将Bean的定义和依赖关系从XML文件中“搬回”了Java代码中。这使得代码的内聚性更高，配置也大大减少了。

-----

#### **第二步：AOP (面向切面编程) 功能演示 (注解版)**

AOP同样可以用注解来配置，这样切面和通知的定义也会更加清晰。

**1. 修改切面类 (LoggingAspect)**

我们将使用 `@Aspect`, `@Component`, `@Pointcut`, `@Before` 等注解来替代XML中的AOP配置。

```java
// src/main/java/com/example/aop/LoggingAspect.java
package com.example.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect      // 1. 使用@Aspect注解声明这是一个切面类
@Component   // 2. 使用@Component注解，让Spring容器能够扫描到并创建这个Bean
public class LoggingAspect {

    // 3. 定义一个可重用的切点
    // @Pointcut注解定义了切点表达式
    @Pointcut("execution(* com.example.service.UserService.addUser(..))")
    public void serviceLayerPointcut() {}

    // 4. 定义各种通知，并引用上面的切点
    @Before("serviceLayerPointcut()")
    public void beforeAdvice(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        System.out.println("【AOP-前置通知】: " + methodName + " 方法开始执行...");
    }

    @After("serviceLayerPointcut()")
    public void afterAdvice(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        System.out.println("【AOP-后置通知】: " + methodName + " 方法执行结束.");
    }

    @AfterReturning(pointcut = "serviceLayerPointcut()", returning = "result")
    public void afterReturningAdvice(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        System.out.println("【AOP-返回通知】: " + methodName + " 方法成功返回, 结果是: " + result);
    }

    @AfterThrowing(pointcut = "serviceLayerPointcut()", throwing = "ex")
    public void afterThrowingAdvice(JoinPoint joinPoint, Exception ex) {
        String methodName = joinPoint.getSignature().getName();
        System.out.println("【AOP-异常通知】: " + methodName + " 方法执行时发生异常: " + ex.getMessage());
    }
}
```

**2. 更新XML配置以启用注解驱动的AOP**

我们还需要在XML中开启对 `@Aspect` 注解的支持。

**`src/main/resources/applicationContext.xml` (最终版)**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        https://www.springframework.org/schema/context/spring-context.xsd
        http://www.springframework.org/schema/aop
        https://www.springframework.org/schema/aop/spring-aop.xsd">

    <context:component-scan base-package="com.example"/>

    <aop:aspectj-autoproxy/>
    
    </beans>
```

**配置解读:**

* 我们添加了 `aop` 的命名空间。
* `<aop:aspectj-autoproxy/>` 这行配置的作用是激活Spring对 `@Aspect` 注解的解析。当Spring发现一个Bean上有 `@Aspect` 注解时，它就会自动为符合切点表达式的其他Bean创建代理。

**3. 运行测试**

同样，`Main.java` 无需任何改动，直接运行。

**运行结果 (与XML AOP版完全相同):**

```
【AOP-前置通知】: addUser 方法开始执行...
业务层调用DAO层...
成功保存用户到数据库: 张三
【AOP-返回通知】: addUser 方法成功返回, 结果是: null
【AOP-后置通知】: addUser 方法执行结束.
```

-----

**注解方式总结:**

* **优点**:
    * **配置简化**: 大大减少了XML文件的编写量。
    * **高内聚**: 配置信息和Java代码在一起，修改和理解起来更方便。
    * **类型安全**: 编译器可以检查注解的正确性。
* **缺点**:
    * **配置分散**: 配置信息散落在各个Java类中，对于大型项目，想看整体配置关系不如XML直观。
    * **修改需重新编译**: 修改配置需要重新编译Java代码。

注解方式是目前Spring（以及SpringBoot）开发的主流方式。它在开发效率和代码可读性之间取得了很好的平衡。

接下来，我们将介绍最后一种方式：**纯Java配置类**，它能让你完全摆脱XML文件。
