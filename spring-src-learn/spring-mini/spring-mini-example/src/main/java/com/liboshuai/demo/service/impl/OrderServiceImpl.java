package com.liboshuai.demo.service.impl;

import com.liboshuai.demo.*;
import com.liboshuai.demo.service.OrderService;
import com.liboshuai.demo.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Lazy
@Scope("singleton")
@Component
public class OrderServiceImpl implements OrderService, InitializingBean, BeanNameAware, ApplicationContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(OrderServiceImpl.class);

    private String orderNo;
    private String beanName;
    private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();

    @Autowired
    private UserService userServiceImpl;

    @Override
    public void test() {
        LOG.info(">>> OrderServiceImpl的test方法被调用了");
        LOG.info(">>> orderNo: {}", orderNo);
        LOG.info(">>> beanName: {}", beanName);
        LOG.info(">>> beanDefinitionMap.keySet(): {}", beanDefinitionMap.keySet());
        userServiceImpl.test();
    }

    @PostConstruct
    public void testPostConstruct() {
        LOG.info(">>> OrderServiceImpl类的testPostConstruct()方法被调用了");
    }

    @Override
    public void afterPropertiesSet() {
        orderNo = "e4bec318-73f9-42c3-970a-65b5359bf705";
    }

    @Override
    public void setBeanName(String name) {
        beanName = name;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        beanDefinitionMap = applicationContext.getBeanDefinitionMap();
    }
}
