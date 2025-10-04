package com.liboshuai.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AnnotationConfigApplicationContext implements ApplicationContext{

    private static final Logger LOG = LoggerFactory.getLogger(AnnotationConfigApplicationContext.class);

    public static final String SCOPE_SINGLETON = "singleton";

    public static final String SCOPE_PROTOTYPE = "prototype";

    /**
     * key=bean名称；value=bean实例对象
     */
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

    /**
     * key=bean名称；value=bean的定义信息
     */
    private final Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();

    public AnnotationConfigApplicationContext(Class<?> clazz) {
        // 扫描以获取用户定义的bean信息，存入beanDefinitionMap
        scan(clazz);
        // 创建非懒加载的单例bean，存入beanMap
        createNotLazySingletonBean();
    }

    /**
     * 创建非懒加载的单例bean
     */
    private void createNotLazySingletonBean() {
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            if (!Objects.equals(beanDefinition.getScope(), SCOPE_SINGLETON) || beanDefinition.isLazy()) {
                continue;
            }
            LOG.debug("创建单例非懒加载对象实例: {}", beanName);
            Object bean = createBean(beanDefinition.getBeanClass());
            singletonObjects.put(beanName, bean);
        }
    }

    /**
     * 扫描以获取用户定义的bean信息
     */
    private void scan(Class<?> clazz) {
        // 首先传入的配置类一定要有@Configuration和@ComponentScan这两个注解
        if (!clazz.isAnnotationPresent(Configuration.class) || !clazz.isAnnotationPresent(ComponentScan.class)) {
            return;
        }
        // 获取扫描路径下所有的类，获取到所有类的全限定名称，例如：com.liboshuai.demo.service.UserService
        List<String> allClassNameList = getAllClassNameList(clazz);
        // 过滤出使用了@Component注解的类，并实例化后存入beanMap
        putBeanMap(allClassNameList);
    }

    /**
     * 过滤出使用了@Component注解的类，并实例化后存入beanMap
     */
    private void putBeanMap(List<String> allClassNameList) {
        for (String className : allClassNameList) {
            try {
                Class<?> aClass = Class.forName(className);
                if (!aClass.isAnnotationPresent(Component.class)) {
                    continue;
                }
                // 获取beanName
                String beanName = getBeanName(aClass);
                // 创建 BeanDefinition，并放入map中
                BeanDefinition beanDefinition = createDefinition(aClass);
                beanDefinitionMap.put(beanName, beanDefinition);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 获取扫描路径下所有的类，获取到所有类的全限定名称，例如：com.liboshuai.demo.service.UserService
     */
    private List<String> getAllClassNameList(Class<?> clazz) {
        // 获取配置类上定义的扫描路径值
        ComponentScan componentScan = clazz.getAnnotation(ComponentScan.class);
        String[] basePackages = componentScan.value(); // 例如：com.liboshuai.demo
        // 获取应用类加载器
        ClassLoader appClassLoader = AnnotationConfigApplicationContext.class.getClassLoader();
        List<String> basePackageList = Arrays.stream(basePackages)
                .collect(Collectors.toList());
        List<String> allClassNameList = new ArrayList<>();
        for (String basePackage : basePackageList) {
            if (basePackage == null || basePackage.trim().isEmpty()) {
                continue;
            }
            String basePackagePath = basePackage.replace(".", "/");
            URL url = appClassLoader.getResource(basePackagePath);
            if (url == null) {
                LOG.warn("扫描路径没有找到: {}", basePackagePath);
                continue;
            }
            // 获取此文件目录下的所有类全限定名
            List<String> classNameList = scanDirectory(new File(url.getFile()), basePackage);
            allClassNameList.addAll(classNameList);
        }
        return allClassNameList;
    }

    /**
     * 获取beanName
     */
    private static String getBeanName(Class<?> aClass) {
        String beanName;
        Component componentAnnotation = aClass.getAnnotation(Component.class);
        String componentValue = componentAnnotation.value();
        if (componentValue == null || componentValue.trim().isEmpty()) {
            String simpleName = aClass.getSimpleName();
            beanName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
        } else {
            beanName = componentValue;
        }
        return beanName;
    }

    /**
     * 创建bean定义信息
     */
    private static BeanDefinition createDefinition(Class<?> aClass) {
        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.setBeanClass(aClass);
        if (aClass.isAnnotationPresent(Scope.class)) {
            // 存在 @Scope 注解
            Scope scopeAnnotation = aClass.getAnnotation(Scope.class);
            String value = scopeAnnotation.value();
            if (value == null || value.trim().isEmpty()) {
                value = SCOPE_SINGLETON;
            }
            if (!Objects.equals(value, SCOPE_SINGLETON) && !Objects.equals(value, SCOPE_PROTOTYPE)) {
                throw new IllegalArgumentException("类" + aClass + "的@Scope注解值" + value + "不合法");
            }
            beanDefinition.setScope(value);
        } else {
            // 不存在 @Scope 注解
            beanDefinition.setScope(SCOPE_SINGLETON);
        }
        // 如果存在@Lazy注解
        if (aClass.isAnnotationPresent(Lazy.class)) {
            Lazy lazyAnnotation = aClass.getAnnotation(Lazy.class);
            beanDefinition.setLazy(lazyAnnotation.value());
        }
        return beanDefinition;
    }

    /**
     * 根据bean名称获取bean对象
     */
    public Object getBean(String name) {
        if (!beanDefinitionMap.containsKey(name)) {
            throw new IllegalArgumentException("名称为[" + name + "]的bean对象没有被定义");
        }
        BeanDefinition beanDefinition = beanDefinitionMap.get(name);
        Object bean;
        if (Objects.equals(beanDefinition.getScope(), SCOPE_SINGLETON) && !beanDefinition.isLazy()) { // 直接获取单例非懒加载的bean对象实例
            bean = singletonObjects.get(name);
        } else if (Objects.equals(beanDefinition.getScope(), SCOPE_SINGLETON) && beanDefinition.isLazy()) { // 创建单例懒加载bean对象实例
            bean = singletonObjects.computeIfAbsent(name, beanName -> {
                LOG.debug("创建单例懒加载bean对象实例: {}", name);
                return createBean(beanDefinition.getBeanClass());
            });
        } else if (Objects.equals(beanDefinition.getScope(), SCOPE_PROTOTYPE)) { // 创建多例bean对象实例
            LOG.debug("创建多例bean对象实例: {}", name);
            bean = createBean(beanDefinition.getBeanClass());
        } else {
            throw new IllegalArgumentException("bean[" + beanDefinition.getBeanClass() + "]中定义@Scope注解值[" + beanDefinition.getScope() + "]与@Lazy注解值[" + beanDefinition.isLazy() + "]不合法");
        }
        return bean;
    }

    public Map<String, BeanDefinition> getBeanDefinitionMap() {
        return beanDefinitionMap;
    }

    /**
     * 创建bean对象
     */
    private Object createBean(Class<?> beanClass) {
        // 实例化
        Object bean = newInstance(beanClass);
        // 依赖注入（@Autowired注解会先按照类型查找bean，然后再按照name，我们这里简化只按照name）
        implAutowired(beanClass, bean);
        // 初始化前
        invokePostConstructMethod(beanClass, bean);
        // 初始化
        invokeAfterPropertiesSetMethod(bean);
        return bean;
    }

    /**
     * 执行afterPropertiesSetMethod方法
     */
    private static void invokeAfterPropertiesSetMethod(Object bean) {
        if (bean instanceof InitializingBean) {
            InitializingBean initializingBean = (InitializingBean) bean;
            initializingBean.afterPropertiesSet();
        }
    }

    /**
     * 执行postConstructMethod注解的方法
     */
    private static void invokePostConstructMethod(Class<?> beanClass, Object bean) {
        for (Method method : beanClass.getDeclaredMethods()) {
            method.setAccessible(true);
            if (!method.isAnnotationPresent(PostConstruct.class)) {
                continue;
            }
            try {
                method.invoke(bean);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 依赖注入（@Autowired注解会先按照类型查找bean，然后再按照name，我们这里简化只按照name）
     */
    private void implAutowired(Class<?> beanClass, Object bean) {
        Field[] fields = beanClass.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            if (!field.isAnnotationPresent(Autowired.class)) {
                continue;
            }
            Autowired autowiredAnnotation = field.getAnnotation(Autowired.class);
            boolean required = autowiredAnnotation.required();
            String fieldName = field.getName();
            BeanDefinition beanDefinition = beanDefinitionMap.get(fieldName);
            if (required) {
                if (beanDefinition == null) {
                    throw new IllegalStateException("名称为[" + fieldName + "]的bean没有被定义");
                }
            } else {
                if (beanDefinition == null) {
                    continue;
                }
            }
            // TODO: 暂时没有考虑懒加载和多例的情况
            Object fieldBean = singletonObjects.get(fieldName);
            try {
                field.set(bean, fieldBean);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 实例化
     */
    private static Object newInstance(Class<?> beanClass) {
        Object bean;
        try {
            bean = beanClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("使用反射创建[" + beanClass + "]对象出现异常", e);
        }
        return bean;
    }

    /**
     * 递归地扫描一个目录，找到所有的 .class 文件，并将它们转换为完全限定类名。
     *
     * @param directory   开始扫描的目录。
     * @param basePackage 与该目录对应的基础包名 (例如 "com.liboshuai.demo")。
     * @return 一个包含完全限定类名的列表 (例如 "com.liboshuai.demo.service.MyService")。
     */
    private List<String> scanDirectory(File directory, String basePackage) {
        List<String> classNames = new ArrayList<>();
        File[] files = directory.listFiles();
        if (files == null) {
            LOG.warn("要扫描的目录为空或不是一个有效目录: {}", directory.getPath());
            return classNames; // 返回空列表
        }

        for (File file : files) {
            if (file.isDirectory()) {
                // 如果是目录，就以更新后的包名递归扫描它
                classNames.addAll(scanDirectory(file, basePackage + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                // 如果是 .class 文件，就构建它的完全限定类名
                String simpleClassName = file.getName().substring(0, file.getName().length() - 6);
                classNames.add(basePackage + "." + simpleClassName);
            }
        }
        return classNames;
    }

}
