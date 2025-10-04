package com.liboshuai.demo.component;

import com.liboshuai.demo.BeanPostProcessor;
import com.liboshuai.demo.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class LbsBeanPostProcessor implements BeanPostProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(LbsBeanPostProcessor.class);

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        LOG.info(">>> [{}]自定义的BeanPostProcessor-before", beanName);
        return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        LOG.info(">>> [{}]自定义的BeanPostProcessor-after", beanName);
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
