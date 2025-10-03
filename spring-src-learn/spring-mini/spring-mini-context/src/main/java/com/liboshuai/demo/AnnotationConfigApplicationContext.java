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
     * key: bean名称
     * value：bean实例对象
     * 例如：key=userServide，value=UserService类的实例化对象
     */
    private final Map<String, Object> beanMap = new HashMap<>();

    public AnnotationConfigApplicationContext(Class<?> clazz) {
        // 首先传入的配置类一定要有@Configuration和@ComponentScan这两个注解
        if (!clazz.isAnnotationPresent(Configuration.class) || !clazz.isAnnotationPresent(ComponentScan.class)) {
            return;
        }
        // 获取配置类上定义的扫描路径值
        ComponentScan componentScan = clazz.getAnnotation(ComponentScan.class);
        String[] basePackages = componentScan.value(); // 例如：com.liboshuai.demo
        // 获取应用类加载器
        ClassLoader appClassLoader = AnnotationConfigApplicationContext.class.getClassLoader();
        // 获取扫描路径下所有的类，获取到所有类的全限定名称，例如：com.liboshuai.demo.service.UserService
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
            List<String> classNameList = scanDirectory(new File(url.getFile()), basePackage);
            allClassNameList.addAll(classNameList);
        }
        // 过滤出使用了@Component注解的类，并实例化后存入beanMap
        for (String className : allClassNameList) {
            try {
                Class<?> aClass = Class.forName(className);
                if (!aClass.isAnnotationPresent(Component.class)) {
                    continue;
                }
                // TODO: 第一版本，直接创建对象，但实际上这里并不会创建
                Object bean = aClass.newInstance();
                String simpleName = aClass.getSimpleName();
                // 首字母转小写
                simpleName = simpleName.substring(0,1).toLowerCase()+ simpleName.substring(1);
                beanMap.put(simpleName, bean);
                LOG.debug("simpleName: {}, bean: {}", simpleName, bean);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

    public Object getBean(String name) {
        return beanMap.get(name);
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
