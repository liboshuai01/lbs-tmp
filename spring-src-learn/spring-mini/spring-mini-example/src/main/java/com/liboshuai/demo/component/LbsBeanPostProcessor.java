package com.liboshuai.demo.component;

import com.liboshuai.demo.BeanPostProcessor;
import com.liboshuai.demo.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class LbsBeanPostProcessor implements BeanPostProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(LbsBeanPostProcessor.class);

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        LOG.info(">>> 用户自定义的BeanPostProcessor");
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
