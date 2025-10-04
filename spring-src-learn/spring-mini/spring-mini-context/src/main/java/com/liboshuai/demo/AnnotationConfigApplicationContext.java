package com.liboshuai.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class AnnotationConfigApplicationContext {

    private static final Logger LOG = LoggerFactory.getLogger(AnnotationConfigApplicationContext.class);

    /**
     * key=bean名称；value=bean实例对象
     */
    private final Map<String, Object> beanMap = new HashMap<>();

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
            if (!Objects.equals(beanDefinition.getScope(), "singleton") || beanDefinition.isLazy()) {
                continue;
            }
            Object bean = reflectionCreateBean(beanDefinition.getBeanClass());
            LOG.debug("创建单例非懒加载bean对象实例: {}", bean);
            beanMap.put(beanName, bean);
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
                value = "singleton";
            }
            if (!Objects.equals(value, "singleton") && !Objects.equals(value, "prototype")) {
                throw new IllegalArgumentException("类" + aClass + "的@Scope注解值" + value + "不合法");
            }
            beanDefinition.setScope(value);
        } else {
            // 不存在 @Scope 注解
            beanDefinition.setScope("singleton");
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
        if (Objects.equals(beanDefinition.getScope(), "singleton") && !beanDefinition.isLazy()) { // 直接获取单例非懒加载的bean对象实例
            bean = beanMap.get(name);
        } else if (Objects.equals(beanDefinition.getScope(), "singleton") && beanDefinition.isLazy()) { // 创建单例懒加载bean对象实例
            if (beanMap.containsKey(name)) {
                bean = beanMap.get(name);
            } else {
                bean = reflectionCreateBean(beanDefinition.getBeanClass());
                LOG.debug("创建单例懒加载bean对象实例: {}", bean);
                beanMap.put(name, bean);
            }
        } else if (Objects.equals(beanDefinition.getScope(), "prototype")) { // 创建多例bean对象实例
            bean = reflectionCreateBean(beanDefinition.getBeanClass());
            LOG.debug("创建多例bean对象实例: {}", bean);
        } else {
            throw new IllegalArgumentException("bean[" + beanDefinition.getBeanClass() + "]中定义@Scope注解值[" + beanDefinition.getScope() + "]与@Lazy注解值[" + beanDefinition.isLazy() + "]不合法");
        }
        return bean;
    }

    /**
     * 通过反射创建bean对象
     */
    private static Object reflectionCreateBean(Class<?> beanClass) {
        Object bean;
        try {
            bean = beanClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
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
